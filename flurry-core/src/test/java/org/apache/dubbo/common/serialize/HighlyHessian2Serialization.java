package org.apache.dubbo.common.serialize;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.serialize.hessian2.Hessian2ObjectInput;
import org.apache.dubbo.common.serialize.hessian2.Hessian2ObjectOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.yunji.gateway.util.GateConstants.SERIALIZATION_CUSTOM;
import static org.apache.dubbo.common.serialize.Constants.HESSIAN2_SERIALIZATION_ID;
import static org.apache.dubbo.remoting.Constants.SERIALIZATION_KEY;

/**
 * @author Denim.leihz 2019-07-10 6:20 PM
 */
public class HighlyHessian2Serialization implements Serialization {

    @Override
    public byte getContentTypeId() {
        return HESSIAN2_SERIALIZATION_ID;
    }

    @Override
    public String getContentType() {
        return "x-application/hessian2";
    }

    @Override
    public ObjectOutput serialize(URL url, OutputStream out) throws IOException {
        if (SERIALIZATION_CUSTOM.equals(url.getParameter(SERIALIZATION_KEY))) {
            return new HighlyHessian2ObjectOutput(out);
//            return new Hessian3ObjectOutput(out);
        }
        return new Hessian2ObjectOutput(out);
    }

    @Override
    public ObjectInput deserialize(URL url, InputStream is) throws IOException {
        if (SERIALIZATION_CUSTOM.equals(url.getParameter(SERIALIZATION_KEY))) {
            return new HighlyHessian2ObjectInput(is);
        }
        return new Hessian2ObjectInput(is);
    }

}