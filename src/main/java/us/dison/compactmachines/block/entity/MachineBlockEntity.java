package us.dison.compactmachines.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.block.enums.MachineSize;
import us.dison.compactmachines.data.persistent.RoomManager;
import us.dison.compactmachines.util.RoomUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MachineBlockEntity extends BlockEntity {

    private MachineSize size;
    private int machineID = -1;
    public int lastPlayerCheckTick = -1;

    private UUID owner;
    public MachineBlockEntity(BlockPos pos, BlockState state, MachineSize size) {
        this(pos, state);
        this.size = size;
    }

    public MachineBlockEntity(BlockPos pos, BlockState state) {
        super(CompactMachines.MACHINE_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, MachineBlockEntity machineBlock) {
        if (world.isClient()) return;

        int now = world.getServer().getTicks();
        if (now - machineBlock.lastPlayerCheckTick < 10) return;
        machineBlock.lastPlayerCheckTick = now;

        if (machineBlock.getSize() == null) return;
        Box box = RoomUtil.getBox(RoomUtil.getCenterPosByID(machineBlock.getMachineID()), machineBlock.getSize().getSize());
        List<ServerPlayerEntity> players = CompactMachines.cmWorld.getPlayers();
        RoomManager roomManager = CompactMachines.getRoomManager();
        RoomManager.Room room = roomManager.getRoomByNumber(machineBlock.getMachineID());
        if (players.size() > 1) {
            List<ServerPlayerEntity> playersInMachine = new ArrayList<>();
            players.forEach(player -> {
                if (box.contains(player.getPos()))
                    playersInMachine.add(player);
            });

            if (room.getPlayers().size() != playersInMachine.size())
                roomManager.updatePlayers(machineBlock.getMachineID(), playersInMachine.stream().map(ServerPlayerEntity::getUuidAsString).toList());
        } else {
            roomManager.updatePlayers(machineBlock.getMachineID(), new ArrayList<String>());
        }


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
        tag.putInt("number", this.machineID);

        if (this.owner == null) setOwner(new UUID(0, 0));
        tag.putUuid("uuid", this.owner);

        super.writeNbt(tag);
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
