package com.jabyftw.lobstercraft;

/**
 * Plugin configuration class
 *
 * Holds configuration names and default value.
 */
public enum Configuration {

    // "org.mariadb.jdbc.Driver" is the official driver, but the pool datasource uses "java.sql.Datasource", so we need
    // to use "org.mariadb.jdbc.MariaDbDataSource"
    MYSQL_HIKARI_DATASOURCE("mysql.hikari_datasource_class", "org.mariadb.jdbc.MariaDbDataSource"),
    MYSQL_JDBC_URL("mysql.jdbc_url", "jdbc:mariadb://localhost:3306/database"),
    MYSQL_USERNAME("mysql.username", "root"),
    MYSQL_PASSWORD("mysql.password", "p455w0rd"),
    MYSQL_POOL_SIZE("mysql.pool_size", 8),
    MYSQL_HIKARI_CONNECTION_TIMEOUT_MILLISECONDS("mysql.connection_timeout_milliseconds", 250),
    MYSQL_JAVA_CONNECTION_TIMEOUT_SECONDS("mysql.java_connection_timeout_seconds", 1),
    MYSQL_LIFETIME_TIMEOUT_SECONDS("mysql.connection_lifetime_timeout", 60 * 30),
    ;

    private final String path;
    private final Object defaultValue;

    Configuration(String path, Object defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return path;
    }

}
