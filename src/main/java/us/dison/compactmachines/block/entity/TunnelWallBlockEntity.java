package us.dison.compactmachines.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import us.dison.compactmachines.data.persistent.tunnel.Tunnel;
import us.dison.compactmachines.data.persistent.tunnel.TunnelType;

public class TunnelWallBlockEntity extends MachineWallBlockEntity {

    private String strType;
    private boolean isConnected;

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

        if (tag.contains("type"))
            this.strType = tag.getString("type");
        this.isConnected = tag.getBoolean("isConnected");
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        if (this.strType != null)
            tag.putString("type", this.strType);
        tag.putBoolean("isConnected", this.isConnected);

        super.writeNbt(tag);
    }


    public TunnelType getTunnelType() {
        return TunnelType.byName(this.strType);
    }

    public void setTunnelType(TunnelType type) {
        this.strType = type.getName();
        this.markDirty();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
        this.markDirty();
    }
}
