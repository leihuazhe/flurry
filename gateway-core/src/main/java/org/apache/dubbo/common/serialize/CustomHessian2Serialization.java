package org.apache.dubbo.common.serialize;

import org.apache.dubbo.common.URL;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.yunji.gateway.util.GateConstants.CUSTOME_HESSIAN2_SERIALIZATION_ID;
import static org.apache.dubbo.common.serialize.Constants.HESSIAN2_SERIALIZATION_ID;

/**
 * @author Denim.leihz 2019-07-10 6:20 PM
 */
public class CustomHessian2Serialization implements Serialization {

    @Override
    public byte getContentTypeId() {
        return CUSTOME_HESSIAN2_SERIALIZATION_ID;
//        return HESSIAN2_SERIALIZATION_ID;
    }

    @Override
    public String getContentType() {
        return "x-application/hessian2";
    }

    @Override
    public ObjectOutput serialize(URL url, OutputStream out) throws IOException {
        return new CustomHessian2ObjectOutput(out);
    }

    @Override
    public ObjectInput deserialize(URL url, InputStream is) throws IOException {
        return new CustomHessian2ObjectInput(is);
    }

}