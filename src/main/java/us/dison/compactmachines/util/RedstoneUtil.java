package us.dison.compactmachines.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class RedstoneUtil {

    public static int getPower(World world, BlockPos pos) {
        final int sanePower = world.getReceivedRedstonePower(pos); 
        if (sanePower >= 15) return sanePower; 
        int insanePower = 0; 
        for (Direction dir : Direction.values()) {
            if (dir == Direction.DOWN) continue;
            final BlockState state = world.getBlockState(pos.offset(dir)); 
            if (state.isOf(Blocks.REDSTONE_WIRE)) {
                final int wirePower = state.get(RedstoneWireBlock.POWER); 
                if (wirePower >= 15) return wirePower; 
                if (wirePower > insanePower) {
                    insanePower = wirePower;
                }
            }
        }
        return Math.max(sanePower, insanePower);
    }
}
