package com.jabyftw.lobstercraft.services;

public interface Service {

    /**
     * Initialize service
     *
     * @return false if couldn't initialize service
     */
    boolean initialize();

    /**
     * Shutdown service
     */
    void shutdown();

    /**
     * Get the service name
     *
     * @return the service name
     */
    String getServiceName();

}
