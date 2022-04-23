package us.dison.compactmachines.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public abstract class AbstractWallBlockEntity extends BlockEntity {

    private int parentID = -1;

    public AbstractWallBlockEntity(BlockEntityType blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
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

    public int getParentID() {
        return parentID;
    }

    public void setParentID(int parentID) {
        this.parentID = parentID;
        markDirty();
    }
}
