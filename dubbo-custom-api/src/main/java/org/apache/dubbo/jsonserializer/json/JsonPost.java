package org.apache.dubbo.jsonserializer.json;

import org.apache.dubbo.jsonserializer.metadata.MetadataFetcher;
import org.apache.dubbo.jsonserializer.metadata.OptimizedMetadata;
import org.apache.dubbo.jsonserializer.metadata.tag.Method;
import org.apache.dubbo.common.serialize.ObjectInput;
import org.apache.dubbo.common.serialize.ObjectOutput;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * @author Denim.leihz 2019-07-08 10:10 PM
 */
public class JsonPost {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonPost.class);

    public static void writeObject(String service, String method, Object object, ObjectOutput out) throws IOException {
        OptimizedMetadata.OptimizedService bizService = getServiceMetadata(service);
        writeObject(method, object, bizService, out);
    }


    public static String readObject(String service, String methodName, ObjectInput in) {
        OptimizedMetadata.OptimizedService bizService;
        try {
            bizService = getServiceMetadata(service);
            return readObject(bizService, methodName, in);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RpcException("Got occurred when custom readObject.");
        }
    }


    /**
     * 利用 hessian2 writeObject object
     */
    private static void writeObject(String methodName, Object object, OptimizedMetadata.OptimizedService optimizedService, ObjectOutput out) throws IOException {
        Method method = optimizedService.getMethodMap().get(methodName);
        if (method == null) {
            throw new IOException(String.format("Specific method %s's metadata info does not found", methodName));
        }

        OptimizedMetadata.OptimizedStruct req = optimizedService.getOptimizedStructs().get(method.request.namespace + "." + method.request.name);
        JsonSerializer jsonEncoder = new JsonSerializer(optimizedService, method, "1.0.0", req);

        jsonEncoder.write((String) object, out);
    }

    private static String readObject(OptimizedMetadata.OptimizedService bizService, String methodName, ObjectInput in) throws IOException {
        Method method = bizService.getMethodMap().get(methodName);

        OptimizedMetadata.OptimizedStruct resp = bizService.getOptimizedStructs().get(method.response.namespace + "." + method.response.name);

        JsonSerializer jsonDecoder = new JsonSerializer(bizService, method, "1.0.0", resp);

        return jsonDecoder.read(in);
    }


    private static OptimizedMetadata.OptimizedService getServiceMetadata(String service) throws IOException {
        OptimizedMetadata.OptimizedService bizService = MetadataFetcher.getService(service, "1.0.0");
        if (bizService == null) {
            throw new IOException(String.format("Specific service %s's metadata info does not found", service));
        }
        return bizService;
    }

}
