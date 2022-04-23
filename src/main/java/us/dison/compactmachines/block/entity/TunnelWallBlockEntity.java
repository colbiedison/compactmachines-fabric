package us.dison.compactmachines.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import us.dison.compactmachines.data.persistent.tunnel.Tunnel;

public class TunnelWallBlockEntity extends MachineWallBlockEntity {

    public TunnelWallBlockEntity(BlockPos pos, BlockState state, int parentID) {
        super(pos, state, parentID);
    }

    public TunnelWallBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    public static void tick(World world, BlockPos blockPos, BlockState blockState, TunnelWallBlockEntity tunnelBlockEntity) {

    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
    }

}
