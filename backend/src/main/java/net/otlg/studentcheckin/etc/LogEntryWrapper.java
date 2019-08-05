package net.otlg.studentcheckin.etc;

public class LogEntryWrapper {
    private int id;
    private int stid;
    private String time;
    private String user;
    private String name;
    private String nick;
    private String classroom;

    public LogEntryWrapper(int id, int stid, String time, String user, String name, String nick, String classroom) {
        this.id = id;
        this.stid = stid;
        this.time = time;
        this.user = user;
        this.name = name;
        this.nick = nick;
        this.classroom = classroom;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStid() {
        return stid;
    }

    public void setStid(int stid) {
        this.stid = stid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
