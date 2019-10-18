package net.otlg.apiserver;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import net.otlg.apiserver.config.ServerConfigFile;
import net.otlg.apiserver.config.ServerInternalConfig;
import net.otlg.apiserver.net.ServerChannelInitializer;
import net.otlg.apiserver.net.ServerExceptionHandler;
import net.otlg.apiserver.net.RequestHandler;

import javax.net.ssl.SSLException;
import java.io.File;
import java.security.cert.CertificateException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class APIServer {
    private final ServerConfigFile config;
    private final ServerInternalConfig internalConfig;
    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private List<RequestHandler> handlerList = new ArrayList<>();
    private SslContext sslCtx;
    private HikariDataSource dataSource;

    public APIServer(ServerConfigFile config) {
        this(config, new ServerInternalConfig());
    }

    public APIServer(ServerConfigFile config, ServerInternalConfig internalConfig) {
        this.config = config;
        this.internalConfig = internalConfig;

        if (config.isSslEnabled()) {
            try {

                if (config.getSslKeychainFile().isEmpty() || config.getSslKeyFile().isEmpty()) {
                    setupSsl(null, null, null);
                } else {
                    setupSsl(new File(config.getSslKeychainFile()), new File(config.getSslKeyFile()), config.getSslKeyPassword());
                }

            } catch (CertificateException | SSLException e) {
                e.printStackTrace();
            }
        }

        if (config.isDatabaseEnabled()) {
            HikariConfig hikariConfig = new HikariConfig();

            hikariConfig.setJdbcUrl(config.getDatabaseUrl());
            hikariConfig.setUsername(config.getDatabaseUser());
            hikariConfig.setPassword(config.getDatabasePassword());

            hikariConfig.setConnectionTimeout(config.getDatabaseConnectionTimeout());
            hikariConfig.setValidationTimeout(config.getDatabaseValidationTimeout());
            hikariConfig.setMaximumPoolSize(config.getDatabasePoolSize());
            hikariConfig.setLeakDetectionThreshold(15000);

            hikariConfig.addDataSourceProperty("characterEncoding","utf8");
            hikariConfig.addDataSourceProperty("useUnicode","true");

            dataSource = new HikariDataSource(hikariConfig);
        }
    }

    public ServerInternalConfig getInternalConfig() {
        return internalConfig;
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public Connection getDatabase() throws SQLException {
        return dataSource.getConnection();
    }

    public ServerConfigFile getConfig() {
        return config;
    }

    public EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public void run() {
        bossGroup = new NioEventLoopGroup(config.getServerBossThreads());
        workerGroup = new NioEventLoopGroup(config.getServerWorkerThreads());
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new ServerExceptionHandler(this))
                    .childHandler(new ServerChannelInitializer(this));

            channel = b.bind(config.getServerPort()).sync().channel();
            System.out.println("Server started on " + channel.localAddress().toString());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sync() {
        try {
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        channel.close();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        if (dataSource != null) dataSource.close();
    }

    public void setupSsl(File keychain, File key, String password) throws CertificateException, SSLException {
        if (keychain == null || !keychain.exists() || key == null || !key.exists()) {
            SelfSignedCertificate ssc = new SelfSignedCertificate(UUID.randomUUID().toString());
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else if (password == null || password.isEmpty()) {
            sslCtx = SslContextBuilder.forServer(keychain, key).build();
        } else {
            sslCtx = SslContextBuilder.forServer(keychain, key, password).build();
        }
    }

    public List<RequestHandler> handlers() {
        return handlerList;
    }

    public SslContext getSslContext() {
        return sslCtx;
    }
}
