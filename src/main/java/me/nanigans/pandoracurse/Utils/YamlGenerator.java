package me.nanigans.pandoracurse.Utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class YamlGenerator {
    private FileConfiguration data;
    private File messagesFile;

    private void load() {
        if (!this.messagesFile.exists()) {
            try {
                this.messagesFile.createNewFile();
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
        this.data = YamlConfiguration.loadConfiguration(this.messagesFile);
    }

    public YamlGenerator(String path) {
        this.messagesFile = new File(path);
        load();
    }

    public FileConfiguration getData() {
        return this.data;
    }

    public void save() {
        try {
            this.data.save(this.messagesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadData() {
        this.data = YamlConfiguration.loadConfiguration(this.messagesFile);
    }

    /**
     * Returns a {@link Map} representative of the passed Object that represents
     * a section of a YAML file. This method neglects the implementation of the
     * section (whether it be {@link ConfigurationSection} or just a
     * {@link Map}), and returns the appropriate value.
     *
     * @since 0.1.0
     * @version 0.1.0
     *
     * @param o The object to interpret
     * @param deep If an object is a {@link ConfigurationSection}, {@code true} to do a deep search
     * @return A {@link Map} representing the section
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> getConfigSectionValue(Object o, boolean deep) {
        if (o == null) {
            return null;
        }
        Map<String, T> map;
        if (o instanceof ConfigurationSection) {
            map = (Map<String, T>) ((ConfigurationSection) o).getValues(deep);
        } else if (o instanceof Map) {
            map = (Map<String, T>) o;
        } else {
            return null;
        }
        return map;
    }
}