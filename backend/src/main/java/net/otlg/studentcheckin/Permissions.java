package net.otlg.studentcheckin;

public enum Permissions {
    ADMIN_ACCOUNT_VIEW("admin.account.view"),
    ADMIN_ACCOUNT_EDIT("admin.account.edit"),
    ADMIN_ACCOUNT_ADD("admin.account.add"),
    ADMIN_ACCOUNT_GRANT("admin.account.grant"),
    ADMIN_ACCOUNT_PASSWORD("admin.account.password"),
    ADMIN_LOG_VIEW("admin.log.view"),
    ADMIN_LOG_DELETE("admin.log.delete");

    private final String node;

    Permissions(String node) {
        this.node = node;
    }

    public String getNode() {
        return node;
    }

    @Override
    public String toString() {
        return node;
    }
}
