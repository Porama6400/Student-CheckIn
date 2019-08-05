package net.otlg.apiserver.config;

public class ServerConfigFile {

    private int serverPort = 8080;
    private int serverBossThreads = 1;
    private int serverWorkerThreads = 10;
    private String allowedHeader = "";

    private boolean databaseEnabled = true;
    private String databaseUrl = "url";
    private String databaseUser = "dbuser";
    private String databasePassword = "dbpasswd";
    private int databaseConnectionTimeout = 5000;
    private int databaseValidationTimeout = 5000;
    private int databasePoolSize = 10;

    private boolean SslEnabled = true;
    private String SslKeychainFile = "/home/whatever";
    private String SslKeyFile = "/home/whatever";
    private String SslKeyPassword = "keypasswd";

    public ServerConfigFile() {
    }

    public int getDatabaseConnectionTimeout() {
        return databaseConnectionTimeout;
    }

    public void setDatabaseConnectionTimeout(int databaseConnectionTimeout) {
        this.databaseConnectionTimeout = databaseConnectionTimeout;
    }

    public int getDatabaseValidationTimeout() {
        return databaseValidationTimeout;
    }

    public void setDatabaseValidationTimeout(int databaseValidationTimeout) {
        this.databaseValidationTimeout = databaseValidationTimeout;
    }

    public int getServerBossThreads() {
        return serverBossThreads;
    }

    public void setServerBossThreads(int serverBossThreads) {
        this.serverBossThreads = serverBossThreads;
    }

    public int getServerWorkerThreads() {
        return serverWorkerThreads;
    }

    public void setServerWorkerThreads(int serverWorkerThreads) {
        this.serverWorkerThreads = serverWorkerThreads;
    }

    public int getDatabasePoolSize() {
        return databasePoolSize;
    }

    public void setDatabasePoolSize(int databasePoolSize) {
        this.databasePoolSize = databasePoolSize;
    }

    public boolean isDatabaseEnabled() {
        return databaseEnabled;
    }

    public void setDatabaseEnabled(boolean databaseEnabled) {
        this.databaseEnabled = databaseEnabled;
    }

    public boolean isSslEnabled() {
        return SslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        SslEnabled = sslEnabled;
    }

    public String getSslKeychainFile() {
        return SslKeychainFile;
    }

    public void setSslKeychainFile(String sslKeychainFile) {
        SslKeychainFile = sslKeychainFile;
    }

    public String getSslKeyFile() {
        return SslKeyFile;
    }

    public void setSslKeyFile(String sslKeyFile) {
        SslKeyFile = sslKeyFile;
    }

    public String getSslKeyPassword() {
        return SslKeyPassword;
    }

    public void setSslKeyPassword(String sslKeyPassword) {
        SslKeyPassword = sslKeyPassword;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getAllowedHeader() {
        return allowedHeader;
    }

    public void setAllowedHeader(String allowedHeader) {
        this.allowedHeader = allowedHeader;
    }
}
