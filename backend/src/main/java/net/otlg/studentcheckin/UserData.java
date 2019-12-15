package net.otlg.studentcheckin;

import io.netty.handler.codec.http.HttpResponseStatus;
import net.otlg.apiserver.APIServer;
import net.otlg.apiserver.net.ResultContainer;
import net.otlg.studentcheckin.utils.SQLCommand;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserData {
    private final APIServer server;

    private String sessid;
    private String user;
    private int userId;
    private List<String> perms = new ArrayList<>();

    private long lastDbFetch = 0;

    public UserData(APIServer server, String sessionID, String user, int userId) {
        this.server = server;
        this.sessid = sessionID;
        this.user = user;
        this.userId = userId;
    }

    public UserData(APIServer server, int userId) {
        this(server, null, null, userId);
        try {
            this.user = SQLCommand.getUserNameById(userId, server.getDatabase(), true);
            update(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getUserId() {
        return userId;
    }

    public String getUser() {
        return user;
    }

    public List<String> getPerms() {
        return getPerms(false);
    }

    public void update(boolean force) {
        if (lastDbFetch < System.currentTimeMillis() + 15000 || force) {
            lastDbFetch = System.currentTimeMillis();
            getPerms(true);
        }
    }

    public List<String> getPerms(boolean update) {
        try {
            if (update) perms = Objects.requireNonNull(SQLCommand.getPerms(getUser(), server.getDatabase(), true));
            return perms;
        } catch (SQLException e) {
            e.printStackTrace();
            return perms;
        }
    }

    public boolean hasPerm(String perm) {
        return hasPerm(Permissions.byNode(perm));
    }

    public boolean hasPerm(Permissions perm) {
        return hasPerm(perm, null);
    }

    public boolean hasPerm(Permissions perm, ResultContainer resultContainer) {

        boolean boolResult = getPerms().contains(perm.getNode());
        boolResult |= (getPerms().contains(Permissions.SUPERUSER.getNode()) && perm.isSuperUserAllow());

        if (!boolResult && resultContainer != null) {
            resultContainer.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
        }

        return boolResult;
    }

    public boolean canEdit(UserData target, ResultContainer result) {

        if (hasPerm(Permissions.SUPERUSER)) return true;

        if (target.hasPerm(Permissions.SUPERUSER)) {
            if (result != null) {
                result.set("Superuser can not be edit!");
                result.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
            }
            return false;
        }

        if (!hasPerm(Permissions.ADMIN_ACCOUNT_SELF_EDIT) && (getUserId() == target.getUserId())) {
            if (result != null) {
                result.set("Self editing is not allowed!");
                result.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
            }
            return false;
        }

        if (target.hasPerm(Permissions.ADMIN_ACCOUNT_PREVENT_EDIT)
                && !hasPerm(Permissions.ADMIN_ACCOUNT_PREVENT_EDIT_BYPASS)
                && target.getUserId() != getUserId()) {
            if (result != null) {
                result.set("Target user can not be edited!");
                result.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
            }
            return false;
        }

        return true;
    }

    public String getSessionID() {
        return sessid;
    }
}
