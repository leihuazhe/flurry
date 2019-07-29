package org.apache.dubbo.jsonserializer.json;

import org.apache.dubbo.metadata.OptimizedMetadata;
import org.apache.dubbo.metadata.tag.Method;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.apache.dubbo.common.serialize.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class JsonSerializer implements BeanSerializer<String> {

    private final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);

    private final static long MAX_JSON_LONG = 1L << 53;
    private final static long MIN_JSON_LONG = -1L << 53;

    private final OptimizedMetadata.OptimizedStruct optimizedStruct;
    private final OptimizedMetadata.OptimizedService optimizedService;
    private final Method method;
    private final String version;
//    private ByteBuf requestByteBuf = ByteBufAllocator.DEFAULT.buffer();

    public JsonSerializer(OptimizedMetadata.OptimizedService optimizedService,
                          Method method, String version,
                          OptimizedMetadata.OptimizedStruct optimizedStruct) {
        this.optimizedStruct = optimizedStruct;
        this.optimizedService = optimizedService;
        this.method = method;
        this.version = version;
    }

    /**
     * hessian2 buffer -> json
     */
    @Override
    public String read(ObjectInput in) throws IOException {
        JsonWriter writer = new JsonWriter();
        read((CustomHessian2ObjectInput) in, writer);
        return writer.toString();
    }


    /**
     * json -> hessian2 buffer
     */
    @Override
    public void write(String input, ObjectOutput oproto) throws IOException {
        JsonReader jsonReader;
//        try {
        jsonReader = new JsonReader(optimizedStruct, optimizedService, (CustomHessian2ObjectOutput) oproto);
        new JsonParser(input, jsonReader).parseJsValue();
//        } catch (IOException e) {
//            if (jsonReader.current != null) {
//                String errorMsg = "Please check field:" + jsonReader.current.getFieldName();
//                logger.error(errorMsg + "\n" + e.getMessage());
//                throw new IOException(e);
//            }
//            throw e;
//        }
    }

    @Override
    public void validate(String s) throws IOException {

    }

    @Override
    public String toString(String s) {
        return s;
    }


    private void read(CustomHessian2ObjectInput output, JsonCallback writer) throws IOException {
        CustomHessian2Input cmH2i = output.getCmH2i();
        cmH2i.setJsonCallback(writer);
        cmH2i.readObject();
    }

}
