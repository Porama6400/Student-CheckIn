package net.otlg.apiserver.net;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import net.otlg.apiserver.APIServer;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final APIServer server;

    public ServerChannelInitializer(APIServer server) {
        this.server = server;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        if (server.getConfig().isSslEnabled()) {
            p.addLast(server.getSslContext().newHandler(p.channel().alloc()));
        }

        p.addLast(new HttpServerCodec());
        p.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
        p.addLast(new ServerInboundHandler(server));
    }
}
