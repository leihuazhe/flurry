package com.yunji.json;

import com.yunji.gateway.service.ReferenceServiceContext;
import com.yunji.json.serializer.JsonSerializer;
import com.yunji.json.util.JException;
import com.yunji.metadata.MetadataFetcher;
import com.yunji.metadata.OptimizedMetadata;
import com.yunji.metadata.tag.Method;
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

    public static void writeObject(String service, String method, Object object, ObjectOutput out) throws Exception {
        OptimizedMetadata.OptimizedService bizService = getServiceMetadata(service);
        writeObject(method, object, bizService, out);
    }


    public static String readObject(String service, String methodName, ObjectInput in) {
        OptimizedMetadata.OptimizedService bizService;
        try {
            bizService = getServiceMetadata(service);
            return readObject(bizService, methodName, in);
        } catch (JException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RpcException("Got occurred when custom readObject.");
        }
    }


    /**
     * 利用 hessian2 writeObject object
     */
    private static void writeObject(String methodName, Object object, OptimizedMetadata.OptimizedService optimizedService, ObjectOutput out) throws Exception {
        Method method = optimizedService.getMethodMap().get(methodName);

        if (method == null) {
            throw new JException("method:" + methodName + " for service: not found");
        }

        OptimizedMetadata.OptimizedStruct req = optimizedService.getOptimizedStructs().get(method.request.namespace + "." + method.request.name);
//        OptimizedMetadata.OptimizedStruct resp = optimizedService.getOptimizedStructs().get(method.response.namespace + "." + method.response.name);

        JsonSerializer jsonEncoder = new JsonSerializer(optimizedService, method, "1.0.0", req);

        jsonEncoder.write((String) object, out);
    }

    private static String readObject(OptimizedMetadata.OptimizedService bizService, String methodName, ObjectInput in) throws IOException {
        Method method = bizService.getMethodMap().get(methodName);

        OptimizedMetadata.OptimizedStruct resp = bizService.getOptimizedStructs().get(method.response.namespace + "." + method.response.name);

        JsonSerializer jsonDecoder = new JsonSerializer(bizService, method, "1.0.0", resp);

        return jsonDecoder.read(in);
    }


    private static OptimizedMetadata.OptimizedService getServiceMetadata(String service) throws JException {
        OptimizedMetadata.OptimizedService bizService = MetadataFetcher.getService(ReferenceServiceContext.HELLO_SERVICE, "1.0.0");
        if (bizService == null) {
            throw new JException("Service " + ReferenceServiceContext.HELLO_SERVICE + " metadata not found .");
        }
        return bizService;
    }

}
