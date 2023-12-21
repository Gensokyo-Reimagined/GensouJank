package net.gensokyoreimagined.gensoujank;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;


public class TouhouPlayer extends ServerPlayer {

    private static final Set<String> commonFieldNames = new HashSet<>();
    private static final HashMap<String, Field> serverPlayerFieldMap = new HashMap<>();
    private static final HashMap<String, Field> touhouPlayerFieldMap = new HashMap<>();

    static {
        // Now invoking reflection magic, please watch warmly

        // TLDR: get all the property descriptors for ServerPlayer and TouhouPlayer, then get the common ones
        var serverPlayerFields = ReflectionJank.getFields(ServerPlayer.class);
        var touhouPlayerFields = ReflectionJank.getFields(TouhouPlayer.class);

        serverPlayerFields.forEach(field -> {
            if ((field.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == (Modifier.STATIC | Modifier.FINAL)) return; // skip static final fields (they're constants)

            serverPlayerFieldMap.put(field.getName(), field);
            field.setAccessible(true);
        });

        touhouPlayerFields.forEach(field -> {
            if ((field.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == (Modifier.STATIC | Modifier.FINAL)) return; // skip static final fields (they're constants)

            touhouPlayerFieldMap.put(field.getName(), field);
            field.setAccessible(true);
        });

        commonFieldNames.addAll(serverPlayerFieldMap.keySet());
        commonFieldNames.retainAll(touhouPlayerFieldMap.keySet());
    }

    public boolean bossMode = false;

    public TouhouPlayer(CraftPlayer player) {
        super(
            ((CraftWorld) player.getWorld()).getHandle().getServer(),
            ((CraftWorld) player.getWorld()).getHandle(),
            player.getProfile(),
            player.getHandle().clientInformation()
        );

        // Using the power of reflection and bytecode manipulation, we can do this

        // Copy all the common properties from the server player to the Touhou player
        for (var name : commonFieldNames) {
            var serverPlayerField = serverPlayerFieldMap.get(name);
            var touhouPlayerField = touhouPlayerFieldMap.get(name);

            if (serverPlayerField != null && touhouPlayerField != null) { // should always be true
                try {
                    switch (serverPlayerField.getType().getName()) {
                        case "boolean":
                            touhouPlayerField.setBoolean(this, serverPlayerField.getBoolean(player.getHandle()));
                            break;
                        case "byte":
                            touhouPlayerField.setByte(this, serverPlayerField.getByte(player.getHandle()));
                            break;
                        case "char":
                            touhouPlayerField.setChar(this, serverPlayerField.getChar(player.getHandle()));
                            break;
                        case "double":
                            touhouPlayerField.setDouble(this, serverPlayerField.getDouble(player.getHandle()));
                            break;
                        case "float":
                            touhouPlayerField.setFloat(this, serverPlayerField.getFloat(player.getHandle()));
                            break;
                        case "int":
                            touhouPlayerField.setInt(this, serverPlayerField.getInt(player.getHandle()));
                            break;
                        case "long":
                            touhouPlayerField.setLong(this, serverPlayerField.getLong(player.getHandle()));
                            break;
                        case "short":
                            touhouPlayerField.setShort(this, serverPlayerField.getShort(player.getHandle()));
                            break;
                        default:
                            touhouPlayerField.set(this, serverPlayerField.get(player.getHandle()));
                            break;
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("[GensouJank] Failed to copy property " + name + " from ServerPlayer to TouhouPlayer\n" + e);
                }
            }
        }

        // Some extra finjangling
        // i had a dream that all we had to do was modify the connection obj to point to our entity instead of making a new one
        //var cookie = new CommonListenerCookie(player.getProfile(), 0, player.getHandle().clientInformation());
        // connection = new ServerGamePacketListenerImpl(this.server, player.getHandle().connection.connection, this, cookie);
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        return bossMode ? getDimensions(Pose.SWIMMING).makeBoundingBox(this.position().add(0, 1.5, 0)) : super.makeBoundingBox();
    }
}
