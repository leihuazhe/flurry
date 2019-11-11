package org.apache.dubbo.common.serialize;

import org.apache.dubbo.common.serialize.hessian2.Hessian2SerializerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Denim.leihz 2019-07-10 6:21 PM
 */
public class HighlyHessian2ObjectOutput implements ObjectOutput {

    private final HighlyHessian2Output cmH2o;

    public HighlyHessian2ObjectOutput(OutputStream os) {
        cmH2o = new HighlyHessian2Output(os);
        cmH2o.setSerializerFactory(Hessian2SerializerFactory.SERIALIZER_FACTORY);
    }

    @Override
    public void writeBool(boolean v) throws IOException {
        cmH2o.writeBoolean(v);
    }

    @Override
    public void writeByte(byte v) throws IOException {
        cmH2o.writeInt(v);
    }

    @Override
    public void writeShort(short v) throws IOException {
        cmH2o.writeInt(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        cmH2o.writeInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        cmH2o.writeLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        cmH2o.writeDouble(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        cmH2o.writeDouble(v);
    }

    @Override
    public void writeBytes(byte[] b) throws IOException {
        cmH2o.writeBytes(b);
    }

    @Override
    public void writeBytes(byte[] b, int off, int len) throws IOException {
        cmH2o.writeBytes(b, off, len);
    }

    @Override
    public void writeUTF(String v) throws IOException {
        cmH2o.writeString(v);
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        cmH2o.writeObject(obj);
    }

    @Override
    public void flushBuffer() throws IOException {
        cmH2o.flushBuffer();
    }

    public void reWriteIndex(int offset, int length) throws IOException {
        cmH2o.reWriteIndex(offset, length);
    }

    public HighlyHessian2Output getCmH2o() {
        return cmH2o;
    }
}