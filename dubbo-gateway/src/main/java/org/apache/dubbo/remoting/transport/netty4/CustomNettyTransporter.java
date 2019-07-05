package org.apache.dubbo.remoting.transport.netty4;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.remoting.*;

public class CustomNettyTransporter implements Transporter {

    public static final String NAME = "customnetty";

    @Override
    public Server bind(URL url, ChannelHandler listener) throws RemotingException {
        return new NettyServer(url, listener);
    }

    @Override
    public Client connect(URL url, ChannelHandler listener) throws RemotingException {
        url = url.addParameter(Constants.CODEC_KEY, "extremejson");
        return new NettyClient(url, listener);
    }

}
