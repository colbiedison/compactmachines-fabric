package us.dison.compactmachines.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import us.dison.compactmachines.CompactMachines;

public class MachineWallBlockEntity extends AbstractWallBlockEntity {

    public MachineWallBlockEntity(BlockPos pos, BlockState state) {
        super(CompactMachines.MACHINE_WALL_BLOCK_ENTITY, pos, state);
    }


    public static void tick(World world, BlockPos blockPos, BlockState blockState, MachineWallBlockEntity wallBlockEntity) {

    }

}
