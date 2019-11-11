package com.yunji.flurry.jsonserializer;

import com.yunji.dubbo.common.serialize.compatible.Hessian3Input;
import com.yunji.dubbo.common.serialize.compatible.Hessian3ObjectInput;
import com.yunji.flurry.metadata.OptimizedService;
import com.yunji.flurry.metadata.OptimizedStruct;
import com.yunji.flurry.metadata.core.ExportServiceManager;
import com.yunji.flurry.metadata.tag.Method;
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

    private static ExportServiceManager serviceManager = ExportServiceManager.getInstance();

    public static void writeObject(String service, String version, String method, Object object, ObjectOutput out) throws IOException {
        com.yunji.flurry.metadata.OptimizedService bizService = getServiceMetadata(service, version);
        writeObject(method, version, object, bizService, out);
    }


    public static String readObject(String service, String methodName, ObjectInput in) {
        com.yunji.flurry.metadata.OptimizedService bizService;
        try {
            //todo version 版本
//            bizService = getServiceMetadata(service, null);
//            return readObject(bizService, methodName, in);
            return readObject((com.yunji.flurry.metadata.OptimizedService) null, methodName, in);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RpcException("Got occurred when custom readObject.");
        }
    }


    /**
     * 利用 hessian2 writeObject object
     */
    private static void writeObject(String methodName, String version, Object object, com.yunji.flurry.metadata.OptimizedService optimizedService, ObjectOutput out) throws IOException {
        Method method = optimizedService.getMethodMap().get(methodName);
        if (method == null) {
            throw new IOException(String.format("Specific method %s's metadata info does not found", methodName));
        }

        OptimizedStruct req = optimizedService.getOptimizedStructs().get(method.request.namespace + "." + method.request.name);
        JsonSerializer jsonEncoder = new JsonSerializer(optimizedService, req);

        jsonEncoder.write((String) object, out);
    }

    private static String readObject(com.yunji.flurry.metadata.OptimizedService bizService, String methodName, ObjectInput in) throws IOException {
//        Method method = bizService.getMethodMap().get(methodName);
//        OptimizedStruct resp = bizService.getOptimizedStructs().get(method.response.namespace + "." + method.response.name);
        JsonSerializer jsonDecoder = new JsonSerializer(null, null);
        return jsonDecoder.read(in);
    }


    private static com.yunji.flurry.metadata.OptimizedService getServiceMetadata(String service, String version) throws IOException {
        OptimizedService bizService = serviceManager.getMetadata(service);
        if (bizService == null) {
            throw new IOException(String.format("Specific service %s's metadata info does not found", service));
        }
        return bizService;
    }

    public static Object readMetadata(ObjectInput in) {
//        HighlyHessian2ObjectInput cmh2 = (HighlyHessian2ObjectInput) in;
        Hessian3ObjectInput cmh2 = (Hessian3ObjectInput) in;
        Hessian3Input cmH2i = cmh2.getCmH3i();
        try {
            return cmH2i.readObject((List<Class<?>>) null);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RpcException("Got occurred when custom readMetadata.");
        }
    }
}
