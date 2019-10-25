package net.otlg.studentcheckin.utils;

import io.netty.handler.codec.http.cookie.DefaultCookie;
import net.otlg.apiserver.net.wrapper.HttpRequestWrapper;
import net.otlg.apiserver.net.ResultContainer;
import net.otlg.studentcheckin.UserData;

import java.util.Map;

public class UserUtils {

    public static final String SESSION_ID_COOKIE_NAME = "sessid";

    public static UserData getSession(HttpRequestWrapper request, Map<String, UserData> userMap) {
        if (request.getCookie(SESSION_ID_COOKIE_NAME) == null) return null;

        String sessid = request.getCookie(SESSION_ID_COOKIE_NAME).value();
        if (sessid == null) return null;

        if (userMap.containsKey(sessid)) {
            return userMap.get(sessid);
        }
        return null;
    }

    public static void setSession(ResultContainer result, String sessid) {
        result.setCookie(new DefaultCookie(SESSION_ID_COOKIE_NAME, sessid));
    }
}
