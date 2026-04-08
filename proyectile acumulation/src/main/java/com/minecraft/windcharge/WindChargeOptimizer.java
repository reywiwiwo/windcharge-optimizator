package com.minecraft.windcharge;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class WindChargeOptimizer extends JavaPlugin {
    
    private WindChargeManager windChargeManager;
    private BukkitTask optimizationTask;
    private ConfigManager configManager;
    
    @Override
    public void onEnable() {
        // Guardar configuración por defecto
        saveDefaultConfig();
        
        // Inicializar managers
        configManager = new ConfigManager(this);
        windChargeManager = new WindChargeManager(this, configManager);
        
        // Registrar eventos
        getServer().getPluginManager().registerEvents(new WindChargeListener(this, windChargeManager), this);
        
        // Registrar comandos
        getCommand("windcharge").setExecutor(new WindChargeCommand(this, windChargeManager, configManager));
        
        // Iniciar tarea de optimización
        startOptimizationTask();
        
        getLogger().info("WindChargeOptimizer ha sido activado correctamente!");
    }
    
    @Override
    public void onDisable() {
        // Detener tarea de optimización
        if (optimizationTask != null) {
            optimizationTask.cancel();
        }
        
        // Limpiar entidades combinadas
        if (windChargeManager != null) {
            windChargeManager.cleanup();
        }
        
        getLogger().info("WindChargeOptimizer ha sido desactivado.");
    }
    
    private void startOptimizationTask() {
        long interval = configManager.getCheckInterval() * 20L; // Convertir a ticks
        
        optimizationTask = getServer().getScheduler().runTaskTimer(this, () -> {
            windChargeManager.optimizeWindCharges();
        }, interval, interval);
    }
    
    public void restartOptimizationTask() {
        if (optimizationTask != null) {
            optimizationTask.cancel();
        }
        startOptimizationTask();
    }
}
