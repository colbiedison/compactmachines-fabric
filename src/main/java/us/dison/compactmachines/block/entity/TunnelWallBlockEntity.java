package us.dison.compactmachines.block.entity;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.block.enums.TunnelDirection;
import us.dison.compactmachines.data.persistent.Room;
import us.dison.compactmachines.data.persistent.tunnel.Tunnel;
import us.dison.compactmachines.data.persistent.tunnel.TunnelType;
import us.dison.compactmachines.tunnel.IInventory;
import us.dison.compactmachines.util.TunnelUtil;

public class TunnelWallBlockEntity extends AbstractWallBlockEntity implements RenderAttachmentBlockEntity, IInventory {

    private String strType = "";
    private boolean isConnected = false;
    private boolean outgoing = false; 
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
        this.outgoing = tag.getBoolean("outgoing");
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        tag.putBoolean("isConnected", this.isConnected);
        tag.putString("type", this.strType);
        tag.putBoolean("outgoing", this.outgoing);
        super.writeNbt(tag);
    }

    @Override
    public Object getRenderAttachmentData() {
        return new RenderAttachmentData(getTunnelType(), isConnected(), isOutgoing());
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
        Tunnel tunnel = getTunnel();
        CompactMachines.getRoomManager().updateTunnel(getRoom().getNumber(), new Tunnel(
                tunnel.getPos(),
                tunnel.getFace(),
                tunnel.getType(),
                connected,
                tunnel.isOutgoing()
        ));
        markDirty();
    }
    public boolean isOutgoing() {
        return outgoing;
    }
    public void setOutgoing(boolean outgoing) {
        this.outgoing = outgoing;
        Tunnel tunnel = getTunnel();
        CompactMachines.getRoomManager().updateTunnel(getRoom().getNumber(), new Tunnel(
                    tunnel.getPos(),
                    tunnel.getFace(),
                    tunnel.getType(),
                    tunnel.isConnected(),
                    outgoing
        ));
        markDirty();
        this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
    }
    public void setStrType(String strType) {
        this.strType = strType;
    }

    public Room getRoom() {
        return CompactMachines.getRoomManager().getRoomByNumber(getParentID());
    }

    public BlockEntity getExtTransferTarget() {
        if (this.getTunnelType() != TunnelType.ITEM) return null;
        Room room = getRoom();
        BlockPos machinePos = room.getMachinePos();
        try {
            ServerWorld machineWorld = getWorld().getServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, room.getWorld()));
            Tunnel tunnel = getTunnel();
            if (tunnel.getFace() == TunnelDirection.NONE) return null;
            BlockPos targetPos = machinePos.offset(tunnel.getFace().toDirection(), 1);
            machineWorld.getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(targetPos), 3, targetPos);
            return machineWorld.getBlockEntity(targetPos);
        } catch (Exception ignored) {}
        return null;
    }

    public BlockEntity getInternalTransferTarget() {
        if (this.getTunnelType() != TunnelType.ITEM) return null;
        Room room = getRoom();
        try {
            for (Direction dir : Direction.values()) {
                BlockEntity be = world.getBlockEntity(pos.offset(dir, 1));
                if (    be instanceof Inventory &&
                     ! (be instanceof TunnelWallBlockEntity) ) {
                    return be;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public MachineBlockEntity getMachineEntity() {
        Room room = getRoom();
        try {
            ServerWorld machineWorld = getWorld().getServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, room.getWorld()));
            machineWorld.getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(room.getMachinePos()), 3, room.getMachinePos());
            return (MachineBlockEntity) machineWorld.getBlockEntity(room.getMachinePos());
        } catch (Exception ignored) {}
        return null;
    }

    public Tunnel getTunnel() {
        return TunnelUtil.fromRoomAndPos(getRoom(), getPos());
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        Room room = getRoom();

        try {
            Tunnel tunnel = getTunnel();
            if (tunnel.getFace() == TunnelDirection.NONE) throw new Exception();
            BlockEntity targetEntity = getExtTransferTarget();
            if (targetEntity instanceof Inventory inv) {
                DefaultedList<ItemStack> targetContents = DefaultedList.ofSize(inv.size(), ItemStack.EMPTY);
                for (int i = 0; i < inv.size(); i++) {
                    targetContents.set(i, inv.getStack(i));
                }
                return targetContents;
            }
        } catch (Exception ignored) {}

        return DefaultedList.ofSize(1, ItemStack.EMPTY);
    }

    @Override
    public int size() {
        Room room = getRoom();

        if (getExtTransferTarget() instanceof Inventory inv)
            return inv.size();
        return 0;
    }

    public class RenderAttachmentData {
        private final TunnelType type;
        private final boolean isConnected;
        private final boolean isOutgoing;
        private RenderAttachmentData(TunnelType type, boolean isConnected, boolean isOutgoing) {
            this.type = type;
            this.isConnected = isConnected;
            this.isOutgoing = isOutgoing;
        }

        public TunnelType getType() {
            return type;
        }

        public boolean isConnected() {
            return isConnected;
        }
        public boolean isOutgoing() {
            return isOutgoing;
        }
    }
}
