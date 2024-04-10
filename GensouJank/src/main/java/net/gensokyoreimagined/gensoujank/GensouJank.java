package net.gensokyoreimagined.gensoujank;

import net.gensokyoreimagined.gensoujankmod.ITouhouPlayer;
import net.gensokyoreimagined.gensoujankmod.TouhouPlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class GensouJank extends JavaPlugin implements Listener {
    private static GensouJank instance;

    public static GensouJank getInstance() {
        return instance;
    }

    public FileConfiguration config = getConfig();

    @Override
    public void onEnable(){
        instance = this;

        config.addDefault("hitbox-y-offset", 1.2);
        // config.addDefault("debug-mode", false);
        config.options().copyDefaults(true);
        saveConfig();

        TouhouPlayer.setAdjustY(config.getDouble("hitbox-y-offset"));

        TouhouHitboxes touhouHitboxes = new TouhouHitboxes(this);
        TouhouHitboxesTabCompleter touhouHitboxesTabCompleter = new TouhouHitboxesTabCompleter();
        var command = Objects.requireNonNull(getCommand("touhouhitbox"));
        command.setExecutor(touhouHitboxes);
        command.setTabCompleter(touhouHitboxesTabCompleter);
        getServer().getPluginManager().registerEvents(touhouHitboxes,this);
    }
}