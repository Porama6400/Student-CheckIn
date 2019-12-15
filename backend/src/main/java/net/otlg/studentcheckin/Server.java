package net.otlg.studentcheckin;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.otlg.apiserver.APIServer;
import net.otlg.apiserver.config.ConfigLoader;
import net.otlg.apiserver.config.ServerConfigFile;
import net.otlg.apiserver.net.RequestHandler;
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
    private HashMap<String, UserData> sessionMap = new HashMap<>();

    private long lastUpdate = 0;

    public static void main(String args[]) throws IOException {
        new Server().run();
    }

    public void resetLastUpdate() {
        lastUpdate = System.currentTimeMillis();
    }

    public HashMap<String, UserData> getSessionMap() {
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
                UserData session = UserUtils.getSession(request, sessionMap);

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
                    switch (action) {
                        case "list":
                            if (session.hasPerm(Permissions.ADMIN_LOG_VIEW, result)) {
                                List<LogEntryWrapper> data = SQLCommand.getDataList(request.getPost(), server.getDatabase(), true);
                                result.set(ConfigLoader.GSON.toJson(data));
                            }
                            return;

                        case "userlist":
                            if (session.hasPerm(Permissions.ADMIN_ACCOUNT_VIEW, result)) {
                                List<UserEntryWrapper> data = SQLCommand.getUsers(server.getDatabase(), request.getPost().get("adminonly").equals("true"), true);
                                result.set(ConfigLoader.GSON.toJson(data));
                            }
                            return;

                        case "usercheckcanupdate":
                            if (session.hasPerm(Permissions.ADMIN_ACCOUNT_EDIT, result)) {
                                int id = Integer.parseInt(request.getPost().get("id"));
                                UserData target = new UserData(server, id);
                                if (!session.canEdit(target, result)) return;
                                result.set("OK");
                            }
                            return;
                        case "userupdate":
                            if (session.hasPerm(Permissions.ADMIN_ACCOUNT_EDIT, result)) {
                                int id = Integer.parseInt(request.getPost().get("id"));
                                UserData target = new UserData(server, id);

                                if (!session.canEdit(target, result)) return;

                                String column = request.getPost().get("column").replaceAll("[^a-zA-Z0-9]", "");

                                switch (column.toLowerCase()) {
                                    case "password":
                                        if (!session.hasPerm(Permissions.ADMIN_ACCOUNT_PASSWORD, result)) return;
                                        break;

                                    case "perm":
                                        if (!session.hasPerm(Permissions.ADMIN_ACCOUNT_GRANT, result)) return;
                                        break;
                                    default:
                                        if (!session.hasPerm(Permissions.ADMIN_ACCOUNT_DETAILS, result)) return;
                                        break;
                                }

                                SQLCommand.updateUser(
                                        id,
                                        column, request.getPost().get("data"),
                                        server.getDatabase(), true);

                                result.set("OK");
                            }
                            return;

                        case "usercheckperm":
                            if (session.hasPerm(Permissions.ADMIN_ACCOUNT_EDIT, result)) {

                                int id = Integer.parseInt(request.getPost().get("id"));
                                UserData userData = new UserData(server, id);

                                if (userData.hasPerm(request.getPost().get("node"))) {
                                    result.set("PERM/TRUE");
                                } else {
                                    result.set("PERM/FALSE");
                                }
                            }
                            return;

                        case "useradd":
                            if (session.hasPerm(Permissions.ADMIN_ACCOUNT_ADD, result)) {
                                SQLCommand.addUser(request.getPost(), server.getDatabase(), true);
                                result.set("OK");
                            }
                            return;

                        case "userdelete":
                            if (session.hasPerm(Permissions.ADMIN_ACCOUNT_DELETE, result)) {
                                int id = Integer.parseInt(request.getPost().get("id"));
                                UserData target = new UserData(server, id);

                                if(!session.canEdit(target,result)){
                                    return;
                                }

                                if (target.hasPerm(Permissions.ADMIN_ACCOUNT_PREVENT_EDIT) && !session.hasPerm(Permissions.ADMIN_ACCOUNT_PREVENT_EDIT_BYPASS)) {
                                    result.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
                                    result.set("");
                                    return;
                                }

                                SQLCommand.deleteUser(id, server.getDatabase(), true);
                                result.set("OK");
                                return;
                            }
                            return;

                        case "delete":
                            if (session.hasPerm(Permissions.ADMIN_LOG_DELETE, result)) {
                                int id = Integer.parseInt(request.getPost().get("id"));
                                SQLCommand.deleteLog(id, server.getDatabase(), true);
                                resetLastUpdate();
                                result.set("OK");
                                return;
                            }
                            return;

                        case "poll":
                            if (session.hasPerm(Permissions.ADMIN_LOG_VIEW, result)) {
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

            public void handleAuth(UserData session, String action, String addr, HttpRequestWrapper
                    request, ResultContainer result) throws SQLException {
                if (action.equals("login")) {
                    if (session == null) {
                        String user = request.getPost().get("username");
                        String pass = request.getPost().get("password");
                        if (SQLCommand.checkPassword(user, pass, server.getDatabase(), true)) {
                            String uuid = UUID.randomUUID().toString();
                            sessionMap.put(uuid, new UserData(server, uuid, user, SQLCommand.getUserId(user, server.getDatabase(), true)));
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
                        if (session.hasPerm(request.getPost().get("node")))
                            result.set("AUTH/OK");
                        else {
                            session.update(false);
                            result.set("AUTH/PERM_ERR");
                            result.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
                        }
                    }
                } else if (action.equalsIgnoreCase("isme")) {
                    if (session == null) {
                        result.set("AUTH/NO_SESSION");
                    } else if (session.getUserId() == Integer.parseInt(request.getPost().get("id"))) {
                        result.set("AUTH/TRUE");
                    } else {
                        result.set("AUTH/FALSE");
                    }
                }
            }

        });

        server.sync();
    }
}
