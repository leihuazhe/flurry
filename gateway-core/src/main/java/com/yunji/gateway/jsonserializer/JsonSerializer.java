package com.yunji.gateway.jsonserializer;

import com.yunji.dubbo.common.serialize.compatible.Hessian3Input;
import com.yunji.dubbo.common.serialize.compatible.Hessian3ObjectInput;
import com.yunji.dubbo.common.serialize.streaming.Hessian2StreamingObjectOutput;
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
//        read((HighlyHessian2ObjectInput) in, writer);
        read((Hessian3ObjectInput) in, writer);
        return writer.toString();
    }


    /**
     * json -> hessian2 buffer
     */
    @Override
    public void write(String input, ObjectOutput oproto) throws IOException {
        JsonReader jsonReader;
        jsonReader = new JsonReader(optimizedStruct, optimizedService, (Hessian2StreamingObjectOutput) oproto);
        new JsonParser(input, jsonReader).parseJsValue();
    }

    @Override
    public void validate(String input) throws IOException {

    }

    @Override
    public String toString(String input) {
        return input;
    }


    private void read(Hessian3ObjectInput output, JsonCallback writer) throws IOException {
        Hessian3Input cmH2i = output.getCmH3i();
        cmH2i.setJsonCallback(writer);
        cmH2i.readObject();
    }

}
