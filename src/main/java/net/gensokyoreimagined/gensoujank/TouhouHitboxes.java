package net.gensokyoreimagined.gensoujank;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class TouhouHitboxes implements CommandExecutor, Listener {
    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        var player = (CraftPlayer) event.getPlayer();

        if (!(player.getHandle() instanceof TouhouPlayer)) {
            Bukkit.getLogger().warning("[GensouJank] Player " + player.getName() + " is not a TouhouPlayer, but " + player.getHandle().getClass());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = (CraftPlayer) event.getPlayer();

        if (player.getHandle() instanceof TouhouPlayer touhouPlayer) {
            if (touhouPlayer.debugTask != null) {
                touhouPlayer.debugTask.cancel();
                touhouPlayer.debugTask = null;
            }
        } else {
            Bukkit.getLogger().warning("[GensouJank] Player " + player.getName() + " is not a TouhouPlayer, but " + player.getHandle().getClass());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length < 2 || !(args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false"))) {
            sender.sendMessage(
                Component
                    .text()
                    .color(NamedTextColor.RED)
                    .content("Usage: /touhouhitbox <player> <true|false> [debug] [adjustY]")
                    .build()
            );

            return true;
        }

        var player = (CraftPlayer) Bukkit.getPlayerExact(args[0]);

        if (player == null) {
            sender.sendMessage(
                Component
                    .text()
                    .append(Component.text().color(NamedTextColor.RED).content("Player "))
                    .append(Component.text().color(NamedTextColor.WHITE).content(args[0]))
                    .append(Component.text().color(NamedTextColor.RED).content(" not found"))
                    .build()
            );
            return true;
        }

        if(player.getHandle() instanceof TouhouPlayer touhouPlayer) {
            touhouPlayer.bossMode=args[1].equalsIgnoreCase("true");

            if (args.length > 2) {
                if (args[2].equalsIgnoreCase("true") && touhouPlayer.debugTask == null) {
                    touhouPlayer.debugTask = Bukkit.getScheduler().runTaskTimer(GensouJank.getInstance(), () -> {
                        var box = player.getBoundingBox();
                        var world = player.getWorld();
                        var center = box.getCenter();
                        world.spawnParticle(Particle.REDSTONE, new Location(world, center.getX(), center.getY(), center.getZ()), 1, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 1));
                        world.spawnParticle(Particle.REDSTONE, new Location(world, box.getMinX(), box.getMinY(), box.getMinZ()), 1, 0, 0, 0, 0, new Particle.DustOptions(Color.BLUE, 1));
                        world.spawnParticle(Particle.REDSTONE, new Location(world, box.getMaxX(), box.getMaxY(), box.getMaxZ()), 1, 0, 0, 0, 0, new Particle.DustOptions(Color.GREEN, 1));
                    }, 0L, 10L);
                } else if (args[2].equalsIgnoreCase("false") && touhouPlayer.debugTask != null) {
                    touhouPlayer.debugTask.cancel();
                    touhouPlayer.debugTask = null;
                }
            }

            if (args.length > 3) {
                try {
                    touhouPlayer.adjustY = Double.parseDouble(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(
                        Component
                            .text()
                            .append(Component.text().color(NamedTextColor.RED).content("Invalid number: "))
                            .append(Component.text().color(NamedTextColor.WHITE).content(args[3]))
                            .build()
                    );
                    return true;
                }
            }

            sender.sendMessage(
                Component
                    .text()
                    .append(Component.text().color(NamedTextColor.GREEN).content("Boss mode for "))
                    .append(Component.text().color(NamedTextColor.WHITE).content(player.getName()))
                    .append(Component.text().color(NamedTextColor.GREEN).content(" set to "))
                    .append(Component.text().color(NamedTextColor.WHITE).content(args[1]))
                    .build()
            );
        } else {
            throw new IllegalStateException("Player is " + player.getHandle().getClass()+ " instead of " + TouhouPlayer.class);
        }

        return true;
    }
}
