package net.otlg.studentcheckin.utils;

import com.google.gson.reflect.TypeToken;
import net.otlg.apiserver.config.ConfigLoader;
import net.otlg.apiserver.net.wrapper.HttpFormPostDecoder;
import net.otlg.studentcheckin.etc.LogEntryWrapper;
import net.otlg.studentcheckin.etc.UserEntryWrapper;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLCommand {

    public static void insertLog(String userID, Connection connection, boolean autoClose) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO log (`stid`) VALUES ((SELECT id FROM user WHERE user = ?));");
        statement.setString(1, userID);
        statement.execute();
        if (autoClose) connection.close();
    }

    public static String getPasswordHash(String name, Connection connection, boolean autoClose) throws SQLException {
        try {
            if (name == null) return null;
            PreparedStatement statement = connection.prepareStatement("SELECT password FROM user WHERE `user` = ?;");
            statement.setString(1, name);
            statement.execute();

            ResultSet resultSet = statement.getResultSet();
            if (!resultSet.next()) return null;
            return resultSet.getString(1);

        } finally {
            if (autoClose) connection.close();
        }
    }

    public static int getUserId(String name, Connection connection, boolean autoClose) throws SQLException {
        try {
            if (name == null) return -1;
            PreparedStatement statement = connection.prepareStatement("SELECT id FROM user WHERE `user` = ?;");
            statement.setString(1, name);
            statement.execute();

            ResultSet resultSet = statement.getResultSet();
            if (!resultSet.next()) return -1;
            return resultSet.getInt(1);

        } finally {
            if (autoClose) connection.close();
        }
    }

    public static boolean checkPassword(String name, String password, Connection connection, boolean autoClose) throws SQLException {
        try {
            String dbhash = getPasswordHash(name, connection, false);
            if (dbhash == null) return false;
            if (password == null) return false;
            return BCrypt.checkpw(password, dbhash);
        } finally {
            if (autoClose) connection.close();
        }
    }

    public static List<String> getPerms(String user, Connection connection, boolean autoClose) throws SQLException {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT perm FROM user WHERE `user` = ?;");
            statement.setString(1, user);
            statement.execute();

            ResultSet resultSet = statement.getResultSet();
            if (!resultSet.next()) return null;
            return ConfigLoader.GSON.fromJson(resultSet.getString(1), new TypeToken<List<String>>() {
            }.getType());

        } finally {
            if (autoClose) connection.close();
        }
    }

    public static List<UserEntryWrapper> getAdmins(Connection connection, boolean autoClose) throws SQLException {
        try {
            List<UserEntryWrapper> list = new ArrayList<>();

            PreparedStatement statement = connection.prepareStatement("SELECT id, name, email, nick, class, perm FROM studentcheckin.user WHERE perm LIKE '%admin%';");
            statement.execute();

            ResultSet resultSet = statement.getResultSet();

            while (resultSet.next()) {
                list.add(new UserEntryWrapper(
                        resultSet.getInt(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4),
                        resultSet.getString(5),
                        resultSet.getString(6)
                ));
            }

            return list;
        } finally {
            if (autoClose) connection.close();
        }
    }

    public static List<LogEntryWrapper> getDataList(HttpFormPostDecoder post, Connection connection, boolean autoClose) throws SQLException {
        try {
            PreparedStatement statement;

            String type = post.get("type");
            String value = post.get("value");

            if (type.equals("date")) {
                statement = connection.prepareStatement(
                        "SELECT log.id,log.stid, log.time, user.user, user.name ,user.nick,user.class FROM log LEFT JOIN user " +
                                "ON log.stid = user.id WHERE DATE(`log`.`time`) = ? ORDER BY id desc LIMIT 100;");

                statement.setString(1, value);
                statement.execute();
            } else if (type.equals("name")) {
                if (value.replaceAll("[%]", "").length() < 2) {
                    return null;
                }

                statement = connection.prepareStatement(
                        "SELECT log.id,log.stid, log.time, user.user, user.name ,user.nick,user.class FROM log LEFT JOIN user " +
                                "ON log.stid = user.id WHERE user.name like ? OR user.nick like ? ORDER BY id desc LIMIT 100;");

                statement.setString(1, "%" + value + "%");
                statement.setString(2, "%" + value + "%");
                statement.execute();
            } else if (type.equals("class")) {
                if (value.replaceAll("[%]", "").length() < 1) {
                    return null;
                }
                statement = connection.prepareStatement(
                        "SELECT log.id,log.stid, log.time, user.user, user.name ,user.nick,user.class FROM log LEFT JOIN user " +
                                "ON log.stid = user.id WHERE user.class like ? ORDER BY id desc LIMIT 100;");

                statement.setString(1, value + "%");
                statement.execute();
            } else {
                return new ArrayList<>();
            }

            ResultSet resultSet = statement.getResultSet();

            List<LogEntryWrapper> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(new LogEntryWrapper(
                        resultSet.getInt(1),
                        resultSet.getInt(2),
                        resultSet.getString(3),
                        resultSet.getString(4),
                        resultSet.getString(5),
                        resultSet.getString(6),
                        resultSet.getString(7)
                ));
            }
            return list;
        } finally {
            if (autoClose) connection.close();
        }
    }

    public static void deleteLog(int id, Connection connection, boolean autoClose) throws SQLException {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM `log` WHERE `id` = ?");
            statement.setInt(1, id);
            statement.execute();
        } finally {
            if (autoClose) connection.close();
        }
    }
}
