package net.otlg.apiserver.net.wrapper;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MixedAttribute;

import java.io.IOException;

public class HttpFormPostDecoder {
    private final FullHttpRequest httpRequest;
    HttpPostRequestDecoder decoder;

    public HttpFormPostDecoder(FullHttpRequest httpRequest) {
        this.httpRequest = httpRequest;
        decoder = new HttpPostRequestDecoder(httpRequest);
    }

    public String get(String attributeName) {
        InterfaceHttpData data = decoder.getBodyHttpData(attributeName);
        if (data instanceof MixedAttribute) {
            MixedAttribute mixedAttribute = (MixedAttribute) data;

            try {
                return mixedAttribute.getValue();
            } catch (IOException e) {
                return null;
            }

        }
        return null;
    }

    public HttpPostRequestDecoder getDecoder() {
        return decoder;
    }

    public FullHttpRequest getHttpRequest() {
        return httpRequest;
    }
}
