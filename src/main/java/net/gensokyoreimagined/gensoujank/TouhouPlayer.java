package net.gensokyoreimagined.gensoujank;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

    private static final Set<String> commonDescriptorNames = new HashSet<>();
    private static final HashMap<String, Field> serverPlayerDescriptorMap = new HashMap<>();
    private static final HashMap<String, Field> touhouPlayerDescriptorMap = new HashMap<>();

    static {
        // Now invoking reflection magic, please watch warmly

        // TLDR: get all the property descriptors for ServerPlayer and TouhouPlayer, then get the common ones
        var serverPlayerFields = getFields(ServerPlayer.class);
        var touhouPlayerFields = getFields(TouhouPlayer.class);

        serverPlayerFields.forEach(field -> {
            if ((field.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == (Modifier.STATIC | Modifier.FINAL)) return; // skip static final fields (they're constants)

            serverPlayerDescriptorMap.put(field.getName(), field);
            field.setAccessible(true);
        });

        touhouPlayerFields.forEach(field -> {
            if ((field.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == (Modifier.STATIC | Modifier.FINAL)) return; // skip static final fields (they're constants)

            touhouPlayerDescriptorMap.put(field.getName(), field);
            field.setAccessible(true);
        });

        commonDescriptorNames.addAll(serverPlayerDescriptorMap.keySet());
        commonDescriptorNames.retainAll(touhouPlayerDescriptorMap.keySet());

        // var nameList = commonDescriptorNames.stream().reduce((a, b) -> a + ", " + b).orElse("");
        // Bukkit.getLogger().info("[GensouJank] Common fields: " + nameList);
    }

    private static Iterable<Field> getFields(Class<?> startClass) {

        var fields = new ArrayList<>(List.of(startClass.getDeclaredFields()));
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && !parentClass.equals(Object.class)) {
            // lol, recursion
            var parentFields = getFields(parentClass);
            parentFields.forEach(fields::add);
        }

        return fields;
    }

    public boolean bossMode = false;

    // public TouhouPlayer(MinecraftServer server, ServerLevel world, GameProfile profile, ClientInformation clientOptions) {
    //     super(server, world, profile, clientOptions);
    // }

    public TouhouPlayer(CraftPlayer player) {
        super(
            ((CraftWorld) player.getWorld()).getHandle().getServer(),
            ((CraftWorld) player.getWorld()).getHandle(),
            player.getProfile(),
            player.getHandle().clientInformation()
        );

        // Using the power of reflection and bytecode manipulation, we can do this

        // Copy all the common properties from the server player to the Touhou player
        for (var name : commonDescriptorNames) {
            var serverPlayerField = serverPlayerDescriptorMap.get(name);
            var touhouPlayerField = touhouPlayerDescriptorMap.get(name);

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

                    Bukkit.getLogger().info("[GensouJank] Copied property " + name + " from ServerPlayer to TouhouPlayer");
                } catch (Exception e) {
                    Bukkit.getLogger().warning("[GensouJank] Failed to copy property " + name + " from ServerPlayer to TouhouPlayer\n" + e);
                }
            }
        }
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        return bossMode ? getDimensions(Pose.SWIMMING).makeBoundingBox(this.position().add(0, 1.5, 0)) : super.makeBoundingBox();
    }
}
