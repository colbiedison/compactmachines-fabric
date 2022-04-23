package us.dison.compactmachines.data.persistent;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.data.persistent.tunnel.Tunnel;

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

    public void addRoom(Identifier world, String owner, BlockPos machine, BlockPos center, BlockPos spawnPos, int number, List<String> players, List<Tunnel> tunnels) {
        Validate.isTrue(!roomExists(number), "Room already exists with number: "+number);
        Validate.isTrue(!roomExists(machine), "Room already exists for machine at: "+machine.toShortString()+"; number: "+number);
        Room room = new Room(world, owner, machine, center, spawnPos, number, players, tunnels);
        rooms.add(room);

        markDirty();
    }

    public void updateOwner(int id, String uuid) {
        Room oldRoom = getRoomByNumber(id);
        if (oldRoom == null) return;
        Room newRoom = new Room(oldRoom.getWorld(), uuid, oldRoom.getMachine(), oldRoom.getCenter(), oldRoom.getSpawnPos(), oldRoom.getNumber(), oldRoom.getPlayers(), oldRoom.getTunnels());
        rooms.remove(oldRoom);
        rooms.add(newRoom);
        markDirty();
    }

    public void updateMachinePos(int id, Identifier world, BlockPos machine) {
        Room oldRoom = getRoomByNumber(id);
        if (oldRoom == null) return;
        Room newRoom = new Room(world, oldRoom.getOwner(), machine, oldRoom.getCenter(), oldRoom.getSpawnPos(), oldRoom.getNumber(), oldRoom.getPlayers(), oldRoom.getTunnels());
        rooms.remove(oldRoom);
        rooms.add(newRoom);
        markDirty();
    }

    public void updateMachinePosAndOwner(int id, Identifier world, BlockPos machine, String uuid) {
        Room oldRoom = getRoomByNumber(id);
        if (oldRoom == null) return;
        Room newRoom = new Room(world, uuid, machine, oldRoom.getCenter(), oldRoom.getSpawnPos(), oldRoom.getNumber(), oldRoom.getPlayers(), oldRoom.getTunnels());
        rooms.remove(oldRoom);
        rooms.add(newRoom);
        markDirty();
    }

    public void rmPlayer(int id, String player) {
        Room oldRoom = getRoomByNumber(id);
        if (oldRoom == null) return;
        List<String> players = new ArrayList<String>(oldRoom.getPlayers());
        if (players.contains(player)) {
            players.remove(player);
            updatePlayers(id, players);
            return;
        }
        CompactMachines.LOGGER.warn("Attempted to remove player from machine #"+id+" but the machine does not contain the player");
    }

    public void addPlayer(int id, String player) {
        Room oldRoom = getRoomByNumber(id);
        if (oldRoom == null) return;
        List<String> players = new ArrayList<String>(oldRoom.getPlayers());
        if (!players.contains(player)) {
            players.add(player);
            updatePlayers(id, players);
            return;
        }
        CompactMachines.LOGGER.warn("Attempted to add player to machine #"+id+" but the machine already contains the player");
    }

    public void updatePlayers(int id, List<String> players) {
        Room oldRoom = getRoomByNumber(id);
        if (oldRoom == null) return;
        Room newRoom = new Room(oldRoom.getWorld(), oldRoom.getOwner(), oldRoom.getMachine(), oldRoom.getCenter(), oldRoom.getSpawnPos(), oldRoom.getNumber(), players, oldRoom.getTunnels());
        rooms.remove(oldRoom);
        rooms.add(newRoom);
        markDirty();
    }

    public void updateSpawnPos(int id, BlockPos pos) {
        Room oldRoom = getRoomByNumber(id);
        if (oldRoom == null) return;
        Room newRoom = new Room(oldRoom.getWorld(), oldRoom.getOwner(), oldRoom.getMachine(), oldRoom.getCenter(), pos, oldRoom.getNumber(), oldRoom.getPlayers(), oldRoom.getTunnels());
        rooms.remove(oldRoom);
        rooms.add(newRoom);
    }

    public void rmTunnel(int id, Tunnel tunnel) {
        Room oldRoom = getRoomByNumber(id);
        if (oldRoom == null) return;
        List<Tunnel> tunnels = new ArrayList<Tunnel>(oldRoom.getTunnels());
        tunnels.forEach(tunnel1 -> {
            System.out.println(tunnel1.toString());
        });
        System.out.println(tunnel.toString());
        if (tunnels.contains(tunnel)) {
            tunnels.remove(tunnel);
            updateTunnels(id, tunnels);
            return;
        }
        CompactMachines.LOGGER.warn("Attempted to remove tunnel from machine #"+id+" but the machine already has the tunnel");
    }

    public void addTunnel(int id, Tunnel tunnel) {
        System.out.println(tunnel.toString());
        Room oldRoom = getRoomByNumber(id);
        if (oldRoom == null) return;
        List<Tunnel> tunnels = new ArrayList<Tunnel>(oldRoom.getTunnels());
//        List<Tunnel> tunnels = new ArrayList<Tunnel>();
        if (!tunnels.contains(tunnel)) {
            tunnels.add(tunnel);
            updateTunnels(id, tunnels);
            return;
        }
        CompactMachines.LOGGER.warn("Attempted to add tunnel to machine #"+id+" but the machine already has the tunnel");
    }

    public void updateTunnels(int id, List<Tunnel> tunnels) {
        Room oldRoom = getRoomByNumber(id);
        if (oldRoom == null) return;
        Room newRoom = new Room(oldRoom.getWorld(), oldRoom.getOwner(), oldRoom.getMachine(), oldRoom.getCenter(), oldRoom.getSpawnPos(), oldRoom.getNumber(), oldRoom.getPlayers(), tunnels);
        rooms.remove(oldRoom);
        rooms.add(newRoom);
        markDirty();
    }

    public void updateTunnel(int id, Tunnel tunnel) {
        Room oldRoom = getRoomByNumber(id);
        if (oldRoom == null) return;
        List<Tunnel> tunnels = oldRoom.getTunnels();
        if (tunnels.size() < 1) {
            addTunnel(id, tunnel);
            return;
        }
        BlockPos pos = tunnel.getPos();
        Tunnel targetTunnel = null;
        for (Tunnel oldTunnel : tunnels) {
            if (oldTunnel.getPos() == pos) {
                targetTunnel = oldTunnel;
            }
        }
        if (targetTunnel == null) return;
        tunnels.remove(targetTunnel);
        tunnels.add(tunnel);
        updateTunnels(id, tunnels);
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

}
