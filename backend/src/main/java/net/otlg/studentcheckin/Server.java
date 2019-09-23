package net.otlg.studentcheckin;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.otlg.apiserver.APIServer;
import net.otlg.apiserver.config.ConfigLoader;
import net.otlg.apiserver.config.ServerConfigFile;
import net.otlg.apiserver.net.ResultContainer;
import net.otlg.apiserver.net.wrapper.HttpRequestWrapper;
import net.otlg.studentcheckin.etc.LogEntryWrapper;
import net.otlg.studentcheckin.etc.UserEntryWrapper;
import net.otlg.studentcheckin.utils.SQLCommand;
import net.otlg.studentcheckin.utils.UserUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Server {

    public final File configFile = new File("./config.json");
    private final String VERSION = "1.0";
    private HashMap<String, SessionData> sessionMap = new HashMap<>();

    private long lastUpdate = 0;

    public static void main(String args[]) throws IOException {
        new Server().run();
    }

    public void resetLastUpdate() {
        lastUpdate = System.currentTimeMillis();
    }

    public HashMap<String, SessionData> getSessionMap() {
        return sessionMap;
    }

    public void run() throws IOException {
        ServerConfigFile config = (ServerConfigFile) ConfigLoader.loadOrSaveDefault(configFile, new ServerConfigFile());
        APIServer server = new APIServer(config);
        server.getInternalConfig().setServerHeader("Student Check-In Server/" + VERSION);
        server.run();

        server.handlers().add(new RequestHandler() {

            @Override
            public void handle(String addr, ChannelHandlerContext ctx, HttpRequestWrapper request, ResultContainer result) throws SQLException {
                SessionData session = UserUtils.getSession(request, sessionMap);

                String action = request.getGetString("action");
                String path = request.getQueryStringDecoder().path();

                if (action == null) {
                    result.setResponseStatus(HttpResponseStatus.BAD_REQUEST);
                    return;
                } else action = action.toLowerCase();

                if (path.equals("/checkin")) {
                    handleCheckIn(action, addr, request, result);
                    return;
                }

                if (path.equals("/auth")) {
                    handleAuth(session, action, addr, request, result);
                    return;
                }
                if (session == null) {
                    result.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
                    return;
                }
                session.update(false);

                if (path.equals("/admin")) {
                    if (action.equals("list")) {
                        if (session.checkPerm("admin", result)) {
                            List<LogEntryWrapper> data = SQLCommand.getDataList(request.getPost(), server.getDatabase(), true);
                            result.set(ConfigLoader.GSON.toJson(data));
                        }
                        return;
                    }
                    if (action.equals("listadmin")) {
                        if (session.checkPerm("userman", result)) {
                            List<UserEntryWrapper> data = SQLCommand.getAdmins(server.getDatabase(), true);
                            result.set(ConfigLoader.GSON.toJson(data));
                        }
                        return;
                    } else if (action.equals("delete")) {
                        if (session.checkPerm("delete", result)) {
                            int id = Integer.parseInt(request.getPost().get("id"));
                            SQLCommand.deleteLog(id, server.getDatabase(), true);
                            resetLastUpdate();
                            result.set("OK");
                            return;
                        }
                        return;
                    } else if (action.equals("poll")) {
                        if (session.checkPerm("admin", result)) {
                            result.set(String.valueOf(lastUpdate));
                        }
                        return;
                    }
                }

                result.setResponseStatus(HttpResponseStatus.NOT_FOUND);
            }

            public void handleCheckIn(String action, String addr, HttpRequestWrapper request, ResultContainer result) throws SQLException {
                if (action.equals("log")) {
                    String user = request.getPost().get("username");
                    String pass = request.getPost().get("password");

                    try (Connection connection = server.getDatabase()) {
                        if (SQLCommand.checkPassword(user, pass, connection, false)) {
                            SQLCommand.insertLog(user, connection, false);
                            resetLastUpdate();
                            result.set("LOG/OK");
                            System.out.println(user + " checked in from " + addr);
                        } else {
                            result.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
                        }
                    }
                }
            }

            public void handleAuth(SessionData session, String action, String addr, HttpRequestWrapper
                    request, ResultContainer result) throws SQLException {
                if (action.equals("login")) {
                    if (session == null) {
                        String user = request.getPost().get("username");
                        String pass = request.getPost().get("password");
                        if (SQLCommand.checkPassword(user, pass, server.getDatabase(), true)) {
                            String uuid = UUID.randomUUID().toString();
                            sessionMap.put(uuid, new SessionData(server, uuid, user, SQLCommand.getUserId(user, server.getDatabase(), true)));
                            UserUtils.setSession(result, uuid);
                            System.out.println(user + " logged in from " + addr);
                            result.set("AUTH/SUCCESS");
                        } else {
                            result.set("AUTH/FAILED");
                            result.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
                        }
                    } else {
                        result.set("AUTH/AUTHENTICATED");
                    }
                } else if (action.equals("logout")) {
                    if (session == null) {
                        result.set("AUTH/NO_SESSION");
                    } else {
                        sessionMap.remove(session.getSessionID());
                        result.set("AUTH/SUCCESS");
                    }
                } else if (action.equalsIgnoreCase("check")) {
                    if (session == null) {
                        result.set("AUTH/NO_SESSION");
                    } else {
                        result.set("AUTH/OK");
                    }
                } else if (action.equalsIgnoreCase("checkperm")) {
                    if (session == null) {
                        result.set("AUTH/NO_SESSION");
                    } else {
                        session.update(false);
                        if (session.checkPerm(request.getPost().get("node")))
                            result.set("AUTH/OK");
                        else {
                            session.update(false);
                            result.set("AUTH/PERM_ERR");
                            result.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
                        }
                    }

                }
            }

        });

        server.sync();
    }
}
