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
                if (args[2].equalsIgnoreCase("true") && query.hitbox == null) {
                    // Debug mode: show hitbox using a block display entity (glass block)

                    var world = player.getWorld();
                    var box = player.getBoundingBox();
                    var hitboxLocation = new Location(world, box.getMinX(), box.getMinY(), box.getMinZ());
                    var hitbox = (CraftBlockDisplay) world.spawnEntity(hitboxLocation, EntityType.BLOCK_DISPLAY);
                    query.hitbox = hitbox;

                    // Only show hitbox to the player
                    hitbox.setVisibleByDefault(false);
                    player.showEntity(GensouJank.getInstance(), hitbox);

                    // Set hitbox to spawner block
                    hitbox.setBlock(Material.SPAWNER.createBlockData());

                    // I do not particularly like Bukkit's API, soooo
                    hitbox.getHandle().setTransformation(new Transformation(
                            new Vector3f((float) -player.getWidth() / 2.0f, (float) -box.getHeight(), (float) -player.getWidth() / 2.0f),
                            null,
                            new Vector3f((float) box.getWidthX(), (float) box.getHeight(), (float) box.getWidthZ()),
                            null
                    ));

                    // Make the hitbox follow the player
                    player.addPassenger(hitbox);
                } else if (args[2].equalsIgnoreCase("false") && query.hitbox != null) {
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

