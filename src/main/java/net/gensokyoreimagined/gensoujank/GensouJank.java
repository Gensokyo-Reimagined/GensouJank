package net.gensokyoreimagined.gensoujank;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class GensouJank extends JavaPlugin implements Listener {

    private ProtocolManager protocolManager;
    @Override
    public void onEnable(){
        protocolManager = ProtocolLibrary.getProtocolManager();
        getCommand("touhouhitbox").setExecutor(new TouhouHitboxes(this,protocolManager));

    }
}