package net.otlg.studentcheckin;

import io.netty.channel.ChannelHandlerContext;
import net.otlg.apiserver.net.wrapper.HttpRequestWrapper;
import net.otlg.apiserver.net.ResultContainer;

import java.sql.SQLException;

public interface RequestHandler {
    void handle(String addr, ChannelHandlerContext ctx, HttpRequestWrapper request, ResultContainer result) throws SQLException;
}
