package us.dison.compactmachines.data.persistent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoomManager extends PersistentState {

    private static final Codec<List<Room>> CODEC = Codec.list(Room.CODEC);
    private static final String KEY = "compactmachines_rooms";

    private final List<Room> rooms = new ArrayList<>();


    public RoomManager() {
    }


    public static RoomManager get(World world) {
        ServerWorld serverWorld = (ServerWorld) world;
        return serverWorld.getPersistentStateManager().getOrCreate(RoomManager::fromTag, RoomManager::new, KEY);
    }

    private static RoomManager fromTag(NbtCompound tag) {
        RoomManager roomManager = new RoomManager();

        roomManager.rooms.clear();

        List<Room> newRooms = CODEC.parse(NbtOps.INSTANCE, tag.getList("rooms", NbtElement.COMPOUND_TYPE))
                .result()
                .orElse(Collections.emptyList());

        roomManager.rooms.addAll(newRooms);

        return roomManager;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compoundTag) {
        CODEC.encodeStart(NbtOps.INSTANCE, rooms)
                .result()
                .ifPresent(tag -> compoundTag.put("rooms", tag));
        return compoundTag;
    }

    public void onServerWorldLoad(ServerWorld world) {

    }

    public void onServerWorldTick(ServerWorld world) {

    }

    public void addRoom(Identifier world, String owner, BlockPos machine, BlockPos center, int number) {
        Validate.isTrue(!roomExists(number), "Room already exists with number: "+number);
        Validate.isTrue(!roomExists(machine), "Room already exists for machine at: "+machine.toShortString()+"; number: "+number);
        Room room = new Room(world, owner, machine, center, number);
        rooms.add(room);

        markDirty();
    }

    public void updateMachinePosAndOwner(int id, Identifier world, BlockPos machine, String uuid) {
        Room oldRoom = getRoomByNumber(id);
        rooms.remove(oldRoom);
        Room newRoom = new Room(world, uuid, machine, oldRoom.getCenter(), oldRoom.getNumber());
        rooms.add(newRoom);
    }

    public void updateOwner(int id, String uuid) {
        Room oldRoom = getRoomByNumber(id);
        rooms.remove(oldRoom);
        Room newRoom = new Room(oldRoom.getWorld(), uuid, oldRoom.getMachine(), oldRoom.getCenter(), oldRoom.getNumber());
        rooms.add(newRoom);
    }

    public void updateMachinePos(int id, Identifier world, BlockPos machine) {
        Room oldRoom = getRoomByNumber(id);
        rooms.remove(oldRoom);
        Room newRoom = new Room(world, oldRoom.getOwner(), machine, oldRoom.getCenter(), oldRoom.getNumber());
        rooms.add(newRoom);
    }

    public boolean roomExists(int number) {
        return getRoomByNumber(number) != null;
    }

    public boolean roomExists(BlockPos machine) {
        return getRoomByMachinePos(machine) != null;
    }

    public Room getRoomByNumber(int id) {
        for (Room room : rooms) {
            if (room.getNumber() == id) return room;
        }
        return null;
    }

    public Room getRoomByMachinePos(BlockPos machine) {
        for (Room room : rooms) {
            if (room.getMachine() == machine) return room;
        }
        return null;
    }
    

    public static class Room {

        public static final Codec<Room> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Identifier.CODEC.fieldOf("world").forGetter(Room::getWorld),
                        Codec.STRING.fieldOf("owner").forGetter(Room::getOwner),
                        BlockPos.CODEC.fieldOf("machine").forGetter(Room::getMachine),
                        BlockPos.CODEC.fieldOf("center").forGetter(Room::getCenter),
                        Codec.INT.fieldOf("number").forGetter(Room::getNumber)
                )
                .apply(instance, Room::new));

        private final Identifier world;
        private final String owner;
        private final BlockPos machine;
        private final BlockPos center;
        private final int number;


        public Room(Identifier world, String owner, BlockPos machine, BlockPos center, int number) {
            this.world = world;
            this.owner = owner;
            this.machine = machine;
            this.center = center;
            this.number = number;
        }


        public Identifier getWorld() {
            return world;
        }

        public String getOwner() {
            return owner;
        }

        public BlockPos getMachine() {
            return machine;
        }

        public BlockPos getCenter() {
            return center;
        }

        public int getNumber() {
            return number;
        }
    }
}
