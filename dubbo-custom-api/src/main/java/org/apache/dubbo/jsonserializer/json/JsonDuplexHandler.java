package org.apache.dubbo.jsonserializer.json;

import org.apache.dubbo.common.serialize.CustomHessian2Input;
import org.apache.dubbo.common.serialize.CustomHessian2ObjectInput;
import org.apache.dubbo.jsonserializer.metadata.ServiceMetadataRepository;
import org.apache.dubbo.jsonserializer.metadata.OptimizedMetadata;
import org.apache.dubbo.jsonserializer.metadata.tag.Method;
import org.apache.dubbo.common.serialize.ObjectInput;
import org.apache.dubbo.common.serialize.ObjectOutput;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;


/**
 * Json encode 与 decode
 *
 * @author Denim.leihz 2019-07-08 10:10 PM
 */
public class JsonDuplexHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonDuplexHandler.class);

    private static ServiceMetadataRepository repository = ServiceMetadataRepository.getRepository();

    public static void writeObject(String service, String version, String method, Object object, ObjectOutput out) throws IOException {
        OptimizedMetadata.OptimizedService bizService = getServiceMetadata(service, version);
        writeObject(method, object, bizService, out);
    }


    public static String readObject(String service, String methodName, ObjectInput in) {
        OptimizedMetadata.OptimizedService bizService;
        try {
            //todo version 版本
            bizService = getServiceMetadata(service, "1.0.0");
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


    private static OptimizedMetadata.OptimizedService getServiceMetadata(String service, String version) throws IOException {
        OptimizedMetadata.OptimizedService bizService = repository.getService(service, version);
        if (bizService == null) {
            throw new IOException(String.format("Specific service %s's metadata info does not found", service));
        }
        return bizService;
    }

    public static Object readMetadata(ObjectInput in) {
        CustomHessian2ObjectInput cmh2 = (CustomHessian2ObjectInput) in;
        CustomHessian2Input cmH2i = cmh2.getCmH2i();
        try {
            return cmH2i.readObject((List<Class<?>>) null);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RpcException("Got occurred when custom readMetadata.");
        }
    }
}
