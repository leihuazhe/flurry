package org.apache.dubbo.common.serialize;

import org.apache.dubbo.common.serialize.hessian2.Hessian2SerializerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * @author Denim.leihz 2019-07-10 9:07 PM
 */
public class HighlyHessian2ObjectInput implements ObjectInput {
    private final HighlyHessian2Input cmH2i;

    public HighlyHessian2ObjectInput(InputStream is) {
        cmH2i = new HighlyHessian2Input(is);
        cmH2i.setSerializerFactory(Hessian2SerializerFactory.SERIALIZER_FACTORY);
    }

    @Override
    public boolean readBool() throws IOException {
        return cmH2i.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return (byte) cmH2i.readInt();
    }

    @Override
    public short readShort() throws IOException {
        return (short) cmH2i.readInt();
    }

    @Override
    public int readInt() throws IOException {
        return cmH2i.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return cmH2i.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return (float) cmH2i.readDouble();
    }

    @Override
    public double readDouble() throws IOException {
        return cmH2i.readDouble();
    }

    @Override
    public byte[] readBytes() throws IOException {
        return cmH2i.readBytes();
    }

    @Override
    public String readUTF() throws IOException {
        return cmH2i.readString();
    }

    @Override
    public Object readObject() throws IOException {
        return cmH2i.readObject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T readObject(Class<T> cls) throws IOException,
            ClassNotFoundException {
        return (T) cmH2i.readObject(cls);
    }

    @Override
    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        return readObject(cls);
    }

    public HighlyHessian2Input getCmH2i() {
        return cmH2i;
    }
}