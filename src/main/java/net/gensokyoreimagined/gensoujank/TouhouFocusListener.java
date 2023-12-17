package net.gensokyoreimagined.gensoujank;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.plugin.Plugin;

public class TouhouFocusListener extends PacketAdapter {

    public TouhouFocusListener(Plugin plugin, ListenerPriority listenerPriority, PacketType... types) {
        super(plugin, listenerPriority, types);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        var player = event.getPlayer();
        var packet = event.getPacket();

        try
        {
            var action = packet.getPlayerActions().read(0);

            if (action == EnumWrappers.PlayerAction.START_SNEAKING) { // Start sneaking/focusing
//                Bukkit.getLogger().info("[GensouJank] Player " + player.getName() + " now focusing");
                event.setCancelled(true);
            } else if (action == EnumWrappers.PlayerAction.STOP_SNEAKING) { // Stop sneaking/focusing
//                Bukkit.getLogger().info("[GensouJank] Player " + player.getName() + " unfocusing");
                // don't need to cancel this, but we can if we want to
            }

            // Because we're using a packet listener, we can't use the TouhouPlayer class
            var bukkitPlayer = (CraftPlayer) Bukkit.getPlayerExact(player.getName());
            if (bukkitPlayer != null && bukkitPlayer.getHandle() instanceof TouhouPlayer touhouPlayer) {
//                Bukkit.getLogger().info("[GensouJank] Player " + player.getName() + " is a TouhouPlayer, setting boss mode to " + (action == EnumWrappers.PlayerAction.START_SNEAKING));
                touhouPlayer.bossMode = (action == EnumWrappers.PlayerAction.START_SNEAKING);
            }
        } catch (FieldAccessException e)
        {
            // ignored, most likely a 1.20 issue
        }
    }
}
