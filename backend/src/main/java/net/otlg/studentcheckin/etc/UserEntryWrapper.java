package net.otlg.studentcheckin.etc;

public class UserEntryWrapper {
    private final int id;
    private final String name;
    private final String email;
    private final String nick;
    private final String classroom;
    private final String perm;

    public UserEntryWrapper(int id, String name, String email, String nick, String classroom, String perm) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.nick = nick;
        this.classroom = classroom;
        this.perm = perm;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNick() {
        return nick;
    }

    public String getClassroom() {
        return classroom;
    }

    public String getPerm() {
        return perm;
    }

    public String getName() {
        return name;
    }
}
