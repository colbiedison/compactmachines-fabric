package us.dison.compactmachines.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import us.dison.compactmachines.CompactMachines;

public class MachineWallBlockEntity extends BlockEntity {

    private int parentID;

    public MachineWallBlockEntity(BlockPos pos, BlockState state, int parentID) {
        this(pos, state);
        this.parentID = parentID;
    }

    public MachineWallBlockEntity(BlockPos pos, BlockState state) {
        super(CompactMachines.MACHINE_WALL_BLOCK_ENTITY, pos, state);
    }


    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        this.parentID = tag.getInt("parent");
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        tag.putInt("parent", this.parentID);

        super.writeNbt(tag);
    }

    public static void tick(World world, BlockPos blockPos, BlockState blockState, MachineWallBlockEntity wallBlockEntity) {

    }


    public int getParentID() {
        return parentID;
    }

    public void setParentID(int parentID) {
        this.parentID = parentID;
        this.markDirty();
    }
}
