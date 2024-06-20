package net.gensokyoreimagined.gensoujank;

import com.mojang.math.Transformation;
import net.gensokyoreimagined.gensoujankmod.TouhouPlayers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.entity.CraftBlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class TouhouHitboxes implements CommandExecutor, Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = (CraftPlayer) event.getPlayer();

        var query = TouhouPlayers.players.get(player.getUniqueId());

        if (query != null) {
            if (query.debugTask != null) {
                query.debugTask.cancel();
            }

            if (query.hitbox != null) {
                query.hitbox.remove();
            }

            TouhouPlayers.players.remove(player.getUniqueId());
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

        var query = TouhouPlayers.players.get(player.getUniqueId());

        if (query != null) {
            query.bossMode = (args[1].equalsIgnoreCase("true"));

            if (args.length > 2) {
                if (args[2].equalsIgnoreCase("true") && query.debugTask == null) {
                    // Debug mode: show hitbox using a block display entity (glass block)

                    var world = player.getWorld();
                    var box = player.getBoundingBox();
                    var hitboxLocation = new Location(world, box.getMinX(), box.getMinY(), box.getMinZ());
                    var hitbox = (CraftBlockDisplay) world.spawnEntity(hitboxLocation, EntityType.BLOCK_DISPLAY);
                    query.hitbox = hitbox;

                    hitbox.setVisibleByDefault(false);
                    hitbox.setTeleportDuration(1);
                    player.showEntity(GensouJank.getInstance(), hitbox);

                    hitbox.setBlock(Material.GLASS.createBlockData());

                    // I do not particularly like Bukkit's API, soooo
                    hitbox.getHandle().setTransformation(new Transformation(
                            null,
                            null,
                            new Vector3f((float) box.getWidthX(), (float) box.getHeight(), (float) box.getWidthZ()),
                            null
                    ));

                    query.debugTask = Bukkit.getScheduler().runTaskTimer(GensouJank.getInstance(), () -> {
                        var timerBox = player.getBoundingBox();
                        var tHitBox = query.hitbox;

                        tHitBox.getHandle().setTransformation(new Transformation(
                            null,
                            null,
                            new Vector3f((float) timerBox.getWidthX(), (float) timerBox.getHeight(), (float) timerBox.getWidthZ()),
                            null
                        ));

                        var onGround = player.isOnGround();
                        // compensate for player movement - step one tick ahead
                        var velocityX = query.lastBoundingBox == null ? 0 : timerBox.getMinX() - query.lastBoundingBox.getMinX();
                        var velocityY = query.lastBoundingBox == null ? 0 : timerBox.getMinY() - query.lastBoundingBox.getMinY();
                        var velocityZ = query.lastBoundingBox == null ? 0 : timerBox.getMinZ() - query.lastBoundingBox.getMinZ();

                        query.hitbox.teleport(new Location(
                            player.getWorld(), timerBox.getMinX() + 1 * velocityX, timerBox.getMinY() + (onGround ? 0 : 1 * velocityY), timerBox.getMinZ() + 1 * velocityZ
                        ));

                        query.lastBoundingBox = timerBox;

                        // Bukkit.getLogger().info("[GensouJank] " + player.getName() + " position: " + player.getLocation());
                    }, 0L, 1L);
                } else if (args[2].equalsIgnoreCase("false") && query.debugTask != null) {
                    query.debugTask.cancel();
                    query.debugTask = null;
                    query.hitbox.remove();
                    query.hitbox = null;
                }
            }

            if (args.length > 3) {
                try {
                    query.adjustY = Double.parseDouble(args[3]);
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
            sender.sendMessage(
                Component
                    .text()
                    .append(Component.text().color(NamedTextColor.RED).content("Player "))
                    .append(Component.text().color(NamedTextColor.WHITE).content(player.getName()))
                    .append(Component.text().color(NamedTextColor.RED).content(" not found"))
                    .build()
            );
        }

        return true;
    }
}

