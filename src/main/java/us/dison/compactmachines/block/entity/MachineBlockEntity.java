package us.dison.compactmachines.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.block.enums.MachineSize;

import java.util.UUID;

public class MachineBlockEntity extends BlockEntity {
    private MachineSize size;

    private int machineID = -1;

    private UUID owner;
    public MachineBlockEntity(BlockPos pos, BlockState state, MachineSize size) {
        this(pos, state);
        this.size = size;
    }

    public MachineBlockEntity(BlockPos pos, BlockState state) {
        super(CompactMachines.MACHINE_BLOCK_ENTITY, pos, state);
    }

    public MachineSize getSize() {
        return size;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        this.machineID = tag.getInt("number");
        this.owner = tag.getUuid("uuid");
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        if (this.machineID != -1) {
            tag.putInt("number", this.machineID);
            tag.putUuid("uuid", this.owner);
        }

        super.writeNbt(tag);
    }

    public int getMachineID() {
        return machineID;
    }

    public void setMachineID(int machineID) {
        this.machineID = machineID;
        this.markDirty();
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        this.markDirty();
    }
}
