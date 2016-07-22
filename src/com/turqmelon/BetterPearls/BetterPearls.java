package com.turqmelon.BetterPearls;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Creator: Devon
 * Project: BetterPearls
 */
public class BetterPearls extends JavaPlugin implements Listener {
    private Map<UUID, Location> throwOrigin = new HashMap<>();
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    // If the user is suffocating, kill the pearl
    @EventHandler
    public void onDamage(EntityDamageEvent event){
        Entity entity = event.getEntity();
        if ((entity instanceof Player)){
            Player player = (Player)entity;
            if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION){
                Entity seat = player.getVehicle();
                if ((seat instanceof EnderPearl)){
                    seat.remove();
                }
            }
        }
    }

    // If the user presses "shift" midair, kill the pearl
    @EventHandler
    public void onDismount(EntityDismountEvent event){
        if ((event.getDismounted() instanceof EnderPearl)){
            event.getDismounted().remove();
        }
    }

    // When an ender-pearl is thrown (and not cancelled), store the origin
    // In the case the user can't land, we'll teleport them back to this position
    @EventHandler(priority = EventPriority.MONITOR)
    public void onThrow(ProjectileLaunchEvent event){
        if (event.isCancelled())return;
        Projectile projectile = event.getEntity();
        if ((projectile instanceof EnderPearl)){
            EnderPearl pearl = (EnderPearl)projectile;
            if ((pearl.getShooter() instanceof Player)){
                Player shooter = (Player) pearl.getShooter();
                if (shooter.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR){
                    throwOrigin.put(shooter.getUniqueId(), shooter.getLocation());
                    pearl.setPassenger(shooter);
                }
                else{
                    event.setCancelled(true);
                    shooter.playSound(shooter.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
                    shooter.sendMessage(ChatColor.RED + "You must be on the ground to throw a pearl.");
                    shooter.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
                }
            }
        }
    }

    // Cancel teleporting using ender pearls
    // In the case that this event would normally be cancelled, teleport the user back to the origin
    // (Supports plugins that normally prevent ender-pearling to some locations.)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent event){
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL){
            if (event.isCancelled()){
                Player player = event.getPlayer();
                Location origin = throwOrigin.getOrDefault(player.getUniqueId(), null);
                if (origin != null){
                    player.teleport(origin);
                    player.sendMessage(ChatColor.RED + "You can't land there.");
                }
            }
            event.setCancelled(true);
        }

    }
}
