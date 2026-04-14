package com.minecraft.windcharge;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class WindChargeListener implements Listener {
    
    private final WindChargeOptimizer plugin;
    private final WindChargeManager windChargeManager;
    private final ConfigManager configManager;
    
    // Cache de reflection para optimización
    private static Field cachedExplosionField = null;
    private static boolean reflectionCacheInitialized = false;
    
    // Rate limiting para evitar timeouts
    private final AtomicInteger explosionsThisTick = new AtomicInteger(0);
    private final ConcurrentLinkedQueue<PendingExplosion> pendingExplosions = new ConcurrentLinkedQueue<>();
    private BukkitTask explosionProcessorTask = null;
    
    public WindChargeListener(WindChargeOptimizer plugin, WindChargeManager windChargeManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.windChargeManager = windChargeManager;
        this.configManager = configManager;
        initializeReflectionCache();
        startExplosionProcessor();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof WindCharge)) {
            return;
        }
        
        WindCharge windCharge = (WindCharge) event.getEntity();
        
        // Verificar si es una carga combinada
        if (windChargeManager.getChargeCount(windCharge) > 1) {
            // Rate limiting: si hay demasiadas explosiones este tick, posponer
            int maxExplosionsPerTick = configManager.getMaxExplosionsPerTick();
            int current = explosionsThisTick.get();
            
            if (current >= maxExplosionsPerTick) {
                // Posponer explosión para el siguiente tick
                pendingExplosions.offer(new PendingExplosion(windCharge, windChargeManager.getChargeCount(windCharge)));
                event.setCancelled(true);
                windCharge.remove();
                return;
            }
            
            explosionsThisTick.incrementAndGet();
            
            // Cancelar el evento para manejar la explosión optimizada
            event.setCancelled(true);
            
            // Crear una sola explosión masiva con knockback abrumador
            createMassiveExplosionWithKnockback(windCharge);
            windCharge.remove();
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        
        if (!(entity instanceof WindCharge)) {
            return;
        }
        
        // Optimizar procesamiento de bloques para cargas combinadas
        int chargeCount = windChargeManager.getChargeCount(entity);
        if (chargeCount > 1) {
            // Limitar bloques afectados para evitar lag
            int maxBlocks = Math.min(
                event.blockList().size(),
                configManager.getMaxBlocksPerExplosion()
            );
            
            if (event.blockList().size() > maxBlocks) {
                event.blockList().subList(maxBlocks, event.blockList().size()).clear();
            }
        }
    }
    
    // Métodos de optimización
    
    private void initializeReflectionCache() {
        if (!reflectionCacheInitialized) {
            try {
                // Pre-calcular el campo de reflection para mejor rendimiento
                Class<?> craftWindChargeClass = Class.forName("org.bukkit.craftbukkit.v1_21_R1.entity.CraftWindCharge");
                Object dummyInstance = craftWindChargeClass.getDeclaredConstructor().newInstance();
                Object handle = craftWindChargeClass.getMethod("getHandle").invoke(dummyInstance);
                
                for (Field field : handle.getClass().getDeclaredFields()) {
                    if (field.getType() == float.class) {
                        field.setAccessible(true);
                        float value = field.getFloat(handle);
                        if (value > 0 && value < 10) { // Potencia típica de wind charge
                            cachedExplosionField = field;
                            break;
                        }
                    }
                }
                reflectionCacheInitialized = true;
            } catch (Exception e) {
                plugin.getLogger().warning("No se pudo inicializar cache de reflection: " + e.getMessage());
            }
        }
    }
    
    private boolean processOptimizedExplosion(WindCharge windCharge) {
        if (cachedExplosionField == null) {
            return false;
        }
        
        try {
            int chargeCount = windChargeManager.getChargeCount(windCharge);
            Object craftWindCharge = windCharge.getClass().getMethod("getHandle").invoke(windCharge);
            
            float originalPower = cachedExplosionField.getFloat(craftWindCharge);
            float newPower = Math.min(originalPower * chargeCount, configManager.getMaxExplosionPower());
            cachedExplosionField.setFloat(craftWindCharge, newPower);
            
            // Dejar que explote naturalmente
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void createMassiveExplosionWithKnockback(WindCharge windCharge) {
        int chargeCount = windChargeManager.getChargeCount(windCharge);
        Location loc = windCharge.getLocation();
        
        // Calcular potencia masiva (una sola explosión)
        float power = Math.min(3.0f * chargeCount, configManager.getMaxExplosionPower());
        
        // Crear explosión visual y sonora
        loc.getWorld().createExplosion(
            loc,
            power,
            false, // No crear fuego
            false, // No romper bloques (comportamiento de carga de viento)
            windCharge
        );
        
        // Aplicar knockback masivo a todas las entidades cercanas
        applyMassiveKnockback(loc, chargeCount);
    }
    
    private void applyMassiveKnockback(Location explosionLoc, int chargeCount) {
        // Radio de knockback basado en configuración
        double knockbackRadius = configManager.getKnockbackBaseRadius() + 
                               (chargeCount * configManager.getKnockbackRadiusPerCharge());
        
        // Fuerza de knockback abrumadora basada en configuración
        double knockbackForce = configManager.getKnockbackBaseForce() + 
                               (chargeCount * configManager.getKnockbackForcePerCharge());
        
        // Obtener todas las entidades vivas en el radio
        Collection<LivingEntity> nearbyEntities = explosionLoc.getWorld().getLivingEntities();
        
        for (LivingEntity entity : nearbyEntities) {
            if (entity.getLocation().distance(explosionLoc) <= knockbackRadius) {
                // Calcular dirección del knockback (alejándose de la explosión)
                Vector direction = entity.getLocation().toVector().subtract(explosionLoc.toVector());
                
                if (direction.length() > 0) {
                    direction.normalize();
                    
                    // Aplicar knockback masivo con componente vertical configurable
                    Vector knockback = direction.multiply(knockbackForce);
                    knockback.setY(knockbackForce * configManager.getKnockbackVerticalRatio());
                    
                    // Aplicar velocidad inmediatamente
                    entity.setVelocity(knockback);
                    
                    // Si es un jugador, añadir efectos adicionales configurables
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        // Añadir impulso extra para jugadores basado en configuración
                        player.setVelocity(player.getVelocity().add(new Vector(0, configManager.getKnockbackPlayerBoost(), 0)));
                    }
                }
            }
        }
    }
    
    private void startExplosionProcessor() {
        explosionProcessorTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            explosionsThisTick.set(0);
            
            // Procesar explosiones pendientes
            int processed = 0;
            int maxPerTick = configManager.getMaxExplosionsPerTick();
            
            while (!pendingExplosions.isEmpty() && processed < maxPerTick) {
                PendingExplosion pending = pendingExplosions.poll();
                if (pending != null && pending.windCharge.isValid()) {
                    createMassiveExplosionWithKnockback(pending.windCharge);
                    pending.windCharge.remove();
                    processed++;
                }
            }
        }, 1L, 1L); // Cada tick
    }
    
    public void cleanup() {
        if (explosionProcessorTask != null) {
            explosionProcessorTask.cancel();
        }
        pendingExplosions.clear();
    }
    
    private static class PendingExplosion {
        final WindCharge windCharge;
        final int chargeCount;
        
        PendingExplosion(WindCharge windCharge, int chargeCount) {
            this.windCharge = windCharge;
            this.chargeCount = chargeCount;
        }
    }
}
