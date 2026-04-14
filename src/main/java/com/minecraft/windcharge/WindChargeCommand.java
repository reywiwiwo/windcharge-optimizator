package com.minecraft.windcharge;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;

public class WindChargeCommand implements CommandExecutor, TabCompleter {
    
    private final WindChargeOptimizer plugin;
    private final WindChargeManager windChargeManager;
    private final ConfigManager configManager;
    
    public WindChargeCommand(WindChargeOptimizer plugin, WindChargeManager windChargeManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.windChargeManager = windChargeManager;
        this.configManager = configManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("windcharge.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                reloadConfig(sender);
                break;
            case "toggle":
                toggleOptimization(sender);
                break;
            case "info":
                showInfo(sender);
                break;
            case "status":
                showStatus(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void reloadConfig(CommandSender sender) {
        configManager.reloadConfig();
        plugin.restartOptimizationTask();
        sender.sendMessage(ChatColor.GREEN + "Configuración recargada correctamente.");
    }
    
    private void toggleOptimization(CommandSender sender) {
        // Implementar toggle si es necesario
        sender.sendMessage(ChatColor.YELLOW + "Función de toggle no implementada aún.");
    }
    
    private void showInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== WindChargeOptimizer v1.0.0 ===");
        sender.sendMessage(ChatColor.AQUA + "Radio de búsqueda: " + ChatColor.WHITE + configManager.getSearchRadius() + " bloques");
        sender.sendMessage(ChatColor.AQUA + "Intervalo de verificación: " + ChatColor.WHITE + configManager.getCheckInterval() + " segundos");
        sender.sendMessage(ChatColor.AQUA + "Cargas mínimas: " + ChatColor.WHITE + configManager.getMinimumCharges());
        sender.sendMessage(ChatColor.AQUA + "Máximo por entidad: " + ChatColor.WHITE + configManager.getMaxChargesPerEntity());
        sender.sendMessage(ChatColor.AQUA + "Acumulación automática: " + ChatColor.WHITE + (configManager.autoAccumulate() ? "Activada" : "Desactivada"));
        sender.sendMessage(ChatColor.AQUA + "Herencia de propiedades: " + ChatColor.WHITE + (configManager.inheritFirstChargeProperties() ? "Activada" : "Desactivada"));
        sender.sendMessage(ChatColor.AQUA + "Tags de cargas: " + ChatColor.WHITE + (configManager.showChargeTag() ? "Activados" : "Desactivados"));
        sender.sendMessage(ChatColor.AQUA + "Color de tags: " + ChatColor.WHITE + configManager.getTagColor());
        sender.sendMessage(ChatColor.AQUA + "Actualización continua: " + ChatColor.WHITE + (configManager.updateTagContinuously() ? "Activada" : "Desactivada"));
        sender.sendMessage(ChatColor.AQUA + "Modo debug: " + ChatColor.WHITE + (configManager.isDebugMode() ? "Activado" : "Desactivado"));
        sender.sendMessage(ChatColor.AQUA + "Partículas: " + ChatColor.WHITE + (configManager.showParticles() ? "Activadas" : "Desactivadas"));
        sender.sendMessage(ChatColor.AQUA + "Sonido: " + ChatColor.WHITE + (configManager.playSound() ? "Activado" : "Desactivado"));
    }
    
    private void showStatus(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Estado del Optimizador ===");
        sender.sendMessage(ChatColor.GREEN + "Plugin activo y funcionando");
        sender.sendMessage(ChatColor.AQUA + "Mundos habilitados: " + 
                         (configManager.getEnabledWorlds().isEmpty() ? "Todos" : 
                          String.join(", ", configManager.getEnabledWorlds())));
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Comandos de WindChargeOptimizer ===");
        sender.sendMessage(ChatColor.YELLOW + "/windcharge reload" + ChatColor.WHITE + " - Recargar configuración");
        sender.sendMessage(ChatColor.YELLOW + "/windcharge info" + ChatColor.WHITE + " - Mostrar información del plugin");
        sender.sendMessage(ChatColor.YELLOW + "/windcharge status" + ChatColor.WHITE + " - Mostrar estado actual");
        sender.sendMessage(ChatColor.YELLOW + "/windcharge toggle" + ChatColor.WHITE + " - Activar/desactivar optimización");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "toggle", "info", "status");
        }
        return Arrays.asList();
    }
}
