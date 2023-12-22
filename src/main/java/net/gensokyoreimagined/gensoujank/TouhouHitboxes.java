package net.gensokyoreimagined.gensoujank;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class TouhouHitboxes implements CommandExecutor, Listener {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length<2) return false;
        if(!(args[1].equalsIgnoreCase("true")||args[1].equalsIgnoreCase("false"))) return false;
        Player player = Bukkit.getPlayerExact(args[0]);
        if(player==null) return false;
        if(((CraftPlayer)player).getHandle() instanceof TouhouPlayer touhouPlayer) {
            touhouPlayer.bossMode=args[1].equalsIgnoreCase("true");
        }else{
            throw new IllegalStateException("Player is "+((CraftPlayer)player).getHandle().getClass()+" instead of "+TouhouPlayer.class);
        }
        return true;
    }
}
