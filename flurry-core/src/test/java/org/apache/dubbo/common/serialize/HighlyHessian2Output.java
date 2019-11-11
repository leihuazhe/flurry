package org.apache.dubbo.common.serialize;

import com.alibaba.com.caucho.hessian.io.AbstractHessianOutput;
import com.alibaba.com.caucho.hessian.io.Hessian2Constants;
import com.alibaba.com.caucho.hessian.io.Serializer;
import com.alibaba.com.caucho.hessian.util.IdentityIntMap;
import org.apache.dubbo.remoting.buffer.ChannelBufferOutputStream;
import org.apache.dubbo.remoting.transport.netty4.NettyBackedChannelBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Denim.leihz 2019-07-10 4:24 PM
 */
public class HighlyHessian2Output
        extends AbstractHessianOutput
        implements Hessian2Constants {

    public final static int SIZE = 4096;
    private final byte[] _buffer = new byte[SIZE];
    // the output stream/
    protected OutputStream _os;
    // map of references
    private IdentityIntMap _refs = new IdentityIntMap();
    private boolean _isCloseStreamOnClose;
    // map of classes
    private HashMap _classRefs;
    // map of types
    private HashMap _typeRefs;
    private int _offset;

    private boolean _isStreaming;

    /**
     * Creates a new Hessian output stream, initialized with an
     * underlying output stream.
     *
     * @param os the underlying output stream.
     */
    public HighlyHessian2Output(OutputStream os) {
        _os = os;
    }

    public boolean isCloseStreamOnClose() {
        return _isCloseStreamOnClose;
    }

    public void setCloseStreamOnClose(boolean isClose) {
        _isCloseStreamOnClose = isClose;
    }


    public void reWriteIndex(int offset, int length) throws IOException {
        writeInt(offset, length);
    }


    @Override
    public void call(String method, Object[] args)
            throws IOException {
        int length = args != null ? args.length : 0;

        startCall(method, length);

        for (int i = 0; i < args.length; i++)
            writeObject(args[i]);

        completeCall();
    }


    @Override
    public void startCall(String method, int length)
            throws IOException {
        int offset = _offset;

        if (SIZE < offset + 32) {
            flush();
            offset = _offset;
        }

        byte[] buffer = _buffer;

        buffer[_offset++] = (byte) 'C';

        writeString(method);
        writeInt(length);
    }


    @Override
    public void startCall()
            throws IOException {
        flushIfFull();

        _buffer[_offset++] = (byte) 'C';
    }


    public void startEnvelope(String method)
            throws IOException {
        int offset = _offset;

        if (SIZE < offset + 32) {
            flush();
            offset = _offset;
        }

        _buffer[_offset++] = (byte) 'E';

        writeString(method);
    }


    public void completeEnvelope()
            throws IOException {
        flushIfFull();

        _buffer[_offset++] = (byte) 'Z';
    }


    @Override
    public void writeMethod(String method)
            throws IOException {
        writeString(method);
    }


    @Override
    public void completeCall()
            throws IOException {
    }

    @Override
    public void startReply()
            throws IOException {
        writeVersion();

        flushIfFull();

        _buffer[_offset++] = (byte) 'R';
    }

    public void writeVersion()
            throws IOException {
        flushIfFull();
        _buffer[_offset++] = (byte) 'H';
        _buffer[_offset++] = (byte) 2;
        _buffer[_offset++] = (byte) 0;
    }

    @Override
    public void completeReply()
            throws IOException {
    }

    public void startMessage()
            throws IOException {
        flushIfFull();

        _buffer[_offset++] = (byte) 'p';
        _buffer[_offset++] = (byte) 2;
        _buffer[_offset++] = (byte) 0;
    }

    public void completeMessage()
            throws IOException {
        flushIfFull();

        _buffer[_offset++] = (byte) 'z';
    }

    @Override
    public void writeFault(String code, String message, Object detail)
            throws IOException {
        flushIfFull();

        writeVersion();

        _buffer[_offset++] = (byte) 'F';
        _buffer[_offset++] = (byte) 'H';

        _refs.put(new HashMap(), _refs.size());

        writeString("code");
        writeString(code);

        writeString("message");
        writeString(message);

        if (detail != null) {
            writeString("detail");
            writeObject(detail);
        }

        flushIfFull();
        _buffer[_offset++] = (byte) 'Z';
    }

    @Override
    public void writeObject(Object object)
            throws IOException {
        if (object == null) {
            writeNull();
            return;
        }

        Serializer serializer;

        serializer = findSerializerFactory().getSerializer(object.getClass());

        serializer.writeObject(object, this);
    }

    @Override
    public boolean writeListBegin(int length, String type)
            throws IOException {
        flushIfFull();

        if (length < 0) {
            if (type != null) {
                _buffer[_offset++] = (byte) BC_LIST_VARIABLE;
                writeType(type);
            } else
                _buffer[_offset++] = (byte) BC_LIST_VARIABLE_UNTYPED;

            return true;
        } /*else if (length <= LIST_DIRECT_MAX) {
            if (type != null) {
                _buffer[_offset++] = (byte) (BC_LIST_DIRECT + length);
                writeType(type);
            } else {
                _buffer[_offset++] = (byte) (BC_LIST_DIRECT_UNTYPED + length);
            }

            return false;
        }*/ else {
            if (type != null) {
                _buffer[_offset++] = (byte) BC_LIST_FIXED;
                writeType(type);
            } else {
                _buffer[_offset++] = (byte) BC_LIST_FIXED_UNTYPED;
            }

            writeInt(length);

            return false;
        }
    }

    @Override
    public void writeListEnd()
            throws IOException {
        flushIfFull();

        _buffer[_offset++] = (byte) BC_END;
    }

    @Override
    public void writeMapBegin(String type)
            throws IOException {
        if (SIZE < _offset + 32)
            flush();

        if (type != null) {
            _buffer[_offset++] = BC_MAP;

            writeType(type);
        } else
            _buffer[_offset++] = BC_MAP_UNTYPED;
    }

    @Override
    public void writeMapEnd()
            throws IOException {
        if (SIZE < _offset + 32)
            flush();

        _buffer[_offset++] = (byte) BC_END;
    }

    @Override
    public int writeObjectBegin(String type)
            throws IOException {
        if (_classRefs == null)
            _classRefs = new HashMap();

        Integer refV = (Integer) _classRefs.get(type);

        if (refV != null) {
            int ref = refV.intValue();

            if (SIZE < _offset + 32)
                flush();

            if (ref <= OBJECT_DIRECT_MAX) {
                _buffer[_offset++] = (byte) (BC_OBJECT_DIRECT + ref);
            } else {
                _buffer[_offset++] = (byte) 'O';
                writeInt(ref);
            }

            return ref;
        } else {
            int ref = _classRefs.size();

            _classRefs.put(type, Integer.valueOf(ref));

            if (SIZE < _offset + 32)
                flush();

            _buffer[_offset++] = (byte) 'C';

            writeString(type);

            return -1;
        }
    }

    @Override
    public void writeClassFieldLength(int len)
            throws IOException {
        writeInt(len);
    }

    @Override
    public void writeObjectEnd()
            throws IOException {
    }

    private void writeType(String type)
            throws IOException {
        flushIfFull();

        int len = type.length();
        if (len == 0) {
            throw new IllegalArgumentException("empty type is not allowed");
        }

        if (_typeRefs == null)
            _typeRefs = new HashMap();

        Integer typeRefV = (Integer) _typeRefs.get(type);

        if (typeRefV != null) {
            int typeRef = typeRefV.intValue();

            writeInt(typeRef);
        } else {
            _typeRefs.put(type, Integer.valueOf(_typeRefs.size()));

            writeString(type);
        }
    }

    @Override
    public void writeBoolean(boolean value)
            throws IOException {
        if (SIZE < _offset + 16)
            flush();

        if (value)
            _buffer[_offset++] = (byte) 'T';
        else
            _buffer[_offset++] = (byte) 'F';
    }

    @Override
    public void writeInt(int value)
            throws IOException {
        int offset = _offset;
        byte[] buffer = _buffer;

        if (SIZE <= offset + 16) {
            flush();
            offset = _offset;
        }

        if (INT_DIRECT_MIN <= value && value <= INT_DIRECT_MAX)
            buffer[offset++] = (byte) (value + BC_INT_ZERO);
        else if (INT_BYTE_MIN <= value && value <= INT_BYTE_MAX) {
            buffer[offset++] = (byte) (BC_INT_BYTE_ZERO + (value >> 8));
            buffer[offset++] = (byte) (value);
        } else if (INT_SHORT_MIN <= value && value <= INT_SHORT_MAX) {
            buffer[offset++] = (byte) (BC_INT_SHORT_ZERO + (value >> 16));
            buffer[offset++] = (byte) (value >> 8);
            buffer[offset++] = (byte) (value);
        } else {
            buffer[offset++] = (byte) ('I');
            buffer[offset++] = (byte) (value >> 24);
            buffer[offset++] = (byte) (value >> 16);
            buffer[offset++] = (byte) (value >> 8);
            buffer[offset++] = (byte) (value);
        }

        _offset = offset;
    }

    public void writeInt(int offset, int value)
            throws IOException {
        byte[] buffer = _buffer;

        if (INT_DIRECT_MIN <= value && value <= INT_DIRECT_MAX)
            buffer[offset++] = (byte) (value + BC_INT_ZERO);
        else if (INT_BYTE_MIN <= value && value <= INT_BYTE_MAX) {
            buffer[offset++] = (byte) (BC_INT_BYTE_ZERO + (value >> 8));
            buffer[offset++] = (byte) (value);
        } else if (INT_SHORT_MIN <= value && value <= INT_SHORT_MAX) {
            buffer[offset++] = (byte) (BC_INT_SHORT_ZERO + (value >> 16));
            buffer[offset++] = (byte) (value >> 8);
            buffer[offset++] = (byte) (value);
        } else {
            buffer[offset++] = (byte) ('I');
            buffer[offset++] = (byte) (value >> 24);
            buffer[offset++] = (byte) (value >> 16);
            buffer[offset++] = (byte) (value >> 8);
            buffer[offset++] = (byte) (value);
        }
    }

    @Override
    public void writeLong(long value)
            throws IOException {
        int offset = _offset;
        byte[] buffer = _buffer;

        if (SIZE <= offset + 16) {
            flush();
            offset = _offset;
        }

        if (LONG_DIRECT_MIN <= value && value <= LONG_DIRECT_MAX) {
            buffer[offset++] = (byte) (value + BC_LONG_ZERO);
        } else if (LONG_BYTE_MIN <= value && value <= LONG_BYTE_MAX) {
            buffer[offset++] = (byte) (BC_LONG_BYTE_ZERO + (value >> 8));
            buffer[offset++] = (byte) (value);
        } else if (LONG_SHORT_MIN <= value && value <= LONG_SHORT_MAX) {
            buffer[offset++] = (byte) (BC_LONG_SHORT_ZERO + (value >> 16));
            buffer[offset++] = (byte) (value >> 8);
            buffer[offset++] = (byte) (value);
        } else if (-0x80000000L <= value && value <= 0x7fffffffL) {
            buffer[offset + 0] = (byte) BC_LONG_INT;
            buffer[offset + 1] = (byte) (value >> 24);
            buffer[offset + 2] = (byte) (value >> 16);
            buffer[offset + 3] = (byte) (value >> 8);
            buffer[offset + 4] = (byte) (value);

            offset += 5;
        } else {
            buffer[offset + 0] = (byte) 'L';
            buffer[offset + 1] = (byte) (value >> 56);
            buffer[offset + 2] = (byte) (value >> 48);
            buffer[offset + 3] = (byte) (value >> 40);
            buffer[offset + 4] = (byte) (value >> 32);
            buffer[offset + 5] = (byte) (value >> 24);
            buffer[offset + 6] = (byte) (value >> 16);
            buffer[offset + 7] = (byte) (value >> 8);
            buffer[offset + 8] = (byte) (value);

            offset += 9;
        }

        _offset = offset;
    }

    @Override
    public void writeDouble(double value)
            throws IOException {
        int offset = _offset;
        byte[] buffer = _buffer;

        if (SIZE <= offset + 16) {
            flush();
            offset = _offset;
        }

        int intValue = (int) value;

        if (intValue == value) {
            if (intValue == 0) {
                buffer[offset++] = (byte) BC_DOUBLE_ZERO;

                _offset = offset;

                return;
            } else if (intValue == 1) {
                buffer[offset++] = (byte) BC_DOUBLE_ONE;

                _offset = offset;

                return;
            } else if (-0x80 <= intValue && intValue < 0x80) {
                buffer[offset++] = (byte) BC_DOUBLE_BYTE;
                buffer[offset++] = (byte) intValue;

                _offset = offset;

                return;
            } else if (-0x8000 <= intValue && intValue < 0x8000) {
                buffer[offset + 0] = (byte) BC_DOUBLE_SHORT;
                buffer[offset + 1] = (byte) (intValue >> 8);
                buffer[offset + 2] = (byte) intValue;

                _offset = offset + 3;

                return;
            }
        }

        int mills = (int) (value * 1000);

        if (0.001 * mills == value) {
            buffer[offset + 0] = (byte) (BC_DOUBLE_MILL);
            buffer[offset + 1] = (byte) (mills >> 24);
            buffer[offset + 2] = (byte) (mills >> 16);
            buffer[offset + 3] = (byte) (mills >> 8);
            buffer[offset + 4] = (byte) (mills);

            _offset = offset + 5;

            return;
        }

        long bits = Double.doubleToLongBits(value);

        buffer[offset + 0] = (byte) 'D';
        buffer[offset + 1] = (byte) (bits >> 56);
        buffer[offset + 2] = (byte) (bits >> 48);
        buffer[offset + 3] = (byte) (bits >> 40);
        buffer[offset + 4] = (byte) (bits >> 32);
        buffer[offset + 5] = (byte) (bits >> 24);
        buffer[offset + 6] = (byte) (bits >> 16);
        buffer[offset + 7] = (byte) (bits >> 8);
        buffer[offset + 8] = (byte) (bits);

        _offset = offset + 9;
    }

    @Override
    public void writeUTCDate(long time)
            throws IOException {
        if (SIZE < _offset + 32)
            flush();

        int offset = _offset;
        byte[] buffer = _buffer;

        if (time % 60000L == 0) {
            // compact date ::= x65 b3 b2 b1 b0

            long minutes = time / 60000L;

            if ((minutes >> 31) == 0 || (minutes >> 31) == -1) {
                buffer[offset++] = (byte) BC_DATE_MINUTE;
                buffer[offset++] = ((byte) (minutes >> 24));
                buffer[offset++] = ((byte) (minutes >> 16));
                buffer[offset++] = ((byte) (minutes >> 8));
                buffer[offset++] = ((byte) (minutes >> 0));

                _offset = offset;
                return;
            }
        }

        buffer[offset++] = (byte) BC_DATE;
        buffer[offset++] = ((byte) (time >> 56));
        buffer[offset++] = ((byte) (time >> 48));
        buffer[offset++] = ((byte) (time >> 40));
        buffer[offset++] = ((byte) (time >> 32));
        buffer[offset++] = ((byte) (time >> 24));
        buffer[offset++] = ((byte) (time >> 16));
        buffer[offset++] = ((byte) (time >> 8));
        buffer[offset++] = ((byte) (time));

        _offset = offset;
    }

    @Override
    public void writeNull()
            throws IOException {
        int offset = _offset;
        byte[] buffer = _buffer;

        if (SIZE <= offset + 16) {
            flush();
            offset = _offset;
        }

        buffer[offset++] = 'N';

        _offset = offset;
    }

    @Override
    public void writeString(String value)
            throws IOException {
        int offset = _offset;
        byte[] buffer = _buffer;

        if (SIZE <= offset + 16) {
            flush();
            offset = _offset;
        }

        if (value == null) {
            buffer[offset++] = (byte) 'N';

            _offset = offset;
        } else {
            int length = value.length();
            int strOffset = 0;

            while (length > 0x8000) {
                int sublen = 0x8000;

                offset = _offset;

                if (SIZE <= offset + 16) {
                    flush();
                    offset = _offset;
                }

                // chunk can't end in high surrogate
                char tail = value.charAt(strOffset + sublen - 1);

                if (0xd800 <= tail && tail <= 0xdbff)
                    sublen--;

                buffer[offset + 0] = (byte) BC_STRING_CHUNK;
                buffer[offset + 1] = (byte) (sublen >> 8);
                buffer[offset + 2] = (byte) (sublen);

                _offset = offset + 3;

                printString(value, strOffset, sublen);

                length -= sublen;
                strOffset += sublen;
            }

            offset = _offset;

            if (SIZE <= offset + 16) {
                flush();
                offset = _offset;
            }

            if (length <= STRING_DIRECT_MAX) {
                buffer[offset++] = (byte) (BC_STRING_DIRECT + length);
            } else if (length <= STRING_SHORT_MAX) {
                buffer[offset++] = (byte) (BC_STRING_SHORT + (length >> 8));
                buffer[offset++] = (byte) (length);
            } else {
                buffer[offset++] = (byte) ('S');
                buffer[offset++] = (byte) (length >> 8);
                buffer[offset++] = (byte) (length);
            }

            _offset = offset;

            printString(value, strOffset, length);
        }
    }

    @Override
    public void writeString(char[] buffer, int offset, int length)
            throws IOException {
        if (buffer == null) {
            if (SIZE < _offset + 16)
                flush();

            _buffer[_offset++] = (byte) ('N');
        } else {
            while (length > 0x8000) {
                int sublen = 0x8000;

                if (SIZE < _offset + 16)
                    flush();

                // chunk can't end in high surrogate
                char tail = buffer[offset + sublen - 1];

                if (0xd800 <= tail && tail <= 0xdbff)
                    sublen--;

                _buffer[_offset++] = (byte) BC_STRING_CHUNK;
                _buffer[_offset++] = (byte) (sublen >> 8);
                _buffer[_offset++] = (byte) (sublen);

                printString(buffer, offset, sublen);

                length -= sublen;
                offset += sublen;
            }

            if (SIZE < _offset + 16)
                flush();

            if (length <= STRING_DIRECT_MAX) {
                _buffer[_offset++] = (byte) (BC_STRING_DIRECT + length);
            } else if (length <= STRING_SHORT_MAX) {
                _buffer[_offset++] = (byte) (BC_STRING_SHORT + (length >> 8));
                _buffer[_offset++] = (byte) length;
            } else {
                _buffer[_offset++] = (byte) ('S');
                _buffer[_offset++] = (byte) (length >> 8);
                _buffer[_offset++] = (byte) (length);
            }

            printString(buffer, offset, length);
        }
    }

    @Override
    public void writeBytes(byte[] buffer)
            throws IOException {
        if (buffer == null) {
            if (SIZE < _offset + 16)
                flush();

            _buffer[_offset++] = 'N';
        } else
            writeBytes(buffer, 0, buffer.length);
    }

    @Override
    public void writeBytes(byte[] buffer, int offset, int length)
            throws IOException {
        if (buffer == null) {
            if (SIZE < _offset + 16)
                flushBuffer();

            _buffer[_offset++] = (byte) 'N';
        } else {
            flush();

            while (SIZE - _offset - 3 < length) {
                int sublen = SIZE - _offset - 3;

                if (sublen < 16) {
                    flushBuffer();

                    sublen = SIZE - _offset - 3;

                    if (length < sublen)
                        sublen = length;
                }

                _buffer[_offset++] = (byte) BC_BINARY_CHUNK;
                _buffer[_offset++] = (byte) (sublen >> 8);
                _buffer[_offset++] = (byte) sublen;

                System.arraycopy(buffer, offset, _buffer, _offset, sublen);
                _offset += sublen;

                length -= sublen;
                offset += sublen;

                flushBuffer();
            }

            if (SIZE < _offset + 16)
                flushBuffer();

            if (length <= BINARY_DIRECT_MAX) {
                _buffer[_offset++] = (byte) (BC_BINARY_DIRECT + length);
            } else if (length <= BINARY_SHORT_MAX) {
                _buffer[_offset++] = (byte) (BC_BINARY_SHORT + (length >> 8));
                _buffer[_offset++] = (byte) (length);
            } else {
                _buffer[_offset++] = (byte) 'B';
                _buffer[_offset++] = (byte) (length >> 8);
                _buffer[_offset++] = (byte) (length);
            }

            System.arraycopy(buffer, offset, _buffer, _offset, length);

            _offset += length;
        }
    }

    @Override
    public void writeByteBufferStart()
            throws IOException {
    }

    @Override
    public void writeByteBufferPart(byte[] buffer, int offset, int length)
            throws IOException {
    }

    /**
     * Writes a byte buffer to the stream.
     * <p>
     * <code><pre>
     * b b16 b18 bytes
     * </pre></code>
     */
    @Override
    public void writeByteBufferEnd(byte[] buffer, int offset, int length)
            throws IOException {
        writeBytes(buffer, offset, length);
    }

    @Override
    protected void writeRef(int value)
            throws IOException {
        if (SIZE < _offset + 16)
            flush();

        _buffer[_offset++] = (byte) BC_REF;

        writeInt(value);
    }

    @Override
    public boolean addRef(Object object)
            throws IOException {
        int ref = _refs.get(object);

        if (ref >= 0) {
            writeRef(ref);

            return true;
        } else {
            _refs.put(object, _refs.size());

            return false;
        }
    }

    @Override
    public boolean removeRef(Object obj)
            throws IOException {
        if (_refs != null) {
            _refs.remove(obj);

            return true;
        } else
            return false;
    }

    /**
     * Replaces a reference from one object to another.
     */
    @Override
    public boolean replaceRef(Object oldRef, Object newRef)
            throws IOException {
        Integer value = (Integer) _refs.remove(oldRef);

        if (value != null) {
            _refs.put(newRef, value);
            return true;
        } else
            return false;
    }

    /**
     * Resets the references for streaming.
     */
    @Override
    public void resetReferences() {
        if (_refs != null)
            _refs.clear();
    }

    /**
     * Prints a string to the stream, encoded as UTF-8 with preceeding length
     *
     * @param v the string to print.
     */
    public void printLenString(String v)
            throws IOException {
        if (SIZE < _offset + 16)
            flush();

        if (v == null) {
            _buffer[_offset++] = (byte) (0);
            _buffer[_offset++] = (byte) (0);
        } else {
            int len = v.length();
            _buffer[_offset++] = (byte) (len >> 8);
            _buffer[_offset++] = (byte) (len);

            printString(v, 0, len);
        }
    }

    /**
     * Prints a string to the stream, encoded as UTF-8
     *
     * @param v the string to print.
     */
    public void printString(String v, int strOffset, int length)
            throws IOException {
        int offset = _offset;
        byte[] buffer = _buffer;

        for (int i = 0; i < length; i++) {
            if (SIZE <= offset + 16) {
                _offset = offset;
                flush();
                offset = _offset;
            }

            char ch = v.charAt(i + strOffset);

            if (ch < 0x80)
                buffer[offset++] = (byte) (ch);
            else if (ch < 0x800) {
                buffer[offset++] = (byte) (0xc0 + ((ch >> 6) & 0x1f));
                buffer[offset++] = (byte) (0x80 + (ch & 0x3f));
            } else {
                buffer[offset++] = (byte) (0xe0 + ((ch >> 12) & 0xf));
                buffer[offset++] = (byte) (0x80 + ((ch >> 6) & 0x3f));
                buffer[offset++] = (byte) (0x80 + (ch & 0x3f));
            }
        }

        _offset = offset;
    }

    /**
     * Prints a string to the stream, encoded as UTF-8
     *
     * @param v the string to print.
     */
    public void printString(char[] v, int strOffset, int length)
            throws IOException {
        int offset = _offset;
        byte[] buffer = _buffer;

        for (int i = 0; i < length; i++) {
            if (SIZE <= offset + 16) {
                _offset = offset;
                flush();
                offset = _offset;
            }

            char ch = v[i + strOffset];

            if (ch < 0x80)
                buffer[offset++] = (byte) (ch);
            else if (ch < 0x800) {
                buffer[offset++] = (byte) (0xc0 + ((ch >> 6) & 0x1f));
                buffer[offset++] = (byte) (0x80 + (ch & 0x3f));
            } else {
                buffer[offset++] = (byte) (0xe0 + ((ch >> 12) & 0xf));
                buffer[offset++] = (byte) (0x80 + ((ch >> 6) & 0x3f));
                buffer[offset++] = (byte) (0x80 + (ch & 0x3f));
            }
        }

        _offset = offset;
    }

    private final void flushIfFull()
            throws IOException {
        int offset = _offset;

        if (SIZE < offset + 32) {
            _offset = 0;
            _os.write(_buffer, 0, offset);
        }
    }

    @Override
    public final void flush()
            throws IOException {
        flushBuffer();

        if (_os != null)
            _os.flush();
    }

    public final void flushBuffer()
            throws IOException {
        int offset = _offset;

        if (!_isStreaming && offset > 0) {
            _offset = 0;

            _os.write(_buffer, 0, offset);
        } else if (_isStreaming && offset > 3) {
            int len = offset - 3;
            _buffer[0] = 'p';
            _buffer[1] = (byte) (len >> 8);
            _buffer[2] = (byte) len;
            _offset = 3;

            _os.write(_buffer, 0, offset);
        }
    }

    @Override
    public final void close()
            throws IOException {
        // hessian/3a8c
        flush();

        OutputStream os = _os;
        _os = null;

        if (os != null) {
            if (_isCloseStreamOnClose)
                os.close();
        }
    }


    class BytesOutputStream extends OutputStream {
        private int _startOffset;

        BytesOutputStream()
                throws IOException {
            if (SIZE < _offset + 16) {
                flush();
            }

            _startOffset = _offset;
            _offset += 3; // skip 'b' xNN xNN
        }

        @Override
        public void write(int ch)
                throws IOException {
            if (SIZE <= _offset) {
                int length = (_offset - _startOffset) - 3;

                _buffer[_startOffset] = (byte) BC_BINARY_CHUNK;
                _buffer[_startOffset + 1] = (byte) (length >> 8);
                _buffer[_startOffset + 2] = (byte) (length);

                flush();

                _startOffset = _offset;
                _offset += 3;
            }

            _buffer[_offset++] = (byte) ch;
        }

        @Override
        public void write(byte[] buffer, int offset, int length)
                throws IOException {
            while (length > 0) {
                int sublen = SIZE - _offset;

                if (length < sublen)
                    sublen = length;

                if (sublen > 0) {
                    System.arraycopy(buffer, offset, _buffer, _offset, sublen);
                    _offset += sublen;
                }

                length -= sublen;
                offset += sublen;

                if (SIZE <= _offset) {
                    int chunkLength = (_offset - _startOffset) - 3;

                    _buffer[_startOffset] = (byte) BC_BINARY_CHUNK;
                    _buffer[_startOffset + 1] = (byte) (chunkLength >> 8);
                    _buffer[_startOffset + 2] = (byte) (chunkLength);

                    flush();

                    _startOffset = _offset;
                    _offset += 3;
                }
            }
        }

        @Override
        public void close()
                throws IOException {
            int startOffset = _startOffset;
            _startOffset = -1;

            if (startOffset < 0)
                return;

            int length = (_offset - startOffset) - 3;

            _buffer[startOffset] = (byte) 'B';
            _buffer[startOffset + 1] = (byte) (length >> 8);
            _buffer[startOffset + 2] = (byte) (length);

            flush();
        }
    }


    //==========
    //
    // 自定义新加入的方法
    // 8.26 fix 这里需要考虑已经 flush 的情况.
    // =========

    /**
     * 当前写指针,包括byteBuf + 缓冲区 byte offset
     *
     * @return index.
     */
    public int markIndex() {
        ChannelBufferOutputStream bos = (ChannelBufferOutputStream) _os;
        NettyBackedChannelBuffer nbBuffer = (NettyBackedChannelBuffer) bos.buffer();
        //当前buffer 的指针
        int bufferIndex = nbBuffer.writerIndex();
        return bufferIndex + _offset;
    }

    /**
     * mark buffer byte array 缓冲区
     */
    public int markBufferOffset() {
        return _offset;
    }

    public void resetIndex(int offset) throws IOException {
        _offset = offset;
    }

    /**
     * 自定义，重写 List 长度
     *
     * @param offset 在上一次某次标记的 write index.
     * @param length 写长度
     * @throws IOException DumpUtil.dump(getByteBuf().buffer);
     */
    public void reWriteListLength(int offset, int length)
            throws IOException {
        int bufferIndex = getByteBufWriteIndex();
        //说明原来mark的index 还没有 flush 到 buffer 中
        if (offset > bufferIndex) {
           /* int currentOffset = _offset;
            _offset = offset - bufferIndex;
            writeListBegin(length, null);
            _offset = currentOffset;*/
            flush();
        }
        //已经写入到了ByteBuf
        int currentOffset = _offset;
        customWriteListBegin(length);
        byte[] needFlush = Arrays.copyOfRange(_buffer, currentOffset, _offset);

        getByteBuf().markWriterIndex();

        getByteBuf().writerIndex(offset);
        getByteBuf().writeBytes(needFlush, 0, needFlush.length);

        getByteBuf().resetWriterIndex();

        _offset = currentOffset;
    }

    /**
     * @param reBackIndex      byteBuf 重写指针
     * @param markCurrentIndex 从哪里开始的 offset
     */
    public void reWriteBuf(int reBackIndex, int markCurrentIndex) {
        getByteBuf().markWriterIndex();
        //已经写入到了ByteBuf
        byte[] needFlush = Arrays.copyOfRange(_buffer, markCurrentIndex, _offset);
        getByteBuf().writerIndex(reBackIndex);

        getByteBuf().writeBytes(needFlush, 0, needFlush.length);

        getByteBuf().resetWriterIndex();

        _offset = markCurrentIndex;
    }

    /**
     * Returns the {@code writerIndex} of this buffer.
     */
    public int getByteBufWriteIndex() {
        return getByteBuf().writerIndex();
    }

    /**
     * Returns  this buffer.
     */
    public NettyBackedChannelBuffer getByteBuf() {
        ChannelBufferOutputStream bos = (ChannelBufferOutputStream) _os;
        return (NettyBackedChannelBuffer) bos.buffer();
    }

    public ChannelBufferOutputStream getOS() {
        return (ChannelBufferOutputStream) _os;
    }

    /**
     * 写 集合 length 长度,直接写大，兼容 7以下的
     *
     * @param length 集合长度.
     * @throws IOException
     */
    private void customWriteListBegin(int length) throws IOException {
        /*if (length <= LIST_DIRECT_MAX) {
            _buffer[_offset++] = (byte) (BC_LIST_DIRECT_UNTYPED + length);
        } else {
            _buffer[_offset++] = (byte) BC_LIST_FIXED_UNTYPED;
            writeInt(length);
        }*/
        _buffer[_offset++] = (byte) BC_LIST_FIXED_UNTYPED;
        writeInt(length);
    }
}
