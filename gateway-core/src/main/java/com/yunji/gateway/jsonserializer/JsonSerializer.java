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
        read((HighlyHessian2ObjectInput) in, writer);
        return writer.toString();
    }


    /**
     * json -> hessian2 buffer
     */
    @Override
    public void write(String input, ObjectOutput oproto) throws IOException {
        JsonReader jsonReader;
        HighlyHessian2ObjectOutput hessian2ObjectOutput = (HighlyHessian2ObjectOutput) oproto;
        HighlyHessian2Output cmH2o = hessian2ObjectOutput.getCmH2o();
        int bufferWriteIndex = cmH2o.getByteBufWriteIndex();
        jsonReader = new JsonReader(optimizedStruct, optimizedService, (HighlyHessian2ObjectOutput) oproto, bufferWriteIndex);

        new JsonParser(input, jsonReader).parseJsValue();
    }

    @Override
    public void validate(String input) throws IOException {

    }

    @Override
    public String toString(String input) {
        return input;
    }


    private void read(HighlyHessian2ObjectInput output, JsonCallback writer) throws IOException {
        HighlyHessian2Input cmH2i = output.getCmH2i();
        cmH2i.setJsonCallback(writer);
        cmH2i.readObject();
    }

}
