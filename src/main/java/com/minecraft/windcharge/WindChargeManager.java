package com.minecraft.windcharge;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WindChargeManager {
    
    private final WindChargeOptimizer plugin;
    private final ConfigManager configManager;
    private final Map<UUID, CombinedWindCharge> combinedCharges;
    private final NamespacedKey chargeCountKey;
    private final NamespacedKey combinedKey;
    private final NamespacedKey originalVelocityKey;
    private final NamespacedKey isStaticKey;
    
    public WindChargeManager(WindChargeOptimizer plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.combinedCharges = new ConcurrentHashMap<>();
        this.chargeCountKey = new NamespacedKey(plugin, "charge_count");
        this.combinedKey = new NamespacedKey(plugin, "combined_charge");
        this.originalVelocityKey = new NamespacedKey(plugin, "original_velocity");
        this.isStaticKey = new NamespacedKey(plugin, "is_static");
    }
    
    public void optimizeWindCharges() {
        for (World world : Bukkit.getWorlds()) {
            if (!configManager.isWorldEnabled(world.getName())) {
                continue;
            }
            
            // Recolectar todas las cargas de viento (normales y combinadas)
            List<WindCharge> allWindCharges = new ArrayList<>();
            List<WindCharge> combinedWindCharges = new ArrayList<>();
            
            for (Entity entity : world.getEntities()) {
                if (entity instanceof WindCharge) {
                    WindCharge windCharge = (WindCharge) entity;
                    allWindCharges.add(windCharge);
                    if (isCombinedCharge(windCharge)) {
                        combinedWindCharges.add(windCharge);
                    }
                }
            }
            
            if (configManager.autoAccumulate()) {
                // Modo de acumulación automática: añadir cargas a entidades combinadas existentes
                accumulateToExistingCharges(allWindCharges, combinedWindCharges);
            } else {
                // Modo tradicional: crear nuevos grupos
                List<WindCharge> normalCharges = new ArrayList<>();
                for (WindCharge charge : allWindCharges) {
                    if (!isCombinedCharge(charge)) {
                        normalCharges.add(charge);
                    }
                }
                
                if (normalCharges.size() >= configManager.getMinimumCharges()) {
                    Map<Location, List<WindCharge>> chargeGroups = groupNearbyCharges(normalCharges);
                    for (List<WindCharge> group : chargeGroups.values()) {
                        if (group.size() >= configManager.getMinimumCharges()) {
                            combineWindCharges(group);
                        }
                    }
                }
            }
            
            // Actualizar tags si es necesario
            if (configManager.updateTagContinuously()) {
                updateAllTags(combinedWindCharges);
            }
        }
    }
    
    private Map<Location, List<WindCharge>> groupNearbyCharges(List<WindCharge> windCharges) {
        Map<Location, List<WindCharge>> groups = new HashMap<>();
        double searchRadius = configManager.getSearchRadius();
        
        for (WindCharge charge : windCharges) {
            Location chargeLoc = charge.getLocation();
            boolean foundGroup = false;
            
            // Buscar grupo existente cercano
            for (Location groupLoc : groups.keySet()) {
                if (groupLoc.distance(chargeLoc) <= searchRadius) {
                    groups.get(groupLoc).add(charge);
                    foundGroup = true;
                    break;
                }
            }
            
            // Crear nuevo grupo si no se encontró uno cercano
            if (!foundGroup) {
                List<WindCharge> newGroup = new ArrayList<>();
                newGroup.add(charge);
                groups.put(chargeLoc, newGroup);
            }
        }
        
        return groups;
    }
    
    private void combineWindCharges(List<WindCharge> charges) {
        if (charges.isEmpty()) return;
        
        // Usar la primera carga como base para heredar propiedades
        WindCharge firstCharge = charges.get(0);
        Location spawnLocation = firstCharge.getLocation().clone();
        
        // Calcular potencia total
        int totalCharges = Math.min(charges.size(), configManager.getMaxChargesPerEntity());
        double explosionPower = totalCharges * 3.0;
        
        // Crear entidad combinada en la posición de la primera carga
        WindCharge combined = (WindCharge) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.WIND_CHARGE);
        
        // Heredar propiedades de la primera carga si está configurado
        if (configManager.inheritFirstChargeProperties()) {
            inheritChargeProperties(combined, firstCharge);
        }
        
        // Marcar como combinada y guardar conteo
        setCombinedCharge(combined, totalCharges);
        
        // Guardar referencia
        combinedCharges.put(combined.getUniqueId(), new CombinedWindCharge(combined, totalCharges, explosionPower));
        
        // Mostrar tag con número de cargas
        updateChargeTag(combined);
        
        // Eliminar cargas originales (excepto la primera que ya fue reemplazada)
        for (WindCharge charge : charges) {
            if (!charge.getUniqueId().equals(firstCharge.getUniqueId())) {
                charge.remove();
            }
        }
        
        // Efectos visuales y de sonido
        if (configManager.showParticles()) {
            spawnLocation.getWorld().spawnParticle(Particle.EXPLOSION, spawnLocation, 1, 0, 0, 0, 1.0);
        }
        
        if (configManager.playSound()) {
            spawnLocation.getWorld().playSound(spawnLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        }
        
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("Combinadas " + totalCharges + " cargas de viento en " + 
                                 spawnLocation.getWorld().getName() + " en (" + 
                                 spawnLocation.getBlockX() + ", " + spawnLocation.getBlockY() + ", " + spawnLocation.getBlockZ() + ")");
        }
    }
    
    private void inheritChargeProperties(WindCharge combined, WindCharge original) {
        // Heredar velocidad
        Vector velocity = original.getVelocity();
        combined.setVelocity(velocity);
        
        // Guardar si es estática (velocidad cero)
        boolean isStatic = velocity.length() < 0.01;
        combined.getPersistentDataContainer().set(isStaticKey, PersistentDataType.BYTE, (byte) (isStatic ? 1 : 0));
        
        // Guardar velocidad original para referencia futura
        String velocityString = velocity.getX() + "," + velocity.getY() + "," + velocity.getZ();
        combined.getPersistentDataContainer().set(originalVelocityKey, PersistentDataType.STRING, velocityString);
        
        // Heredar otras propiedades si es necesario
        if (original.getFireTicks() > 0) {
            combined.setFireTicks(original.getFireTicks());
        }
    }
    
    private Location calculateCenter(List<WindCharge> charges) {
        double x = 0, y = 0, z = 0;
        
        for (WindCharge charge : charges) {
            Location loc = charge.getLocation();
            x += loc.getX();
            y += loc.getY();
            z += loc.getZ();
        }
        
        int count = charges.size();
        return new Location(charges.get(0).getWorld(), x / count, y / count, z / count);
    }
    
    private void accumulateToExistingCharges(List<WindCharge> allCharges, List<WindCharge> combinedCharges) {
        double searchRadius = configManager.getSearchRadius();
        
        for (WindCharge combined : combinedCharges) {
            if (!combined.isValid()) continue;
            
            List<WindCharge> nearbyCharges = new ArrayList<>();
            Location combinedLoc = combined.getLocation();
            
            // Buscar cargas normales cercanas
            for (WindCharge charge : allCharges) {
                if (!charge.isValid() || isCombinedCharge(charge)) continue;
                
                if (charge.getLocation().distance(combinedLoc) <= searchRadius) {
                    nearbyCharges.add(charge);
                }
            }
            
            if (!nearbyCharges.isEmpty()) {
                // Añadir las cargas cercanas a la entidad combinada
                addToCombinedCharge(combined, nearbyCharges);
            }
        }
        
        // Si no hay cargas combinadas, crear nuevas como antes
        if (combinedCharges.isEmpty()) {
            List<WindCharge> normalCharges = new ArrayList<>();
            for (WindCharge charge : allCharges) {
                if (!isCombinedCharge(charge)) {
                    normalCharges.add(charge);
                }
            }
            
            if (normalCharges.size() >= configManager.getMinimumCharges()) {
                Map<Location, List<WindCharge>> chargeGroups = groupNearbyCharges(normalCharges);
                for (List<WindCharge> group : chargeGroups.values()) {
                    if (group.size() >= configManager.getMinimumCharges()) {
                        combineWindCharges(group);
                    }
                }
            }
        }
    }
    
    private void addToCombinedCharge(WindCharge combined, List<WindCharge> newCharges) {
        int currentCount = getChargeCount(combined);
        int newCount = Math.min(currentCount + newCharges.size(), configManager.getMaxChargesPerEntity());
        
        // Actualizar contador
        combined.getPersistentDataContainer().set(chargeCountKey, PersistentDataType.INTEGER, newCount);
        
        // Actualizar en el mapa
        UUID combinedId = combined.getUniqueId();
        if (combinedCharges.containsKey(combinedId)) {
            CombinedWindCharge existing = combinedCharges.get(combinedId);
            combinedCharges.put(combinedId, new CombinedWindCharge(combined, newCount, newCount * 3.0));
        }
        
        // Eliminar las cargas añadidas
        for (WindCharge charge : newCharges) {
            charge.remove();
        }
        
        // Actualizar tag
        updateChargeTag(combined);
        
        // Efectos visuales sutiles
        if (configManager.showParticles()) {
            combined.getWorld().spawnParticle(Particle.CRIT, combined.getLocation(), 3);
        }
        
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("Añadidas " + newCharges.size() + " cargas a entidad combinada. Total: " + newCount);
        }
    }
    
    private void updateAllTags(List<WindCharge> combinedCharges) {
        for (WindCharge charge : combinedCharges) {
            if (charge.isValid()) {
                updateChargeTag(charge);
            }
        }
    }
    
    private void updateChargeTag(WindCharge charge) {
        if (!configManager.showChargeTag()) return;
        
        int count = getChargeCount(charge);
        String tagText = "⚡ " + count + "x";
        
        // Usar el nombre de la entidad para mostrar el tag
        charge.setCustomName(tagText);
        charge.setCustomNameVisible(true);
    }
    
    private boolean isCombinedCharge(Entity entity) {
        return entity.getPersistentDataContainer().has(combinedKey, PersistentDataType.BYTE);
    }
    
    private void setCombinedCharge(WindCharge charge, int count) {
        charge.getPersistentDataContainer().set(combinedKey, PersistentDataType.BYTE, (byte) 1);
        charge.getPersistentDataContainer().set(chargeCountKey, PersistentDataType.INTEGER, count);
    }
    
    public int getChargeCount(Entity entity) {
        if (!isCombinedCharge(entity)) {
            return 1;
        }
        
        Integer count = entity.getPersistentDataContainer().get(chargeCountKey, PersistentDataType.INTEGER);
        return count != null ? count : 1;
    }
    
    public double getExplosionPower(Entity entity) {
        int count = getChargeCount(entity);
        return count * 3.0; // Potencia base por carga de viento
    }
    
    public void cleanup() {
        // Eliminar todas las cargas combinadas al desactivar el plugin
        for (CombinedWindCharge combined : combinedCharges.values()) {
            if (combined.getEntity().isValid()) {
                combined.getEntity().remove();
            }
        }
        combinedCharges.clear();
    }
    
    public static class CombinedWindCharge {
        private final WindCharge entity;
        private final int chargeCount;
        private final double explosionPower;
        
        public CombinedWindCharge(WindCharge entity, int chargeCount, double explosionPower) {
            this.entity = entity;
            this.chargeCount = chargeCount;
            this.explosionPower = explosionPower;
        }
        
        public WindCharge getEntity() { return entity; }
        public int getChargeCount() { return chargeCount; }
        public double getExplosionPower() { return explosionPower; }
    }
}
