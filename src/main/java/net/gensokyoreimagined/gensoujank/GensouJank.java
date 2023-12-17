package net.gensokyoreimagined.gensoujank;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class GensouJank extends JavaPlugin implements Listener {
    @Override
    public void onEnable(){
        TouhouHitboxes touhouHitboxes = new TouhouHitboxes(this);
        Objects.requireNonNull(getCommand("touhouhitbox")).setExecutor(touhouHitboxes);
        getServer().getPluginManager().registerEvents(touhouHitboxes,this);

        ProtocolManager lib = ProtocolLibrary.getProtocolManager();
        lib.addPacketListener(new TouhouFocusListener(this, ListenerPriority.NORMAL, PacketType.Play.Client.ENTITY_ACTION));
    }
}