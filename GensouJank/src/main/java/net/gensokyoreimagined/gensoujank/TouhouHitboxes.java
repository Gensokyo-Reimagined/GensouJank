package net.gensokyoreimagined.gensoujank;

import com.mojang.math.Transformation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftBlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

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

            if (touhouPlayer.hitbox != null) {
                touhouPlayer.hitbox.remove();
            }

            if (touhouPlayer.bossMode) {
                // Log out and fix collision box
                touhouPlayer.bossMode = false;
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
                    // Debug mode: show hitbox using a block display entity (glass block)

                    var world = player.getWorld();
                    var box = player.getBoundingBox();
                    var hitboxLocation = new Location(world, box.getMinX(), box.getMinY(), box.getMinZ());
                    touhouPlayer.hitbox = (CraftBlockDisplay) world.spawnEntity(hitboxLocation, EntityType.BLOCK_DISPLAY);

                    touhouPlayer.hitbox.setVisibleByDefault(false);
                    touhouPlayer.hitbox.setTeleportDuration(1);
                    player.showEntity(GensouJank.getInstance(), touhouPlayer.hitbox);

                    touhouPlayer.hitbox.setBlock(Material.GLASS.createBlockData());

                    // I do not particularly like Bukkit's API, soooo
                    touhouPlayer.hitbox.getHandle().setTransformation(new Transformation(
                            null,
                            null,
                            new Vector3f((float) box.getWidthX(), (float) box.getHeight(), (float) box.getWidthZ()),
                            null
                    ));

                    touhouPlayer.debugTask = Bukkit.getScheduler().runTaskTimer(GensouJank.getInstance(), () -> {
                        var timerBox = player.getBoundingBox();

                        touhouPlayer.hitbox.getHandle().setTransformation(new Transformation(
                            null,
                            null,
                            new Vector3f((float) timerBox.getWidthX(), (float) timerBox.getHeight(), (float) timerBox.getWidthZ()),
                            null
                        ));

                        var onGround = player.isOnGround();
                        // compensate for player movement - step one tick ahead
                        var velocityX = touhouPlayer.lastBoundingBox == null ? 0 : timerBox.getMinX() - touhouPlayer.lastBoundingBox.getMinX();
                        var velocityY = touhouPlayer.lastBoundingBox == null ? 0 : timerBox.getMinY() - touhouPlayer.lastBoundingBox.getMinY();
                        var velocityZ = touhouPlayer.lastBoundingBox == null ? 0 : timerBox.getMinZ() - touhouPlayer.lastBoundingBox.getMinZ();

                        touhouPlayer.hitbox.teleport(new Location(
                            touhouPlayer.getBukkitEntity().getWorld(), timerBox.getMinX() + 1 * velocityX, timerBox.getMinY() + (onGround ? 0 : 1 * velocityY), timerBox.getMinZ() + 1 * velocityZ
                        ));

                        touhouPlayer.lastBoundingBox = timerBox;

                        // Bukkit.getLogger().info("[GensouJank] " + player.getName() + " position: " + player.getLocation());
                    }, 0L, 1L);
                } else if (args[2].equalsIgnoreCase("false") && touhouPlayer.debugTask != null) {
                    touhouPlayer.debugTask.cancel();
                    touhouPlayer.debugTask = null;
                    touhouPlayer.hitbox.remove();
                }
            }

            if (args.length > 3) {
                try {
                    TouhouPlayer.adjustY = Double.parseDouble(args[3]);
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

