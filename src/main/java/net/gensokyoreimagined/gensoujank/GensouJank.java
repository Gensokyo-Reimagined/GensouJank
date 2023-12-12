package net.gensokyoreimagined.gensoujank;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class GensouJank extends JavaPlugin implements Listener {
    @Override
    public void onEnable(){
        TouhouHitboxes touhouHitboxes = new TouhouHitboxes(this);
        getCommand("touhouhitbox").setExecutor(touhouHitboxes);
        getServer().getPluginManager().registerEvents(touhouHitboxes,this);

    }
}