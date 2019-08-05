package net.otlg.apiserver.config;

public class ServerInternalConfig {

    private String serverHeader = "Apache/2.4.18";

    public String getServerHeader() {
        return serverHeader;
    }

    public void setServerHeader(String serverHeader) {
        this.serverHeader = serverHeader;
    }
}
