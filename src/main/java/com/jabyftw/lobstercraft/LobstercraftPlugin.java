package com.jabyftw.lobstercraft;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.jabyftw.lobstercraft.services.AuthenticationService;
import com.jabyftw.lobstercraft.services.Service;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class LobstercraftPlugin extends JavaPlugin {

    /* Plugin's instance */
    private static LobstercraftPlugin plugin = null;
    private static ConfigurationFile configuration = null;
    private static HikariDataSource mysqlPool = null;

    /* Plugin's services */
    private static Service[] services = null;
    private static AuthenticationService authenticationService = null;

    /* Vault integration: used for chat configuration and permission checking */
    private static Permission permission = null;
    private static Chat chat = null;

    /* ProtocolLib integration: used for packet manipulation */
    private static ProtocolManager protocolManager = null;


    @Override
    public void onEnable() {
        try {
            /*
             * Register this instance
             */
            plugin = this;
            try {
                configuration = new ConfigurationFile(this, "config.yml");
            } catch (IOException | InvalidConfigurationException exception) {
                throw new IllegalStateException("Couldn't read configuration file!", exception);
            }


            /*
             * Attempt to register MySQL connection pool
             */
            // Starting with the configuration
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDataSourceClassName(configuration.getString(Configuration.MYSQL_HIKARI_DATASOURCE));
            // Credentials
            hikariConfig.setJdbcUrl(configuration.getString(Configuration.MYSQL_JDBC_URL));
            hikariConfig.setUsername(configuration.getString(Configuration.MYSQL_USERNAME));
            hikariConfig.setPassword(configuration.getString(Configuration.MYSQL_PASSWORD));
            // MaximumPoolSize: from documentation, "(Core count * 2) + spindle"
            hikariConfig.setMaximumPoolSize(configuration.getInt(Configuration.MYSQL_POOL_SIZE));
            // ConnectionTimeout: milliseconds before throwing exceptions
            hikariConfig.setConnectionTimeout(TimeUnit.SECONDS.toMillis(configuration.getInt(
                    Configuration.MYSQL_HIKARI_CONNECTION_TIMEOUT_MILLISECONDS)));
            // MaxLifetime: milliseconds before idling timeout
            hikariConfig.setMaxLifetime(
                    TimeUnit.SECONDS.toMillis(configuration.getInt(Configuration.MYSQL_LIFETIME_TIMEOUT_SECONDS) - 60));

            // Creating the pool
            mysqlPool = new HikariDataSource(hikariConfig);

            // Testing the MySQL connection
            try {
                // Check if the connection is valid using a timeout in seconds
                if (!mysqlPool.getConnection().isValid(configuration.getInt(
                        Configuration.MYSQL_JAVA_CONNECTION_TIMEOUT_SECONDS)))
                    throw new SQLException("Connection isn't valid.");
            } catch (SQLException exception) {
                throw new IllegalStateException("Couldn't setup required MySQL connection.", exception);
            }


            /*
             * Attempt to register Vault
             */
            if (!setupChat()) {
                getLogger().warning("Couldn't setup Vault's chat integration!");
            }
            if (!setupPermissions()) {
                throw new IllegalStateException("Couldn't setup Vault's permission integration! Can't continue.");
            }


            /*
             * Attempt to register ProtocolLib
             */
            if (getServer().getPluginManager().isPluginEnabled("ProtocolLib"))
                protocolManager = ProtocolLibrary.getProtocolManager();
            else
                getLogger().warning("Couldn't find ProtocolLib plugin!");


            /*
             * Initialize our services
             */
            services = new Service[]{
                    authenticationService = new AuthenticationService(),
            };
            for (Service service : services) {
                // We use try-catch for catching exceptions inside services initialization too
                try {
                    if (!service.initialize()) {
                        throw new Exception("Initialization failed!");
                    }
                } catch (Exception exception) {
                    // catch returning false to initialize AND exceptions inside initialization
                    throw new IllegalStateException(
                            "Couldn't initialize " + service.getServiceName() + ": " + exception.getMessage(),
                            exception
                    );
                }
            }
        } catch (Exception exception) {
            getLogger().severe("Failed to start essential plugin, stopping server. Cause: " + exception.getMessage());
            exception.printStackTrace();
            Bukkit.shutdown();
            return;
        }

        getLogger().info("LobsterCraft enabled!");
    }

    @Override
    public void onDisable() {
        /*
         * Shutdown all services
         */
        if (services != null)
            for (Service service : services) {
                service.shutdown();
            }


        /*
         * Delete previous instances of services
         */
        services = null;
        // Delete the instance itself
        authenticationService = null;


        /*
         * Stop Vault's integration
         */
        permission = null;
        chat = null;


        /*
         * Stop ProtocolLib's integration
         */
        if (protocolManager != null)
            protocolManager.removePacketListeners(this);
        protocolManager = null;


        /*
         * Stop MySQL connection pool
         */
        if (mysqlPool != null)
            mysqlPool.close();
        mysqlPool = null;


        /*
         * Delete the reference to this instance
         */
        configuration = null;
        plugin = null;
    }

    /*
     * Static methods (mostly getters/setters)
     */

    /**
     * Get current Plugin's instance
     *
     * @return null if plugin hasn't been initialized.
     */
    public static LobstercraftPlugin getPlugin() {
        return plugin;
    }

    /**
     * Get current plugin's configuration
     *
     * @return null if plugin hasn't been initialized
     */
    public static ConfigurationFile getConfiguration() {
        return configuration;
    }

    /**
     * Get the Vault's chat instance.
     *
     * @return null if couldn't initialize Vault's chat integration or if this plugin hasn't been initialized
     */
    public static Chat getChat() {
        return chat;
    }

    /**
     * Get Vault's permission instance.
     *
     * @return null only if this plugin hasn't been initialized.
     */
    public static Permission getPermission() {
        return permission;
    }

    /**
     * Get ProtocolLib's manager instance
     *
     * @return null if couldn't integrate to ProtocolLib or if this plugin hasn't been initialized
     */
    public static ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    /**
     * Get MySQL connection pool
     *
     * @return null if this plugin hasn't been initialized
     */
    public static HikariDataSource getMysqlPool() {
        return mysqlPool;
    }

    /*
     * Vault setup methods
     */

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider =
                getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return permission != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider =
                getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return chat != null;
    }

}
