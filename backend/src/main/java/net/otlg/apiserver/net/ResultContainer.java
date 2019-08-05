package net.otlg.apiserver.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.DefaultCookie;

import java.util.ArrayList;
import java.util.List;

public class ResultContainer {
    private boolean drop = false;
    private Object data = null;
    private HttpResponseStatus responseStatus = HttpResponseStatus.OK;
    private List<DefaultCookie> cookies = new ArrayList<>();

    public List<DefaultCookie> getCookies() {
        return cookies;
    }

    public void setCookie(DefaultCookie cookie) {
        cookies.add(cookie);
    }

    public void set(String string) {
        data = string;
    }

    public void set(byte[] data) {
        this.data = data;
    }

    public void append(String string) {
        if (data == null) {
            data = "";
        }

        if (data instanceof String) {
            data += string;
        }
    }

    public Class getType() {
        return data.getClass();
    }

    public String getString() {
        if (data instanceof String) {
            return (String) data;
        }

        return null;
    }

    public byte[] getByte() {
        if (data instanceof byte[]) {
            return (byte[]) data;
        } else if (data instanceof String) {
            return data.toString().getBytes();
        }

        if (cookies.size() > 0) return new byte[0];

        return null;
    }

    public ByteBuf getByteBuffer() {
        if (getByte() == null) return null;
        return Unpooled.copiedBuffer(getByte());
    }

    public boolean isDropped() {
        return drop;
    }

    public void drop() {
        this.drop = true;
    }

    public HttpResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(HttpResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
        if (data == null) data = "";
    }
}
