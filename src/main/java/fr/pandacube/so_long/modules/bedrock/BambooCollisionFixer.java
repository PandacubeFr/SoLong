package fr.pandacube.so_long.modules.bedrock;

import fr.pandacube.lib.paper.reflect.wrapper.minecraft.world.AABB;
import fr.pandacube.lib.paper.reflect.wrapper.minecraft.world.block.BambooBlock;
import fr.pandacube.lib.paper.reflect.wrapper.paper.AABBVoxelShape;
import fr.pandacube.lib.util.Log;
import fr.pandacube.so_long.SoLong;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.BoundingBox;

// simplified version of https://github.com/Camotoy/BambooCollisionFix/tree/c7d7d5327791cbb416d106de0b9eb0bf2461acbd/src/main/java/net/camotoy/bamboocollisionfix
// we remove the bamboo bounding box due to bedrock clients not having the same placement for bamboos
public final class BambooCollisionFixer implements Listener {
    private final BoundingBox originalBambooBoundingBox = new BoundingBox(6.5D / 16D, 0.0D, 6.5D / 16.0D, 9.5D / 16.0D, 1D, 9.5D / 16.0D);

    public BambooCollisionFixer() {
        // Make the bamboo block have zero collision.
        try {
            BambooBlock.COLLISION_SHAPE(new AABBVoxelShape(new AABB(0.5, 0, 0.5, 0.5, 0, 0.5)));
            Log.info("Bamboo block collision box removed succesfully.");
        } catch (Exception e) {
            Log.severe("Unable to remove the collision box of the Bamboo block.", e);
            return;
        }
        Bukkit.getPluginManager().registerEvents(this, SoLong.getPlugin());
    }

    /**
     * Because the bamboo has an empty bounding box, it can be placed inside players... prevent that to the best of
     * our ability.
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getBlockData().getMaterial().equals(Material.BAMBOO)) {
            BoundingBox currentBambooBoundingBox = originalBambooBoundingBox.clone().shift(event.getBlockPlaced().getLocation());
            for (LivingEntity e : event.getBlock().getLocation().getNearbyLivingEntities(5)) {
                if (e.getBoundingBox().overlaps(currentBambooBoundingBox)) {
                    // Don't place the bamboo as it intersects
                    event.setBuild(false);
                    break;
                }
            }
        }
    }
}
