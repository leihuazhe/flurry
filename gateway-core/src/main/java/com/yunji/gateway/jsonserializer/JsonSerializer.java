package com.yunji.gateway.jsonserializer;

import com.yunji.gateway.metadata.OptimizedService;
import com.yunji.gateway.metadata.OptimizedStruct;
import org.apache.dubbo.common.serialize.*;

import java.io.IOException;


public class JsonSerializer implements BeanSerializer<String> {

    private final OptimizedStruct optimizedStruct;
    private final OptimizedService optimizedService;


    JsonSerializer(OptimizedService optimizedService,
                   OptimizedStruct optimizedStruct) {

        this.optimizedStruct = optimizedStruct;
        this.optimizedService = optimizedService;
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
        jsonReader = new JsonReader(optimizedStruct, optimizedService, (CustomHessian2ObjectOutput) oproto);

        new JsonParser(input, jsonReader).parseJsValue();
    }

    @Override
    public void validate(String input) throws IOException {

    }

    @Override
    public String toString(String input) {
        return input;
    }


    private void read(CustomHessian2ObjectInput output, JsonCallback writer) throws IOException {
        CustomHessian2Input cmH2i = output.getCmH2i();
        cmH2i.setJsonCallback(writer);
        cmH2i.readObject();
    }

}
