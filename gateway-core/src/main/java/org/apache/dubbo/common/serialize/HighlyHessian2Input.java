package org.apache.dubbo.common.serialize;

import com.yunji.gateway.jsonserializer.JsonCallback;
import org.apache.dubbo.common.serialize.compatible.CodecContext;
import org.apache.dubbo.common.serialize.compatible.Offset;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Denim.leihz 2019-07-11 9:17 AM
 */
public class HighlyHessian2Input extends HiglyHessian2InputCompatible {

    private JsonCallback jsonWriter;

    public void setJsonCallback(JsonCallback jsonWriter) {
        this.jsonWriter = jsonWriter;
    }

    public HighlyHessian2Input(InputStream is) {
        super(is);
    }

    @Override
    public Object readObject(List<Class<?>> expectedTypes) throws IOException {
        if (CodecContext.getContext().isUseJsonDecoder()) {
            return readObject0(expectedTypes);
        }
        return super.readObject(expectedTypes);
    }


    @Override
    protected Object readObjectInstance(Class cl, ObjectDefinition def)
            throws IOException {
        if (CodecContext.getContext().isUseJsonDecoder()) {
            return readObjectInstance0(cl, def);
        }
        return super.readObjectInstance(cl, def);
    }


    // =======================================================
    // Json 自定义需要修改的一些逻辑
    //========================================================
    private Object readObject0(List<Class<?>> expectedTypes) throws IOException {
        int tag = _offset < _length ? (_buffer[_offset++] & 0xff) : read();

        switch (tag) {
            case 'N':
                if (ifExist()) {
                    jsonWriter.onNull();
                }
                return null;

            case 'T':
                if (ifExist()) {
                    jsonWriter.onBoolean(true);
                }
                return Boolean.TRUE;

            case 'F':
                if (ifExist()) {
                    jsonWriter.onBoolean(false);
                }
                return Boolean.FALSE;

            // direct integer
            case 0x80:
            case 0x81:
            case 0x82:
            case 0x83:
            case 0x84:
            case 0x85:
            case 0x86:
            case 0x87:
            case 0x88:
            case 0x89:
            case 0x8a:
            case 0x8b:
            case 0x8c:
            case 0x8d:
            case 0x8e:
            case 0x8f:

            case 0x90:
            case 0x91:
            case 0x92:
            case 0x93:
            case 0x94:
            case 0x95:
            case 0x96:
            case 0x97:
            case 0x98:
            case 0x99:
            case 0x9a:
            case 0x9b:
            case 0x9c:
            case 0x9d:
            case 0x9e:
            case 0x9f:

            case 0xa0:
            case 0xa1:
            case 0xa2:
            case 0xa3:
            case 0xa4:
            case 0xa5:
            case 0xa6:
            case 0xa7:
            case 0xa8:
            case 0xa9:
            case 0xaa:
            case 0xab:
            case 0xac:
            case 0xad:
            case 0xae:
            case 0xaf:

            case 0xb0:
            case 0xb1:
            case 0xb2:
            case 0xb3:
            case 0xb4:
            case 0xb5:
            case 0xb6:
            case 0xb7:
            case 0xb8:
            case 0xb9:
            case 0xba:
            case 0xbb:
            case 0xbc:
            case 0xbd:
            case 0xbe:
            case 0xbf:
                int int1 = tag - BC_INT_ZERO;
                if (ifExist()) {
                    jsonWriter.onNumber(int1);
                }
                return int1;

            /* byte int */
            case 0xc0:
            case 0xc1:
            case 0xc2:
            case 0xc3:
            case 0xc4:
            case 0xc5:
            case 0xc6:
            case 0xc7:
            case 0xc8:
            case 0xc9:
            case 0xca:
            case 0xcb:
            case 0xcc:
            case 0xcd:
            case 0xce:
            case 0xcf:
                int int2 = ((tag - BC_INT_BYTE_ZERO) << 8) + read();
                if (ifExist()) {
                    jsonWriter.onNumber(int2);
                }
                return int2;

            /* short int */
            case 0xd0:
            case 0xd1:
            case 0xd2:
            case 0xd3:
            case 0xd4:
            case 0xd5:
            case 0xd6:
            case 0xd7:
                int int3 = ((tag - BC_INT_SHORT_ZERO) << 16) + 256 * read() + read();
                if (ifExist()) {
                    jsonWriter.onNumber(int3);
                }
                return int3;

            case 'I':
                int int4 = parseInt();
                if (ifExist()) {
                    jsonWriter.onNumber(int4);
                }
                return int4;
            // direct long
            case 0xd8:
            case 0xd9:
            case 0xda:
            case 0xdb:
            case 0xdc:
            case 0xdd:
            case 0xde:
            case 0xdf:

            case 0xe0:
            case 0xe1:
            case 0xe2:
            case 0xe3:
            case 0xe4:
            case 0xe5:
            case 0xe6:
            case 0xe7:
            case 0xe8:
            case 0xe9:
            case 0xea:
            case 0xeb:
            case 0xec:
            case 0xed:
            case 0xee:
            case 0xef:
                long long1 = (long) (tag - BC_LONG_ZERO);
                if (ifExist()) {
                    jsonWriter.onNumber(long1);
                }
                return long1;
            /* byte long */
            case 0xf0:
            case 0xf1:
            case 0xf2:
            case 0xf3:
            case 0xf4:
            case 0xf5:
            case 0xf6:
            case 0xf7:
            case 0xf8:
            case 0xf9:
            case 0xfa:
            case 0xfb:
            case 0xfc:
            case 0xfd:
            case 0xfe:
            case 0xff:
                long long2 = (long) (((tag - BC_LONG_BYTE_ZERO) << 8) + read());
                if (ifExist()) {
                    jsonWriter.onNumber(long2);
                }
                return long2;

            /* short long */
            case 0x38:
            case 0x39:
            case 0x3a:
            case 0x3b:
            case 0x3c:
            case 0x3d:
            case 0x3e:
            case 0x3f:
                long long3 = (long) (((tag - BC_LONG_SHORT_ZERO) << 16) + 256 * read() + read());
                if (ifExist()) {
                    jsonWriter.onNumber(long3);
                }
                return long3;

            case BC_LONG_INT:
                long long4 = (long) parseInt();
                if (ifExist()) {
                    jsonWriter.onNumber(long4);
                }
                return long4;

            case 'L':
                long long5 = parseLong();
                if (ifExist()) {
                    jsonWriter.onNumber(long5);
                }
                return long5;

            case BC_DOUBLE_ZERO:
                double d1 = (double) 0;
                if (ifExist()) {
                    jsonWriter.onNumber(d1);
                }
                return d1;

            case BC_DOUBLE_ONE:
                double d2 = (double) 1;
                if (ifExist()) {
                    jsonWriter.onNumber(d2);
                }
                return d2;

            case BC_DOUBLE_BYTE:
                double d3 = (double) (byte) read();
                if (ifExist()) {
                    jsonWriter.onNumber(d3);
                }
                return d3;

            case BC_DOUBLE_SHORT:
                double d4 = (double) (short) (256 * read() + read());
                if (ifExist()) {
                    jsonWriter.onNumber(d4);
                }
                return d4;

            case BC_DOUBLE_MILL: {
                int mills = parseInt();

                double d5 = 0.001 * mills;
                if (ifExist()) {
                    jsonWriter.onNumber(d5);
                }
                return d5;
            }

            case 'D':
                double d6 = parseDouble();
                if (ifExist()) {
                    jsonWriter.onNumber(d6);
                }
                return d6;

            //Date 时间类型
            case BC_DATE:
                long ts = parseLong();
                if (ifExist()) {
                    jsonWriter.onNumber(ts);
                }
                return ts;
//                return new Date(parseLong());

            case BC_DATE_MINUTE:
                long bCts = parseInt() * 60000L;
                if (ifExist()) {
                    jsonWriter.onNumber(bCts);
                }
                return bCts;
//                return new Date(parseInt() * 60000L);

            case BC_STRING_CHUNK:
            case 'S': {
                _isLastChunk = tag == 'S';
                _chunkLength = (read() << 8) + read();

                _sbuf.setLength(0);

                parseString(_sbuf);

                String s1 = _sbuf.toString();
                if (ifExist()) {
                    jsonWriter.onString(s1);
                }
                return s1;
            }

            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
            case 0x04:
            case 0x05:
            case 0x06:
            case 0x07:
            case 0x08:
            case 0x09:
            case 0x0a:
            case 0x0b:
            case 0x0c:
            case 0x0d:
            case 0x0e:
            case 0x0f:

            case 0x10:
            case 0x11:
            case 0x12:
            case 0x13:
            case 0x14:
            case 0x15:
            case 0x16:
            case 0x17:
            case 0x18:
            case 0x19:
            case 0x1a:
            case 0x1b:
            case 0x1c:
            case 0x1d:
            case 0x1e:
            case 0x1f: {
                _isLastChunk = true;
                _chunkLength = tag - 0x00;

                _sbuf.setLength(0);

                parseString(_sbuf);

                String s2 = _sbuf.toString();
                if (ifExist()) {
                    jsonWriter.onString(s2);
                }
                return s2;
            }

            case 0x30:
            case 0x31:
            case 0x32:
            case 0x33: {
                _isLastChunk = true;
                _chunkLength = (tag - 0x30) * 256 + read();

                _sbuf.setLength(0);

                parseString(_sbuf);

                String s3 = _sbuf.toString();
                if (ifExist()) {
                    jsonWriter.onString(s3);
                }
                return s3;
            }

            case BC_BINARY_CHUNK:
            case 'B': {
                _isLastChunk = tag == 'B';
                _chunkLength = (read() << 8) + read();

                int data;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                while ((data = parseByte()) >= 0)
                    bos.write(data);
                //todo
                return bos.toByteArray();
            }

            case 0x20:
            case 0x21:
            case 0x22:
            case 0x23:
            case 0x24:
            case 0x25:
            case 0x26:
            case 0x27:
            case 0x28:
            case 0x29:
            case 0x2a:
            case 0x2b:
            case 0x2c:
            case 0x2d:
            case 0x2e:
            case 0x2f: {
                _isLastChunk = true;
                int len = tag - 0x20;
                _chunkLength = 0;

                byte[] data = new byte[len];

                for (int i = 0; i < len; i++)
                    data[i] = (byte) read();
                //todo
                return data;
            }

            case 0x34:
            case 0x35:
            case 0x36:
            case 0x37: {
                _isLastChunk = true;
                int len = (tag - 0x34) * 256 + read();
                _chunkLength = 0;

                byte[] buffer = new byte[len];

                for (int i = 0; i < len; i++) {
                    buffer[i] = (byte) read();
                }
                //todo
                return buffer;
            }

            case BC_LIST_VARIABLE: {
                // variable length list
                String type = readType();
                //todo
                return findSerializerFactory().readList(this, -1, type);
            }

            case BC_LIST_VARIABLE_UNTYPED: {
                return findSerializerFactory().readList(this, -1, null);
            }

            case BC_LIST_FIXED: {
                // fixed length lists
                String type = readType();
                int length = readInt();
                customReadList(length);
                return null;
                /*
                Deserializer reader;
                reader = findSerializerFactory().getListDeserializer(type, null);

                boolean valueType = expectedTypes != null && expectedTypes.size() == 1;

                return reader.readLengthList(this, length, valueType ? expectedTypes.get(0) : null);*/
            }

            case BC_LIST_FIXED_UNTYPED: {
                // fixed length lists
                int length = readInt();
                customReadList(length);
                return null;
                /*Deserializer reader;
                reader = findSerializerFactory().getListDeserializer(null, null);

                boolean valueType = expectedTypes != null && expectedTypes.size() == 1;

                return reader.readLengthList(this, length, valueType ? expectedTypes.get(0) : null);*/
            }
            // compact fixed list
            case 0x70:
            case 0x71:
            case 0x72:
            case 0x73:
            case 0x74:
            case 0x75:
            case 0x76:
            case 0x77: {
                // fixed length lists
                String type = readType();
                int length = tag - 0x70;
                customReadList(length);
                return null;
            }

            // compact fixed untyped list
            case 0x78:
            case 0x79:
            case 0x7a:
            case 0x7b:
            case 0x7c:
            case 0x7d:
            case 0x7e:
            case 0x7f: {
                // fixed length lists
                int length = tag - 0x78;

                customReadList(length);
                return null;
            }

            case 'H': {
                customReadMap();
                return null;
            }

            case 'M': {
                String type = readType();
                customReadMap();
                return null;
//                return findSerializerFactory().readMap(this, type);
            }

            case 'C': {
                //开始一个 object
                readObjectDefinition(null);
                return readObject();
            }

            case 0x60:
            case 0x61:
            case 0x62:
            case 0x63:
            case 0x64:
            case 0x65:
            case 0x66:
            case 0x67:
            case 0x68:
            case 0x69:
            case 0x6a:
            case 0x6b:
            case 0x6c:
            case 0x6d:
            case 0x6e:
            case 0x6f: {
                int ref = tag - 0x60;

                if (_classDefs == null)
                    throw error("No classes defined at reference '{0}'" + tag);

                ObjectDefinition def = (ObjectDefinition) _classDefs.get(ref);

                return readObjectInstance(null, def);
            }

            case 'O': {
                int ref = readInt();

                ObjectDefinition def = (ObjectDefinition) _classDefs.get(ref);

                return readObjectInstance(null, def);
            }

            case BC_REF: {
                int ref = readInt();
                if (_refs != null) {
                    jsonWriter.copyObjectJson((Offset) _refs.get(ref));
                }
                return null;
            }

            default:
                if (tag < 0)
                    throw new EOFException("readObject: unexpected end of file");
                else
                    throw error("readObject: unknown code " + codeName(tag));
        }
    }

    private Object readObjectInstance0(Class cl, ObjectDefinition def)
            throws IOException {
        boolean special = judgeSpecificObjectDefinition(def);

        if (ifExist()) {
            int position = addRef(new Offset(jsonWriter.markIndex()));
            if (!special) {
                jsonWriter.onStartObject();
            }
            String[] fieldNames = def.getFieldNames();

            for (int i = 0; i < fieldNames.length; i++) {
                String name = fieldNames[i];
                if (!special) {
                    jsonWriter.onStartField(name);
                }
                readObject();
                if (!special) {
                    jsonWriter.onEndField();
                }
            }
            if (!special) {
                jsonWriter.onEndObject();
            }

            setRef(position, jsonWriter.markIndex());
            return null;
        } else {
            if (ifExist()) {
                jsonWriter.onStartObject();
            }
            String[] fieldNames = def.getFieldNames();

            for (int i = 0; i < fieldNames.length; i++) {
                String name = fieldNames[i];
                if (ifExist()) {
                    jsonWriter.onStartField(name);
                }
                readObject();
                if (ifExist()) {
                    jsonWriter.onEndField();
                }
            }
            if (ifExist()) {
                jsonWriter.onEndObject();
            }
            return null;
        }
    }


    private boolean ifExist() {
        return jsonWriter != null;
    }


    private void customReadList(int length) throws IOException {
        if (ifExist()) {
            jsonWriter.onStartArray();
        }
        for (; length > 0; length--) {
            readObject();
            jsonWriter.onEndField();
        }
        if (ifExist()) {
            jsonWriter.onEndArray();
        }
    }

    private void customReadMap() throws IOException {
        if (ifExist()) {
            jsonWriter.onStartObject();
        }

        while (!isEnd()) {
            readObject();

            if (ifExist()) {
                jsonWriter.onColon();
            }
            readObject();

            if (ifExist()) {
                jsonWriter.onEndField();
            }
        }
        readEnd();

        if (ifExist()) {
            jsonWriter.onEndObject();
        }
    }

    /**
     * 处理 hessian2 共用对象的情况
     */
    public int addRef(Offset offset) {
        if (_refs == null)
            _refs = new ArrayList();

        _refs.add(offset);

        return _refs.size() - 1;
    }

    public void setRef(int position, int endIndex) {
        if (_refs != null) {
            ((Offset) _refs.get(position)).setEndIndex(endIndex);
        }
    }

    /**
     * 特殊对象处理
     */
    private boolean judgeSpecificObjectDefinition(ObjectDefinition def) {
        if (def != null) {
            switch (def.getType()) {
                case "java.math.BigDecimal":
                    return true;
                default:
                    return false;
            }

        }
        return false;
    }
}
