package de.jeff_media.angelchest.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.enums.Features;
import de.jeff_media.daddy.Daddy;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

/**
 * Hooks into WorldGuard 7+. If this fails, it tries to use the WorldGuardLegacyHandler for older versions.
 */
public final class WorldGuardHandler extends WorldGuardWrapper {


    final Main main;
    public boolean disabled = false;
    public static StateFlag FLAG_ALLOW_ANGELCHEST = null;
    WorldGuardPlugin wg;
    RegionContainer container;
    // This is for WorldGuard 7+ only.
    // If an older version is installed, this class will redirect the check to the legacy handler
    WorldGuardLegacyHandler legacyHandler = null;

    public WorldGuardHandler(final Main main) {
        this.main = main;

        if (main.getConfig().getBoolean(Config.DISABLE_WORLDGUARD_INTEGRATION)) {
            disabled = true;
            main.getLogger().info("WorldGuard integration has been disabled in the config.yml.");
            return;
        }

        if (main.getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            main.debug("WorldGuard is not installed at all.");
            disabled = true;
            return;
        }

        try {
            Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin").getMethod("inst");
            wg = WorldGuardPlugin.inst();
        } catch (final ClassNotFoundException | NoSuchMethodException e) {
            //System.out.println("WorldGuard not found");
            disabled = true;
            return;
        }

        // Getting here means WorldGuard is installed

        if (wg != null) {
            try {
                // This only works on WorldGuard 7+
                container = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(WorldGuard.getInstance(), "WorldGuard#getInstance is null")
                        .getPlatform(), "WorldGuard#getInstance#getPlatform is null").getRegionContainer(), "WorldGuard#getInstance#getRegionContainer is null");
                main.getLogger().info("Successfully hooked into WorldGuard 7+");


            } catch (final NoClassDefFoundError e) {
                // Ok, try again with version 6
                legacyHandler = new WorldGuardLegacyHandler(this);
            } catch (final NullPointerException e) {
                disabled = true;
                main.getLogger().info("You are using a version of WorldGuard that does not fully support your Minecraft version. WorldGuard integration is disabled.");
            }
        }
    }

    public static void tryToRegisterFlags() {

        Main main = Main.getInstance();
        //main.debug("Trying to register WorldGuard Flags");

        // Check if WorldGuard is installed AND IF ITS A SUPPORTED VERSION (7+)
        if (main.getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            //main.debug("Could not register flags: WorldGuard not installed.");
            return;
        }
        try {
            Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin").getMethod("inst");
        } catch (final Exception | Error e) {
            main.getLogger().warning("Could not register WorldGuard flags although WorldGuard is installed.");
            e.printStackTrace();
            return;
        }

        // Flags start
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("allow-angelchest", true);
            registry.register(flag);
            FLAG_ALLOW_ANGELCHEST = flag;
        } catch (Exception weDontUseflagConflictExceptionBecauseItThrowsNoClassDefFoundErrorWhenWorldGuardIsNotInstalled) {
            Flag<?> existing = registry.get("allow-angelchest");
            if(existing instanceof StateFlag) {
                FLAG_ALLOW_ANGELCHEST = (StateFlag) existing;
            } else {
                main.getLogger().warning("Could not register WorldGuard flag \"allow-angelchest\"");
            }
        }
        main.getLogger().info("Successfully registered WorldGuard flags.");
        // Flags end
    }

    @Override
    public boolean getAngelChestFlag(final Player player) {
        if(disabled) return true;
        if(legacyHandler != null) return true;
        if(wg == null) return true;
        if(FLAG_ALLOW_ANGELCHEST == null) return true;
        final Block block = player.getLocation().getBlock();
        final RegionManager regions = container.get(BukkitAdapter.adapt(block.getWorld()));
        final BlockVector3 position = BlockVector3.at(block.getX(),block.getY(), block.getZ());
        final ApplicableRegionSet set = regions.getApplicableRegions(position);
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        boolean allow = set.testState(localPlayer,FLAG_ALLOW_ANGELCHEST);
        if(allow) {
            return true;
        } else {
            if(!Daddy.allows(Features.WORLD_GUARD_FLAGS)) {
                main.getLogger().warning("You are using AngelChest's WorldGuard flags, which are only available in AngelChestPlus. See here: "+main.UPDATECHECKER_LINK_DOWNLOAD_PLUS);
                return true;
            }
            return false;
        }
    }

    /**
     * Checks whether this block is inside one of the disabled WorldGuard Regions.
     *
     * @param block Block to check
     * @return true if the block is inside a blacklisted region, otherwise false
     */
    @Override
    public boolean isBlacklisted(final Block block) {
        if (disabled) return false;
        if (legacyHandler != null) return legacyHandler.isBlacklisted(block);
        if (wg == null) return false;
        if (main.disabledRegions == null || main.disabledRegions.isEmpty()) return false;

        final RegionManager regions = container.get(BukkitAdapter.adapt(block.getWorld()));
        final List<String> regionList = regions.getApplicableRegionsIDs(getBlockVector3(block));

        main.debug("Checking Regions in WG7+");

        for (final String r : regionList) {
            main.debug("Player died in region " + r);
            if (main.disabledRegions.contains(r)) {
                main.debug("Preventing AngelChest from spawning in disabled worldguard region");
                return true;
            }
        }
        return false;
    }

    BlockVector3 getBlockVector3(final Block block) {
        return BlockVector3.at(block.getX(), block.getY(), block.getZ());
    }
}
