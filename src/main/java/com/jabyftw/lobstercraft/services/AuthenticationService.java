package com.jabyftw.lobstercraft.services;

import com.jabyftw.lobstercraft.LobstercraftPlugin;

public class AuthenticationService implements Service {

    public boolean initialize() {
        return false;
    }

    public void shutdown() {

    }

    public String getServiceName() {
        return "authentication service";
    }
}
