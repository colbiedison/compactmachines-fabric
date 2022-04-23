package us.dison.compactmachines.block.entity;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.data.persistent.tunnel.TunnelType;

public class TunnelWallBlockEntity extends AbstractWallBlockEntity implements RenderAttachmentBlockEntity {

    private String strType = "";
    private boolean isConnected = false;

    public TunnelWallBlockEntity(BlockPos pos, BlockState state) {
        super(CompactMachines.TUNNEL_WALL_BLOCK_ENTITY, pos, state);
    }


    public static void tick(World world, BlockPos blockPos, BlockState blockState, TunnelWallBlockEntity tunnelBlockEntity) {

    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        this.strType = tag.getString("type");
        this.isConnected = tag.getBoolean("isConnected");
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        tag.putBoolean("isConnected", this.isConnected);
        tag.putString("type", this.strType);

        super.writeNbt(tag);
    }

    @Override
    public Object getRenderAttachmentData() {
        System.out.println(strType);
        return new RenderAttachmentData(getTunnelType(), isConnected());
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    public TunnelType getTunnelType() {
        return TunnelType.byName(strType);
    }

    public void setTunnelType(TunnelType type) {
        strType = type.getName();
        markDirty();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
        markDirty();
    }

    public void setStrType(String strType) {
        this.strType = strType;
    }

    public class RenderAttachmentData {
        private final TunnelType type;
        private final boolean isConnected;

        private RenderAttachmentData(TunnelType type, boolean isConnected) {
            this.type = type;
            this.isConnected = isConnected;
        }

        public TunnelType getType() {
            return type;
        }

        public boolean isConnected() {
            return isConnected;
        }
    }
}
