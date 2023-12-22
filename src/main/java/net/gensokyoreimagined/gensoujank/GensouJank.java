package net.gensokyoreimagined.gensoujank;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class GensouJank extends JavaPlugin implements Listener {
    private static GensouJank instance;

    public static GensouJank getInstance() {
        return instance;
    }

    @Override
    public void onEnable(){
        instance = this;

        TouhouHitboxes touhouHitboxes = new TouhouHitboxes();
        Objects.requireNonNull(getCommand("touhouhitbox")).setExecutor(touhouHitboxes);
        getServer().getPluginManager().registerEvents(touhouHitboxes,this);
    }
}