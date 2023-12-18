package net.gensokyoreimagined.gensoujank;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

public class TouhouHitboxes implements CommandExecutor, Listener {
    private final GensouJank gensouJank;

    public TouhouHitboxes(GensouJank gensouJank){
        this.gensouJank=gensouJank;
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

            CraftPlayer oldPlayer = (CraftPlayer) event.getPlayer();
            oldPlayer.saveData();

            // TouhouPlayer player = new TouhouPlayer(
            //         ((CraftWorld)oldPlayer.getWorld()).getHandle().getServer(),
            //         ((CraftWorld)oldPlayer.getWorld()).getHandle(),
            //         oldPlayer.getProfile(),
            //         oldPlayer.getHandle().clientInformation());
            TouhouPlayer player = new TouhouPlayer(oldPlayer);

            // player.connection=oldPlayer.getHandle().connection;
            // player.connection.player=player;
            // oldPlayer.getHandle().discard();
            // PlayerList playerList = ((CraftWorld)oldPlayer.getWorld()).getHandle().getServer().getPlayerList();
            // Bukkit.getLogger().info("[GensouJank] Removing " + event.getPlayer().getName() + " from player list...");
            // ((CraftWorld)oldPlayer.getWorld()).getHandle().getServer().getPlayerList().remove((oldPlayer).getHandle());
            // Bukkit.getLogger().info("[GensouJank] Adding " + event.getPlayer().getName() + " to player list...");
            // ((CraftWorld)oldPlayer.getWorld()).getHandle().getServer().getPlayerList().placeNewPlayer((oldPlayer).getHandle().connection.connection,player, new CommonListenerCookie((oldPlayer).getProfile(), 0, (oldPlayer).getHandle().clientInformation()));
            // Bukkit.getLogger().info("[GensouJank] Respawning " + event.getPlayer().getName() + "...");
            // playerList.respawn(player, ((CraftWorld)oldPlayer.getWorld()).getHandle().getLevel(), true, oldPlayer.getLocation(),false,PlayerRespawnEvent.RespawnReason.PLUGIN);
            //
            // Bukkit.getScheduler().runTaskTimer(gensouJank, () -> {
            //     var box = player.getBoundingBox();
            //     var world = event.getPlayer().getWorld();
            //     world.spawnParticle(Particle.REDSTONE, new Location(world, box.minX, box.minY, box.minZ), 1, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 1));
            //     world.spawnParticle(Particle.REDSTONE, new Location(world, box.maxX, box.maxY, box.maxX), 1, 0, 0, 0, 0, new Particle.DustOptions(Color.BLUE, 1));
            // }, 0L, 10L);
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
