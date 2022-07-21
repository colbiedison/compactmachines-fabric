package us.dison.compactmachines.block.entity;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
//import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
//import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.Optional;

//import java.util.Iterator;
//import java.util.NoSuchElementException;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.block.enums.TunnelDirection;
import us.dison.compactmachines.data.persistent.Room;
import us.dison.compactmachines.data.persistent.tunnel.Tunnel;
import us.dison.compactmachines.data.persistent.tunnel.TunnelType;
import us.dison.compactmachines.util.TunnelUtil;

public class TunnelWallBlockEntity extends AbstractWallBlockEntity implements RenderAttachmentBlockEntity {

    private String strType = "";
    private boolean connectedToItem = false;
    private boolean connectedToEnergy = false; 
    private boolean connectedToFluid = false;
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
        this.connectedToItem = tag.getBoolean("connectedToItem");
        this.connectedToEnergy = tag.getBoolean("connectedToEnergy");
        this.connectedToFluid = tag.getBoolean("connectedToFluid");
        this.outgoing = tag.getBoolean("outgoing");
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        tag.putBoolean("connectedToItem", this.connectedToItem);
        tag.putBoolean("connectedToEnergy", this.connectedToEnergy);
        tag.putBoolean("connectedToFluid", this.connectedToFluid);
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
        return connectedToItem || connectedToFluid || connectedToEnergy;
    }

    public void setConnectedToFluid(boolean connected) {
        connectedToFluid = connected;
        Tunnel tunnel = getTunnel();
        final Room room = getRoom();
        if (room == null) return;
        CompactMachines.getRoomManager().updateTunnel(room.getNumber(), tunnel.withConnectedToFluid(connected));
        markDirty();
    }
    public void setConnectedToItem(boolean connected) {
        connectedToItem = connected;
        Tunnel tunnel = getTunnel();
        final Room room = getRoom();
        if (room == null) return;
        CompactMachines.getRoomManager().updateTunnel(room.getNumber(), tunnel.withConnectedToItem(connected));
        markDirty();
    }

    public void setConnectedToEnergy(boolean connected) {
        connectedToEnergy = connected;
        Tunnel tunnel = getTunnel();
        final Room room = getRoom();
        if (room == null) return;
        CompactMachines.getRoomManager().updateTunnel(room.getNumber(), tunnel.withConnectedToEnergy(connected));
        markDirty();
    }

    public boolean isOutgoing() {
        return outgoing;
    }
    public void setOutgoing(boolean outgoing) {
        this.outgoing = outgoing;
        Tunnel tunnel = getTunnel();
        final Room room = getRoom();
        if (room == null) return;
        CompactMachines.getRoomManager().updateTunnel(room.getNumber(), tunnel.withOutgoing(outgoing));
        markDirty();
        this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
    }
    public void setStrType(String strType) {
        this.strType = strType;
    }

    public Room getRoom() {
        return CompactMachines.getRoomManager().getRoomByNumber(getParentID());
    }
    @Nullable
    public Storage<ItemVariant> getExtTransferTarget() {
        if (this.getTunnelType() != TunnelType.ITEM) return null;
        final Room room = getRoom();
        if (room == null) return null;
        BlockPos machinePos = room.getMachinePos();
        try {
            ServerWorld machineWorld = getWorld().getServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, room.getWorld()));
            Tunnel tunnel = getTunnel();
            if (tunnel.getFace() == TunnelDirection.NONE) return null;
            BlockPos targetPos = machinePos.offset(tunnel.getFace().toDirection(), 1);
            machineWorld.getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(targetPos), 3, targetPos);
            return ItemStorage.SIDED.find(world, targetPos, tunnel.getFace().toDirection());
        } catch (Exception ignored) {}
        return new NullInventory();
    }
    @Nullable
    public Storage<ItemVariant> getInternalTransferTarget() {
        if (this.getTunnelType() != TunnelType.ITEM) return null;
        try {
            for (Direction dir : Direction.values()) {
                final BlockPos newPos = pos.offset(dir, 1);
                BlockEntity be = world.getBlockEntity(newPos);
                if (be instanceof TunnelWallBlockEntity) continue;
                final Storage<ItemVariant> storage = ItemStorage.SIDED.find(world, newPos, dir);
                if (storage != null) return storage;
            }
        } catch (Exception ignored) {}
        return null;
    }
    @NotNull
    public Optional<Storage<FluidVariant>> getExtFluidTarget() {
        if (this.getTunnelType() != TunnelType.ITEM) return Optional.empty();
        Room room = getRoom();
        if (room == null) return null;
        BlockPos machinePos = room.getMachinePos();
        try {
            ServerWorld machineWorld = getWorld().getServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, room.getWorld()));
            Tunnel tunnel = getTunnel();
            if (tunnel.getFace() == TunnelDirection.NONE) return Optional.empty();
            BlockPos targetPos = machinePos.offset(tunnel.getFace().toDirection(), 1);
            machineWorld.getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(targetPos), 3, targetPos);
            return Optional.ofNullable(FluidStorage.SIDED.find(world, targetPos, tunnel.getFace().toDirection()));
        } catch (Exception ignored) {}
        return Optional.of(new NullFluidInventory());    
    }
    @NotNull
    public Optional<Storage<FluidVariant>> getInternalFluidTarget() {
        if (this.getTunnelType() != TunnelType.ITEM) return Optional.empty();
        try {
            for (Direction dir : Direction.values()) {
                final BlockPos newPos = pos.offset(dir, 1);
                BlockEntity be = world.getBlockEntity(newPos);
                if (be instanceof TunnelWallBlockEntity) continue; 
                final Storage<FluidVariant> storage = FluidStorage.SIDED.find(world, newPos, dir); 
                if (storage != null) return Optional.of(storage);
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }
    @NotNull
    public Optional<EnergyStorage> getExtEnergyTarget() {
        if (this.getTunnelType() != TunnelType.ITEM) return Optional.empty(); 
        Room room = getRoom();
        BlockPos machinePos = room.getMachinePos();
        try {
            ServerWorld machineWorld = getWorld().getServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, room.getWorld()));
            Tunnel tunnel = getTunnel();
            if (tunnel.getFace() == TunnelDirection.NONE) return Optional.empty();
            BlockPos targetPos = machinePos.offset(tunnel.getFace().toDirection(), 1);
            machineWorld.getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(targetPos), 3, targetPos);
            return Optional.ofNullable(EnergyStorage.SIDED.find(world, targetPos, tunnel.getFace().toDirection()));
        } catch (Exception ignored) {}
        return Optional.of(new SimpleEnergyStorage(0l, 0l, 0l));
    }
    public Optional<EnergyStorage> getInternalEnergyTarget() {
        if (this.getTunnelType() != TunnelType.ITEM) return Optional.empty();
        try {
            for (Direction dir : Direction.values()) {
                final BlockPos newPos = pos.offset(dir, 1);
                BlockEntity be = world.getBlockEntity(newPos);
                if (be instanceof TunnelWallBlockEntity) continue; 
                final EnergyStorage storage = EnergyStorage.SIDED.find(world, newPos, dir); 
                if (storage != null) return Optional.of(storage);
            }
        } catch (Exception ignored) {}
        return Optional.empty();

    }
    /*
    @Override
    public Iterator<? extends StorageView<ItemVariant>> iterator(TransactionContext context) {
        final Storage<ItemVariant> storage = getExtTransferTarget();
        if (storage == null) {
            return new Iterator<StorageView<ItemVariant>>() {
                public boolean hasNext() {
                    return false; 
                }
                public StorageView<ItemVariant> next() {
                    throw new NoSuchElementException();
                }
            };
        }
        return storage.iterator(context);
    }
    @Override
    public Iterable<? extends StorageView<ItemVariant>> iterable(TransactionContext context) {
        final Storage<ItemVariant> storage = getExtTransferTarget();
        if (storage == null) {
            return new Iterable<StorageView<ItemVariant>>() {
                public Iterator<StorageView<ItemVariant>> iterator() {
                    return new Iterator<StorageView<ItemVariant>>() {
                        public boolean hasNext() {
                            return false; 
                        }
                        public StorageView<ItemVariant> next() {
                            throw new NoSuchElementException();
                        }
                    };
                }
            };
        }
        return storage.iterable(context);
    }
    @Override 
    public boolean supportsInsertion() {
        final Storage<ItemVariant> storage = getExtTransferTarget();
        return storage == null ? false : storage.supportsInsertion();
    }
    @Override 
    public boolean supportsExtraction() {
        final Storage<ItemVariant> storage = getExtTransferTarget();
        return storage == null ? false : storage.supportsExtraction();
    }
    @Override 
    @Nullable
    public StorageView<ItemVariant> exactView(TransactionContext context, ItemVariant resource) {
        final Storage<ItemVariant> storage = getExtTransferTarget();
        return storage == null ? null : storage.exactView(context, resource);
    }
    @Override 
    public long extract(ItemVariant resource, long maxAmount, TransactionContext context) {
        final Storage<ItemVariant> storage = getExtTransferTarget();
        return storage == null ? 0l : storage.extract(resource, maxAmount, context);
    }
    @Override 
    public long getVersion() {
        final Storage<ItemVariant> storage = getExtTransferTarget();
        return storage == null ? 0l : storage.getVersion();
    }
    @Override 
    public long insert(ItemVariant resource, long maxAmount, TransactionContext context) {

        final Storage<ItemVariant> storage = getExtTransferTarget();
        return storage == null ? 0l : storage.insert(resource, maxAmount, context);
    }
    @Override 
    public long simulateExtract(ItemVariant resource, long maxAmount, @Nullable TransactionContext context) { 
        final Storage<ItemVariant> storage = getExtTransferTarget();
        if (storage == null) return 0l;
        return storage.simulateExtract(resource, maxAmount, context);
    } 
    public long simulateInset(ItemVariant resource, long maxAmount, @Nullable TransactionContext context) { 
        final Storage<ItemVariant> storage = getExtTransferTarget();
        if (storage == null) return 0l;
        return storage.simulateInsert(resource, maxAmount, context);
    }
    */
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
    public class NullInventory extends SingleVariantStorage<ItemVariant> {
        @Override 
        protected long getCapacity(ItemVariant variant) {
            return 0l;
        }
        @Override 
        protected ItemVariant getBlankVariant() {
            return ItemVariant.blank();
        }
    }
    public class NullFluidInventory extends SingleVariantStorage<FluidVariant> {
        @Override 
        protected long getCapacity(FluidVariant variant) {
            return 0l;
        }
        @Override 
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }
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
