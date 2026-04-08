package com.minecraft.windcharge;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ConfigManager {
    
    private final JavaPlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    
    public double getSearchRadius() {
        return config.getDouble("search-radius", 10.0);
    }
    
    public int getCheckInterval() {
        return config.getInt("check-interval", 5);
    }
    
    public int getMinimumCharges() {
        return config.getInt("minimum-charges", 3);
    }
    
    public int getMaxChargesPerEntity() {
        return config.getInt("max-charges-per-entity", 50);
    }
    
    public boolean isDebugMode() {
        return config.getBoolean("debug-mode", false);
    }
    
    public List<String> getEnabledWorlds() {
        return config.getStringList("enabled-worlds");
    }
    
    public boolean showParticles() {
        return config.getBoolean("show-particles", true);
    }
    
    public boolean playSound() {
        return config.getBoolean("play-sound", true);
    }
    
    public boolean showChargeTag() {
        return config.getBoolean("show-charge-tag", true);
    }
    
    public String getTagColor() {
        return config.getString("tag-color", "#FFAA00");
    }
    
    public boolean inheritFirstChargeProperties() {
        return config.getBoolean("inherit-first-charge-properties", true);
    }
    
    public boolean autoAccumulate() {
        return config.getBoolean("auto-accumulate", true);
    }
    
    public boolean updateTagContinuously() {
        return config.getBoolean("update-tag-continuously", true);
    }
    
    public boolean isWorldEnabled(String worldName) {
        List<String> enabledWorlds = getEnabledWorlds();
        return enabledWorlds.isEmpty() || enabledWorlds.contains(worldName);
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
}
