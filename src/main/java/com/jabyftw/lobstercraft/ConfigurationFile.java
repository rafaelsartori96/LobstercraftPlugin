package com.jabyftw.lobstercraft;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class ConfigurationFile extends YamlConfiguration {

    private final File configurationFile;

    public ConfigurationFile(final Plugin plugin, final String fileName) throws IOException, InvalidConfigurationException {
        super();
        this.configurationFile = new File(plugin.getDataFolder(), fileName);
        loadFile();
    }

    public String getFileName() {
        return configurationFile.getName();
    }

    public void saveFile() throws IOException {
        save(configurationFile);
    }

    public void loadFile() throws IOException, InvalidConfigurationException {
        // Load file if it exists
        if (configurationFile.exists())
            load(configurationFile);

        // Insert non-present default configurations
        for (Configuration configurationValue : Configuration.values())
            if (!contains(configurationValue.toString()))
                set(configurationValue.toString(), configurationValue.getDefaultValue());

        // Save changes and load file again
        saveFile();
        load(configurationFile);
    }

    public int getInt(Configuration configurationValue) {
        return super.getInt(configurationValue.toString());
    }

    public String getString(Configuration configurationValue) {
        return super.getString(configurationValue.toString());
    }
}
