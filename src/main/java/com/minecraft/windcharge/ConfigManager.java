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
    
    public int getMaxExplosionsPerTick() {
        return config.getInt("max-explosions-per-tick", 5);
    }
    
    public float getMaxExplosionPower() {
        return (float) config.getDouble("max-explosion-power", 100.0);
    }
    
    public int getMaxBlocksPerExplosion() {
        return config.getInt("max-blocks-per-explosion", 200);
    }
    
    public double getKnockbackBaseRadius() {
        return config.getDouble("knockback-base-radius", 5.0);
    }
    
    public double getKnockbackRadiusPerCharge() {
        return config.getDouble("knockback-radius-per-charge", 0.5);
    }
    
    public double getKnockbackBaseForce() {
        return config.getDouble("knockback-base-force", 2.0);
    }
    
    public double getKnockbackForcePerCharge() {
        return config.getDouble("knockback-force-per-charge", 0.3);
    }
    
    public double getKnockbackVerticalRatio() {
        return config.getDouble("knockback-vertical-ratio", 0.8);
    }
    
    public double getKnockbackPlayerBoost() {
        return config.getDouble("knockback-player-boost", 0.5);
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
