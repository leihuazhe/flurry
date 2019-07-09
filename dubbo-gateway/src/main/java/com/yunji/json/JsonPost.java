package com.yunji.json;

import com.yunji.metadata.ServiceCache;
import com.yunji.metadata.tag.Method;
import org.apache.dubbo.common.serialize.ObjectOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Denim.leihz 2019-07-08 10:10 PM
 */
public class JsonPost {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonPost.class);
    private static final String service = "com.yunji.demo.api.HelloService";

    static {
        ServiceCache.loadServicesMetadata(service);
    }

    public static void post(String methodName, Object[] argsArray, ObjectOutput out) {

        try {
            OptimizedMetadata.OptimizedService bizService = ServiceCache.getService(service, "1.0.0");

            if (bizService == null) {
                throw new JException("Service " + service + " metadata not found .");

            }
            encode(methodName, argsArray, bizService, out);
        } catch (JException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 利用 hessian2 encode object
     */
    public static void encode(String methodName, Object[] paramsArray, OptimizedMetadata.OptimizedService optimizedService, ObjectOutput out) throws Exception {
        Method method = optimizedService.getMethodMap().get(methodName);

        if (method == null) {
            throw new JException("method:" + methodName + " for service: not found");
        }

        OptimizedMetadata.OptimizedStruct req = optimizedService.getOptimizedStructs().get(method.request.namespace + "." + method.request.name);
//        OptimizedMetadata.OptimizedStruct resp = optimizedService.getOptimizedStructs().get(method.response.namespace + "." + method.response.name);

        JsonSerializer jsonEncoder = new JsonSerializer(optimizedService, method, "1.0.0", req);

        jsonEncoder.write((String) paramsArray[0], out);
    }


}
