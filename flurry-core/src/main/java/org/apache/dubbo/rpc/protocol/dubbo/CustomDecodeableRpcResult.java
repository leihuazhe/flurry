package org.apache.dubbo.rpc.protocol.dubbo;

import com.yunji.dubbo.common.serialize.util.CodecContext;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.serialize.Cleanable;
import org.apache.dubbo.common.serialize.ObjectInput;
import org.apache.dubbo.common.utils.ArrayUtils;
import org.apache.dubbo.common.utils.Assert;
import org.apache.dubbo.common.utils.StringUtils;
import com.yunji.flurry.jsonserializer.JsonDuplexHandler;
import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.Codec;
import org.apache.dubbo.remoting.Decodeable;
import org.apache.dubbo.remoting.exchange.Response;
import org.apache.dubbo.remoting.transport.CodecSupport;
import org.apache.dubbo.rpc.AppResponse;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.support.RpcUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Map;

import static com.yunji.flurry.util.GateConstants.INTERFACE;
import static com.yunji.flurry.util.GateConstants.METADATA_METHOD_NAME;


public class CustomDecodeableRpcResult extends AppResponse implements Codec, Decodeable {
    private static final Logger log = LoggerFactory.getLogger(CustomDecodeableRpcResult.class);

    private Channel channel;

    private byte serializationType;

    private InputStream inputStream;

    private Response response;

    private Invocation invocation;

    private volatile boolean hasDecoded;

    public CustomDecodeableRpcResult(Channel channel, Response response, InputStream is, Invocation invocation, byte id) {
        Assert.notNull(channel, "channel == null");
        Assert.notNull(response, "response == null");
        Assert.notNull(is, "inputStream == null");
        this.channel = channel;
        this.response = response;
        this.inputStream = is;
        this.invocation = invocation;
        this.serializationType = id;
    }

    @Override
    public void encode(Channel channel, OutputStream output, Object message) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object decode(Channel channel, InputStream input) throws IOException {
        ObjectInput in = CodecSupport.getSerialization(channel.getUrl(), serializationType)
                .deserialize(channel.getUrl(), input);

        try {
            byte flag = in.readByte();
            switch (flag) {
                case DubboCodec.RESPONSE_NULL_VALUE:
                    break;
                case DubboCodec.RESPONSE_VALUE:
                    CodecContext.getContext().setUseJsonDecoder(true);
                    handleJsonValue(in);
                    break;
                case DubboCodec.RESPONSE_WITH_EXCEPTION:
                    handleException(in);
                    break;
                case DubboCodec.RESPONSE_NULL_VALUE_WITH_ATTACHMENTS:
                    handleAttachment(in);
                    break;
                case DubboCodec.RESPONSE_VALUE_WITH_ATTACHMENTS:
                    CodecContext.getContext().setUseJsonDecoder(true);
                    handleJsonValue(in);
                    CodecContext.getContext().setUseJsonDecoder(false);
                    handleAttachment(in);
                    break;
                case DubboCodec.RESPONSE_WITH_EXCEPTION_WITH_ATTACHMENTS:
                    handleException(in);
                    handleAttachment(in);
                    break;
                default:
                    throw new IOException("Unknown result flag, expect '0' '1' '2' '3' '4' '5', but received: " + flag);
            }
            if (in instanceof Cleanable) {
                ((Cleanable) in).cleanup();
            }
            return this;
        } finally {
            CodecContext.removeContext();
        }
    }

    @Override
    public void decode() throws Exception {
        if (!hasDecoded && channel != null && inputStream != null) {
            try {
                decode(channel, inputStream);
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn("Decode rpc result failed: " + e.getMessage(), e);
                }
                response.setStatus(Response.CLIENT_ERROR);
                response.setErrorMessage(StringUtils.toString(e));
            } finally {
                hasDecoded = true;
            }
        }
    }

    /**
     * 流式序列化 handle json value.
     */
    private void handleJsonValue(ObjectInput in) {
        if (invocation != null && invocation.getInvoker() != null && invocation.getInvoker().getUrl() != null) {
            String service = invocation.getAttachment(INTERFACE);
            String methodName = invocation.getMethodName();

            Object value;
            if (METADATA_METHOD_NAME.equals(methodName)) {
                value = JsonDuplexHandler.readMetadata(in);
            } else {
                value = JsonDuplexHandler.readObject(service, methodName, in);
            }
            setValue(value);
        }
    }

    private void handleValue(ObjectInput in) throws IOException {
        try {
            Type[] returnTypes = RpcUtils.getReturnTypes(invocation);
            Object value;
            if (ArrayUtils.isEmpty(returnTypes)) {
                value = in.readObject();
            } else if (returnTypes.length == 1) {
                value = in.readObject((Class<?>) returnTypes[0]);
            } else {
                value = in.readObject((Class<?>) returnTypes[0], returnTypes[1]);
            }
            setValue(value);
        } catch (ClassNotFoundException e) {
            rethrow(e);
        }
    }

    private void handleException(ObjectInput in) throws IOException {
        try {
            Object obj = in.readObject();
            if (!(obj instanceof Throwable)) {
                throw new IOException("Response data error, expect Throwable, but get " + obj);
            }
            setException((Throwable) obj);
        } catch (ClassNotFoundException e) {
            rethrow(e);
        }
    }

    private void handleAttachment(ObjectInput in) throws IOException {
        try {
            setAttachments((Map<String, String>) in.readObject(Map.class));
        } catch (ClassNotFoundException e) {
            rethrow(e);
        }
    }

    private void rethrow(Exception e) throws IOException {
        throw new IOException(StringUtils.toString("Read response data failed.", e));
    }
}
