package com.yunji.flurry.json;

import com.google.common.base.Joiner;
import com.yunji.flurry.jsonserializer.JsonDuplexHandler;
import com.yunji.flurry.metadata.OptimizedService;
import com.yunji.flurry.metadata.core.ExportServiceManager;
import com.yunji.flurry.metadata.tag.Service;
import io.netty.buffer.ByteBufAllocator;
import org.apache.commons.io.IOUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.serialize.ObjectOutput;
import org.apache.dubbo.common.serialize.Serialization;
import org.apache.dubbo.common.utils.ClassUtils;
import org.apache.dubbo.remoting.buffer.ChannelBuffer;
import org.apache.dubbo.remoting.buffer.ChannelBufferOutputStream;
import org.apache.dubbo.remoting.transport.netty4.NettyBackedChannelBuffer;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentMap;

import static com.yunji.flurry.util.GateConstants.SERIALIZATION_CUSTOM;
import static org.apache.dubbo.remoting.Constants.SERIALIZATION_KEY;

/**
 * -Dconfig_env=local -Ddiamond_server_host=tdiamond.yunjiweidian.com -Xbootclasspath/a:/Users/maple/yunji/erlang/transmittable-thread-local-2.1.1.jar -javaagent:/Users/maple/yunji/erlang/transmittable-thread-local-2.1.1.jar -javaagent:/Users/maple/yunji/erlang/erlang-trace-agent-1.0.0/erlang-trace-agent-1.0.0.jar
 *
 * @author Denim.leihz 2019-09-09 7:16 PM
 */
public class JsonReaderTest {
    private static String interfaceName = "com.yunji.multi.MultiParameterService";

    private ObjectOutput objectOutput;

    @Before
    public void prepare() throws Exception {
        Serialization serialization = ExtensionLoader.getExtensionLoader(Serialization.class).getAdaptiveExtension();
//        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(1024);
        ChannelBuffer channelBuffer = new NettyBackedChannelBuffer(ByteBufAllocator.DEFAULT.buffer(1024 * 1024));

        OutputStream output = new ChannelBufferOutputStream(channelBuffer);
        URL url = new URL("dubbo", "10.3.17.212", 20880, "");
        URL useUrl = url.addParameter(SERIALIZATION_KEY, SERIALIZATION_CUSTOM);

        objectOutput = serialization.serialize(useUrl, output);

        //metadata
        String metaString = Joiner
                .on("\n")
                .join(IOUtils.readLines(
                        ClassUtils.getClassLoader()
                                .getResourceAsStream(interfaceName + ".xml")
                        )
                );
        OptimizedService optimizedService;
        try (StringReader reader = new StringReader(metaString)) {
            Service service = JAXB.unmarshal(reader, Service.class);
            optimizedService = new OptimizedService(service);
        }
        Field field = ExportServiceManager.class.getDeclaredField("serviceMetadataMap");
        field.setAccessible(true);
        ConcurrentMap<String, OptimizedService> serviceMao =
                (ConcurrentMap<String, OptimizedService>) field.get(ExportServiceManager.getInstance());

        serviceMao.put(interfaceName, optimizedService);
    }

    @Test
    public void test1() throws IOException {
        String json1 = "{\"args0\":[{\"applyId\":\"1023\",\"dataType\":\"OTC\",\"data\":\"2019-09-20\"},{\"applyId\":\"0317\",\"dataType\":\"MAY\",\"data\":\"2019-10-10\"}],\"args1\":\"15.6\"}";
        JsonDuplexHandler.writeObject(interfaceName, "1.0.0", "getList", json1, objectOutput);


    }

}
