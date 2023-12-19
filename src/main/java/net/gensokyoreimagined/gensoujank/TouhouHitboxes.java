package net.gensokyoreimagined.gensoujank;

import io.papermc.paper.chunk.system.entity.EntityLookup;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class TouhouHitboxes implements CommandExecutor, Listener {
    private final GensouJank gensouJank;

    private static final HashMap<String, Field> playerListFields = new HashMap<>();
    private static final HashMap<String, Field> entityLookupFields = new HashMap<>();

    public TouhouHitboxes(GensouJank gensouJank){
        this.gensouJank=gensouJank;
    }

    static {
        ReflectionJank.getFields(DedicatedPlayerList.class).forEach(field -> {
            field.setAccessible(true);
            playerListFields.put(field.getName(), field);
        });

        // oops, Paper specific uhhh (maybe replace with NMS depending on runtime?)
        ReflectionJank.getFields(EntityLookup.class).forEach(field -> {
            field.setAccessible(true);
            entityLookupFields.put(field.getName(), field);
        });
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event){
        if(((CraftPlayer)event.getPlayer()).getHandle() instanceof TouhouPlayer){
            return;
        }

        Bukkit.getScheduler().runTaskLater(gensouJank, () -> {
            Bukkit.getLogger().info("[GensouJank] Player " + event.getPlayer().getName() + " is not a TouhouPlayer, converting...");

            // Biggest problem: this breaks reloading the plugin without restarting the server
            // Also, collision detection is done both client-side and server-side, so this doesn't actually work (or at least, with rubberbanding)
            // Also, server thinks player is in flight since collision does not touch the ground (but client-side, it does) - luckily we're talking about Touhou soooooo
            // Also, logging in sets the player position a few units below the actual position, so we have to teleport the player to the actual position
            // The easiest way at the moment is to rewrite the bytecode of ServerPlayer to call our makeBoundingBox, doing it fairly doesn't help since the player is already spawned
            // ^^ lol what if we did something more jank to hijack the PlayerList

            CraftPlayer oldPlayer = (CraftPlayer) event.getPlayer();
            // oldPlayer.saveData();

            TouhouPlayer player = new TouhouPlayer(oldPlayer);

            var playerList = ((CraftWorld) oldPlayer.getWorld()).getHandle().getServer().getPlayerList();

            var players = playerList.players;
            var index = players.indexOf(oldPlayer.getHandle());

            if (index == -1) {
                Bukkit.getLogger().warning("[GensouJank] Player " + event.getPlayer().getName() + " not found in player list, this is probably bad");
            } else {
                players.set(index, player);
            }

            if (playerListFields.containsKey("playersByName")) {
                var playersByName = ReflectionJank.<Map<String, ServerPlayer>>getValue(playerList, playerListFields.get("playersByName"));
                playersByName.put(player.getScoreboardName().toLowerCase(Locale.ROOT), player);
            }

            // Doesn't exist on Paper?
            if (playerListFields.containsKey("playersByUUID")) {
                var playersByUUID = ReflectionJank.<Map<UUID, ServerPlayer>>getValue(playerList, playerListFields.get("playersByUUID"));
                playersByUUID.put(player.getUUID(), player);
            }

            var server = playerList.getServer();
            var level = server.getLevel(player.getRespawnDimension());

            if (level == null) {
                Bukkit.getLogger().warning("[GensouJank] Player " + event.getPlayer().getName() + " respawn dimension " + player.getRespawnDimension() + " not found, this is probably bad");
            } else {
                var entities = level.getEntityLookup();
                // Again, Paper-specific
                var entityById = ReflectionJank.<Int2ReferenceOpenHashMap<Entity>>getValue(entities, entityLookupFields.get("entityById"));
                var entityByUUID = ReflectionJank.<Object2ReferenceOpenHashMap<UUID, Entity>>getValue(entities, entityLookupFields.get("entityByUUID"));
                entityById.put(player.getId(), player);
                entityByUUID.put(player.getUUID(), player);

                try {
                    var callbackClass = Class.forName("io.papermc.paper.chunk.system.entity.EntityLookup$EntityCallback");
                    var callbackConstructor = callbackClass.getDeclaredConstructor(EntityLookup.class, net.minecraft.world.entity.Entity.class);
                    callbackConstructor.setAccessible(true);
                    var callback = (EntityInLevelCallback) callbackConstructor.newInstance(entities, player);

                    player.setLevelCallback(callback);
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                         IllegalAccessException | InvocationTargetException e) {
                    Bukkit.getLogger().warning("[GensouJank] Player " + event.getPlayer().getName() + " failed to get callback class, this is probably bad\n" + e);
                }
            }

            playerList.respawn(player, ((CraftWorld) oldPlayer.getWorld()).getHandle().getLevel(), true, oldPlayer.getLocation(), false, PlayerRespawnEvent.RespawnReason.PLUGIN);
            oldPlayer.getHandle().discard();

            Bukkit.getScheduler().runTaskTimer(gensouJank, () -> {
                var box = player.getBoundingBox();
                var world = event.getPlayer().getWorld();
                world.spawnParticle(Particle.REDSTONE, new Location(world, box.minX, box.minY, box.minZ), 1, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 1));
                world.spawnParticle(Particle.REDSTONE, new Location(world, box.maxX, box.maxY, box.maxX), 1, 0, 0, 0, 0, new Particle.DustOptions(Color.BLUE, 1));
            }, 0L, 10L);
        }, 0L);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length < 2 || !(args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false"))) {
            sender.sendMessage(
                Component
                    .text()
                    .color(NamedTextColor.RED)
                    .content("Usage: /touhouhitbox <player> <true|false>")
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
