package com.minecraft.windcharge;

import org.bukkit.entity.Entity;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public class WindChargeListener implements Listener {
    
    private final WindChargeManager windChargeManager;
    
    public WindChargeListener(WindChargeOptimizer plugin, WindChargeManager windChargeManager) {
        this.windChargeManager = windChargeManager;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof WindCharge)) {
            return;
        }
        
        WindCharge windCharge = (WindCharge) event.getEntity();
        
        // Verificar si es una carga combinada
        if (windChargeManager.getChargeCount(windCharge) > 1) {
            // Cancelar el evento de impacto normal para manejar la explosión personalizada
            event.setCancelled(true);
            
            // Crear explosión con potencia equivalente
            double explosionPower = windChargeManager.getExplosionPower(windCharge);
            windCharge.getWorld().createExplosion(
                windCharge.getLocation(),
                (float) explosionPower,
                false, // No crear fuego
                true, // Romper bloques
                windCharge // Entidad fuente
            );
            
            // Eliminar la entidad
            windCharge.remove();
            
            if (windChargeManager instanceof WindChargeManager) {
                // Remover del mapa de cargas combinadas
                // Esto se implementaría en WindChargeManager
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        
        if (!(entity instanceof WindCharge)) {
            return;
        }
        
        // Si es una carga combinada, ajustar la potencia de explosión
        int chargeCount = windChargeManager.getChargeCount(entity);
        if (chargeCount > 1) {
            // La explosión ya fue manejada en onProjectileHit, pero por seguridad
            // ajustamos los bloques afectados si es necesario
            if (windChargeManager.getExplosionPower(entity) > 10.0) {
                // Limitar el radio de bloques afectados para evitar lag extremo
                int maxBlocks = (int) (chargeCount * 8); // 8 bloques por carga como máximo
                if (event.blockList().size() > maxBlocks) {
                    event.blockList().subList(maxBlocks, event.blockList().size()).clear();
                }
            }
        }
    }
}
