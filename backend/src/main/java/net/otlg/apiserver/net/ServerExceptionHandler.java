package net.otlg.apiserver.net;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.otlg.apiserver.APIServer;

import java.io.IOException;

public class ServerExceptionHandler extends ChannelDuplexHandler {

    private final APIServer server;

    public ServerExceptionHandler(APIServer APIServer) {
        this.server = APIServer;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("Error on " + ctx.channel().remoteAddress());
        cause.printStackTrace();
        ctx.channel().close();
        ctx.close();
    }
}
