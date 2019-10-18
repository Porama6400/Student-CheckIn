package net.otlg.apiserver.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import net.otlg.apiserver.APIServer;
import net.otlg.apiserver.net.wrapper.HttpRequestWrapper;

import java.util.List;

public class ServerInboundHandler extends ChannelInboundHandlerAdapter {

    private final APIServer server;

    public ServerInboundHandler(APIServer server) {
        this.server = server;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            final HttpRequestWrapper request = new HttpRequestWrapper((FullHttpRequest) msg);
            String addr = request.headers().get("X-FORWARDED-FOR");
            if (addr == null) {
                addr = ctx.channel().remoteAddress().toString();
            }

            ResultContainer result = new ResultContainer();

            for (RequestHandler handler : server.handlers()) {
                try {
                    handler.handle(addr, ctx, request, result);
                } catch (Throwable e) {
                    System.err.println("Error while handling client " + ctx.channel().remoteAddress().toString());
                    //System.err.println(">> " + e.getMessage());
                    e.printStackTrace();
                }
            }

            if (result.isDropped()) {
                return;
            }

            try {

                ByteBuf messageBuf = result.getByteBuffer();
                if (messageBuf == null) return;
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        result.getResponseStatus(),
                        messageBuf
                );

                List<DefaultCookie> cookieArray = result.getCookies();

                response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, true);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, (result.getType() == String.class ? HttpHeaderValues.TEXT_PLAIN : HttpHeaderValues.BINARY));
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, result.getByte().length);

                String serverHeader = server.getInternalConfig().getServerHeader();
                if (serverHeader != null && !serverHeader.isEmpty())
                    response.headers().set(HttpHeaderNames.SERVER, serverHeader);

                if (!server.getConfig().getAllowedHeader().isEmpty()) {
                    response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, server.getConfig().getAllowedHeader());
                }

                if (cookieArray.size() > 0) {
                    response.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookieArray));
                }

                ctx.write(response);
            } finally {
                ctx.flush();
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
