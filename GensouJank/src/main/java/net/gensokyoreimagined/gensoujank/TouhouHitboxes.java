package net.gensokyoreimagined.gensoujank;

import com.mojang.math.Transformation;
import net.gensokyoreimagined.gensoujankmod.TouhouPlayer;
import net.gensokyoreimagined.gensoujankmod.ITouhouPlayer;
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

        if (!(player.getHandle() instanceof ITouhouPlayer)) {
            Bukkit.getLogger().warning("[GensouJank] Player " + player.getName() + " is not a TouhouPlayer, but " + player.getHandle().getClass());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = (CraftPlayer) event.getPlayer();

        if (player.getHandle() instanceof ITouhouPlayer touhouPlayer) {
            if (touhouPlayer.getDebugTask() != null) {
                touhouPlayer.getDebugTask().cancel();
                touhouPlayer.setDebugTask(null);
            }

            if (touhouPlayer.getHitboxDisplay() != null) {
                touhouPlayer.getHitboxDisplay().remove();
                touhouPlayer.setHitboxDisplay(null);
            }

            if (touhouPlayer.isBossMode()) {
                // Log out and fix collision box
                touhouPlayer.setBossMode(false);
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

        if(player.getHandle() instanceof ITouhouPlayer touhouPlayer) {
            touhouPlayer.setBossMode(args[1].equalsIgnoreCase("true"));

            if (args.length > 2) {
                if (args[2].equalsIgnoreCase("true") && touhouPlayer.getDebugTask() == null) {
                    // Debug mode: show hitbox using a block display entity (glass block)

                    var world = player.getWorld();
                    var box = player.getBoundingBox();
                    var hitboxLocation = new Location(world, box.getMinX(), box.getMinY(), box.getMinZ());
                    var hitbox = (CraftBlockDisplay) world.spawnEntity(hitboxLocation, EntityType.BLOCK_DISPLAY);
                    touhouPlayer.setHitboxDisplay(hitbox);

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

                    var debugTask = Bukkit.getScheduler().runTaskTimer(GensouJank.getInstance(), () -> {
                        var timerBox = player.getBoundingBox();
                        var tHitBox = touhouPlayer.getHitboxDisplay();

                        tHitBox.getHandle().setTransformation(new Transformation(
                            null,
                            null,
                            new Vector3f((float) timerBox.getWidthX(), (float) timerBox.getHeight(), (float) timerBox.getWidthZ()),
                            null
                        ));

                        var onGround = player.isOnGround();
                        // compensate for player movement - step one tick ahead
                        var velocityX = touhouPlayer.getLastBoundingBox() == null ? 0 : timerBox.getMinX() - touhouPlayer.getLastBoundingBox().getMinX();
                        var velocityY = touhouPlayer.getLastBoundingBox() == null ? 0 : timerBox.getMinY() - touhouPlayer.getLastBoundingBox().getMinY();
                        var velocityZ = touhouPlayer.getLastBoundingBox() == null ? 0 : timerBox.getMinZ() - touhouPlayer.getLastBoundingBox().getMinZ();

                        touhouPlayer.getHitboxDisplay().teleport(new Location(
                            touhouPlayer.getBukkitEntity().getWorld(), timerBox.getMinX() + 1 * velocityX, timerBox.getMinY() + (onGround ? 0 : 1 * velocityY), timerBox.getMinZ() + 1 * velocityZ
                        ));

                        touhouPlayer.setLastBoundingBox(timerBox);

                        // Bukkit.getLogger().info("[GensouJank] " + player.getName() + " position: " + player.getLocation());
                    }, 0L, 1L);

                    touhouPlayer.setDebugTask(debugTask);
                } else if (args[2].equalsIgnoreCase("false") && touhouPlayer.getDebugTask() != null) {
                    touhouPlayer.getDebugTask().cancel();
                    touhouPlayer.setDebugTask(null);
                    touhouPlayer.getHitboxDisplay().remove();
                    touhouPlayer.setHitboxDisplay(null);
                }
            }

            if (args.length > 3) {
                try {
                    TouhouPlayer.setAdjustY(Double.parseDouble(args[3]));
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
            throw new IllegalStateException("Player is " + player.getHandle().getClass()+ " instead of TouhouPlayer");
        }

        return true;
    }
}

