package com.jabyftw.lobstercraft;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.jabyftw.lobstercraft.services.AuthenticationService;
import com.jabyftw.lobstercraft.services.Service;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public class LobstercraftPlugin extends JavaPlugin {

    /* Plugin's instance */
    private static LobstercraftPlugin plugin = null;

    /* Plugin's services */
    private static AuthenticationService authenticationService = null;

    /* Vault integration: used for chat configuration and permission checking */
    private static Permission permission = null;
    private static Chat chat = null;

    /* ProtocolLib integration: used for packet manipulation */
    private static ProtocolManager protocolManager = null;

    @Override
    public void onEnable() {
        /* Register this instance */
        plugin = this;

        /* Attempt to register Vault */
        if (!setupChat()) {
            getLogger().warning("Couldn't setup Vault's chat integration!");
        }
        if (!setupPermissions()) {
            throw new IllegalStateException("Couldn't setup Vault's permission integration! Can't continue.");
        }

        /* Attempt to register ProtocolLib */
        protocolManager = ProtocolLibrary.getProtocolManager();

        /* Initialize our services */
        Service[] services = new Service[]{
                authenticationService = new AuthenticationService(),
        };
        for (Service service : services) {
            try {
                if (!service.initialize()) {
                    throw new Exception("Initialization failed!");
                }
            } catch (Exception e) {
                getLogger().severe("Couldn't initialize " + service.getServiceName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        /* Stop Vault's integration, we don't know whats going to happen */
        permission = null;
        chat = null;

        /* Stop everything, delete instance */
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

    /*
     * Vault setup methods
     */

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return permission != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return chat != null;
    }

}
