package net.otlg.apiserver.net.wrapper;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestWrapper {

    private FullHttpRequest fullHttpRequest;
    private String content;
    private Map<String, Cookie> cookieMap = new HashMap<>();
    private QueryStringDecoder queryStringDecoder;
    private HttpFormPostDecoder httpFormPostDecoder;

    public HttpRequestWrapper(FullHttpRequest request) {
        fullHttpRequest = request;
        if (request.headers().get(HttpHeaderNames.COOKIE) != null) {
            for (Cookie cookie : ServerCookieDecoder.LAX.decode(request.headers().get(HttpHeaderNames.COOKIE))) {
                cookieMap.put(cookie.name(), cookie);
            }
        }

        content = request.content().toString(Charset.forName("UTF-8"));
        queryStringDecoder = new QueryStringDecoder(request.uri());
    }

    public HttpFormPostDecoder getPost() {
        if (httpFormPostDecoder == null) {
            httpFormPostDecoder = new HttpFormPostDecoder(fullHttpRequest);
        }

        return httpFormPostDecoder;
    }

    public String getContent() {
        return content;
    }

    public Map<String, Cookie> getCookieMap() {
        return cookieMap;
    }

    public Cookie getCookie(String key) {
        return cookieMap.get(key);
    }

    public QueryStringDecoder getQueryStringDecoder() {
        return queryStringDecoder;
    }

    public FullHttpRequest getFullHttpRequest() {
        return fullHttpRequest;
    }

    public HttpHeaders headers() {
        return fullHttpRequest.headers();
    }

    public Map<String, List<String>> getGetMap() {
        return queryStringDecoder.parameters();
    }

    public HttpMethod getMethod() {
        return fullHttpRequest.method();
    }

    public List<String> getGet(String key) {
        return getGetMap().get(key);
    }

    public String getGetString(String key) {
        List<String> getMap = getGetMap().get(key);

        if (getMap == null) {
            return null;
        } else if (getMap.size() == 0) {
            return null;
        } else if (getMap.size() == 1) {
            return getMap.get(0);
        } else {
            return getMap.toString();
        }
    }
}