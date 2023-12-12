package net.gensokyoreimagined.gensoujank;

import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.bukkit.Bukkit;
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
            CraftPlayer oldPlayer = (CraftPlayer)event.getPlayer();
            oldPlayer.saveData();
            TouhouPlayer player = new TouhouPlayer(((CraftWorld)oldPlayer.getWorld()).getHandle().getServer(),((CraftWorld)oldPlayer.getWorld()).getHandle(),oldPlayer.getProfile(), oldPlayer.getHandle().clientInformation());
            player.connection=oldPlayer.getHandle().connection;
            player.connection.player=player;
            oldPlayer.getHandle().discard();
            PlayerList playerList = ((CraftWorld)oldPlayer.getWorld()).getHandle().getServer().getPlayerList();
            ((CraftWorld)oldPlayer.getWorld()).getHandle().getServer().getPlayerList().remove((oldPlayer).getHandle());
            ((CraftWorld)oldPlayer.getWorld()).getHandle().getServer().getPlayerList().placeNewPlayer((oldPlayer).getHandle().connection.connection,player, new CommonListenerCookie((oldPlayer).getProfile(), 0, (oldPlayer).getHandle().clientInformation()));
            playerList.respawn(player, ((CraftWorld)oldPlayer.getWorld()).getHandle().getLevel(), true, oldPlayer.getLocation(),false,PlayerRespawnEvent.RespawnReason.PLUGIN);
        }, 0L);

    }

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
