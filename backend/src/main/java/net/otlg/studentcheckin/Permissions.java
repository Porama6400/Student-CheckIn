package net.otlg.studentcheckin;

public enum Permissions {
    ADMIN_ACCOUNT_VIEW("admin.account.view", true),
    ADMIN_ACCOUNT_EDIT("admin.account.edit", true),
    ADMIN_ACCOUNT_ADD("admin.account.add", true),
    ADMIN_ACCOUNT_DELETE("admin.account.delete", true),
    ADMIN_ACCOUNT_GRANT("admin.account.grant", true),
    ADMIN_ACCOUNT_PREVENT_EDIT("admin.account.noedit", true),
    ADMIN_ACCOUNT_PREVENT_EDIT_BYPASS("admin.account.noeditbypass", true),
    ADMIN_ACCOUNT_PREVENT_SELF_EDIT("admin.account.noselfedit", false),
    ADMIN_ACCOUNT_PASSWORD("admin.account.password", true),
    ADMIN_LOG_VIEW("admin.log.view", true),
    ADMIN_LOG_DELETE("admin.log.delete", true),
    SUPERUSER("superuser", false);

    private final String node;
    private final boolean superUserAllow;

    Permissions(String node, boolean superUserAllow) {
        this.node = node;
        this.superUserAllow = superUserAllow;
    }

    public static Permissions byNode(String node) {
        for (Permissions value : Permissions.values()) {
            if (value.node.equalsIgnoreCase(node)) {
                return value;
            }
        }
        return null;
    }

    public String getNode() {
        return node;
    }

    @Override
    public String toString() {
        return node;
    }

    public boolean isSuperUserAllow() {
        return superUserAllow;
    }
}
