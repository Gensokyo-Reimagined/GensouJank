package net.gensokyoreimagined.gensoujank;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TouhouHitboxes implements CommandExecutor {
    private final Set<UUID> fakedPlayers =new HashSet<>();
    public TouhouHitboxes(GensouJank gensouJank, ProtocolManager protocolManager){
        protocolManager.addPacketListener(new PacketAdapter(
                gensouJank,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.ENTITY_METADATA
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if((fakedPlayers.contains(event.getPlayer().getUniqueId())&&event.getPacket().getEntityModifier(event).size()==0) || fakedPlayers.contains(event.getPacket().getEntityModifier(event).read(0).getUniqueId())) {

                    List<WrappedDataValue> list = event.getPacket().getDataValueCollectionModifier().read(0);
                    for (WrappedDataValue value : list) {
                        if (value.getSerializer().getType().getName().toLowerCase().contains("entitypose")) {
                            value.setValue(EnumWrappers.EntityPose.STANDING);
                        }
                    }
                }
            }
        });
        protocolManager.addPacketListener(new PacketAdapter(
                gensouJank,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.ENTITY_TELEPORT
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if((event.getPacket().getEntityModifier(event).size()>0&&event.getPacket().getEntityModifier(event).read(0)!=null)&&fakedPlayers.contains(event.getPacket().getEntityModifier(event).read(0).getUniqueId())) {
                    event.getPacket().getDoubles().write(1, (event.getPacket().getDoubles().read(1)-1));
                }
            }
        });
        protocolManager.addPacketListener(new PacketAdapter(
                gensouJank,
                ListenerPriority.NORMAL,
                PacketType.Play.Client.POSITION, PacketType.Play.Client.POSITION_LOOK
        ) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if(fakedPlayers.contains(event.getPlayer().getUniqueId())) {
                    event.getPacket().getDoubles().write(1, event.getPacket().getDoubles().read(1) + 1);
                }
            }
        });
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length<2) return false;
        if(!(args[1].equalsIgnoreCase("true")||args[1].equalsIgnoreCase("false"))) return false;
        Player player = Bukkit.getPlayerExact(args[0]);
        if(player==null) return false;
        if (args[1].equalsIgnoreCase("true")) {
            fakedPlayers.add(player.getUniqueId());
            player.setPose(Pose.SWIMMING, true);
        } else {
            fakedPlayers.remove(player.getUniqueId());
            player.setPose(Pose.STANDING, false);
        }
        return true;
    }
}
