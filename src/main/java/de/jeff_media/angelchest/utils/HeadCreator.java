package de.jeff_media.angelchest.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.nms.NMSGenericHeadCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public final class HeadCreator {

    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "CanBeFinal"})
    private static String defaultBase64 = "SSBkb24ndCBsaWtlIHBlb3BsZSB3aG8gdHJ5IHRvIHN0ZWFsIG15IHBsdWdpbnMuIEl0J3Mgb3BlbiBzb3VyY2UgYnJvLCBzbyB3aHkgZG8geW91IGV2ZW4gYm90aGVyIGFib3V0IHNlbmRpbmcgdGhlIC5qYXIgZmlsZSBhd2F5PyBBdCBsZWFzdCBoYXZlIG90aGVyIHBlb3BsZSBjb21waWxlIGl0IHRoZW1zZWx2ZXMuLi4=eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90Z%%__USER__%%Xh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNThiYzhmYTcxNmN%%__NONCE__%%hZGQwMDRiODI4Y2IyN2NjMGY2ZjZhZGUzYmU0MTUxMTY4OGNhOWVjZWZmZDE2NDdmYjkifX19SSBkb24ndCBsaWtlIHBlb3BsZSB3aG8gdHJ5IHRvIHN0ZWFsIG15IHBsdWdpbnMuIEl0J3Mgb3BlbiBzb3VyY2UgYnJvLCBzbyB3aHkgZG8geW91IGV2ZW4gYm90aGVyIGFib3V0IHNlbmRpbmcgdGhlIC5qYXIgZmlsZSBhd2F5PyBBdCBsZWFzdCBoYXZlIG90aGVyIHBlb3BsZSBjb21waWxlIGl0IHRoZW1zZWx2ZXMuLi4=";

    public static void createHeadInWorld(final Block block, final UUID uuid) {
        final Main main = Main.getInstance();

        if (main.isOutsideOfNormalWorld(block)) return;

        if (!(block.getState() instanceof Skull)) {
            main.getLogger().severe("Could not spawn head at " + block);
            return;
        }

        final Skull state = (Skull) block.getState();

        // Use the player skin's texture
        if (main.getConfig().getBoolean(Config.HEAD_USES_PLAYER_NAME)) {
            if(main.debug) main.debug("Player head = username");
            final OfflinePlayer player = main.getServer().getOfflinePlayer(uuid);
            state.setOwningPlayer(player);
            state.update();
        }
        // Use a predefined texture
        else {
            if(main.debug) main.debug("Player head = base64");
            final String base64 = main.getConfig().getString(Config.CUSTOM_HEAD_BASE64);
            final GameProfile profile = new GameProfile(UUID.randomUUID(), "");
            profile.getProperties().put("textures", new Property("textures", base64));

            // 1.17 +
            try {
                NMSGenericHeadCreator.createHeadInWorld(block,profile);
            } catch (final Throwable t) {
                // 1.16 and below
                try {
                    final Object nmsWorld = block.getWorld().getClass().getMethod("getHandle").invoke(block.getWorld());
                    final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
                    final Class<?> blockPositionClass = Class.forName("net.minecraft.server." + version + ".BlockPosition");
                    final Class<?> tileEntityClass = Class.forName("net.minecraft.server." + version + ".TileEntitySkull");
                    final Constructor<?> cons = blockPositionClass.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);
                    final Object blockPosition = cons.newInstance(block.getX(), block.getY(), block.getZ());
                    final Method getTileEntity = nmsWorld.getClass().getMethod("getTileEntity", blockPositionClass);
                    final Object tileEntity = tileEntityClass.cast(getTileEntity.invoke(nmsWorld, blockPosition));
                    tileEntityClass.getMethod("setGameProfile", GameProfile.class).invoke(tileEntity, profile);
                } catch (final IllegalArgumentException | IllegalAccessException | SecurityException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException | InstantiationException e) {
                    main.getLogger().warning("Could not set custom base64 player head.");
                }
            }

        }
    }

    public static ItemStack getHead(final String base64) {

        final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta) head.getItemMeta();
        final GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", base64));
        final Field profileField;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (final IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
            return new ItemStack(Material.PLAYER_HEAD);
        }

        head.setItemMeta(meta);
        return head;
    }

    @SuppressWarnings("unused")
    public static ItemStack getHead() {
        return getHead(defaultBase64);
    }

    public static ItemStack getPlayerHead(final UUID uuid) {
        final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta skullMeta = (SkullMeta) (head.hasItemMeta() ? head.getItemMeta() : Bukkit.getItemFactory().getItemMeta(Material.PLAYER_HEAD));
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        head.setItemMeta(skullMeta);
        return head;
    }
}
