package us.dison.compactmachines.data.persistent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import us.dison.compactmachines.data.persistent.tunnel.Tunnel;

import java.util.List;

public class Room {

    public static final Codec<Room> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                            Identifier.CODEC.fieldOf("world").forGetter(Room::getWorld),
                            Codec.STRING.fieldOf("owner").forGetter(Room::getOwner),
                            BlockPos.CODEC.fieldOf("machine").forGetter(Room::getMachinePos),
                            BlockPos.CODEC.fieldOf("center").forGetter(Room::getCenter),
                            BlockPos.CODEC.fieldOf("spawnPos").forGetter(Room::getSpawnPos),
                            Codec.INT.fieldOf("number").forGetter(Room::getNumber),
                            Codec.list(Codec.STRING).fieldOf("players").forGetter(Room::getPlayers),
                            Codec.list(Tunnel.CODEC).fieldOf("tunnels").forGetter(Room::getTunnels)
                    )
                    .apply(instance, Room::new));

    private final Identifier world;
    private final String owner;
    private final BlockPos machine;
    private final BlockPos center;
    private final BlockPos spawnPos;
    private final int number;
    private final List<String> players;
    private final List<Tunnel> tunnels;


    public Room(Identifier world, String owner, BlockPos machine, BlockPos center, BlockPos spawnPos, int number, List<String> players, List<Tunnel> tunnels) {
        this.world = world;
        this.owner = owner;
        this.machine = machine;
        this.center = center;
        this.spawnPos = spawnPos;
        this.number = number;
        this.players = players;
        this.tunnels = tunnels;
    }


    public Identifier getWorld() {
        return world;
    }

    public String getOwner() {
        return owner;
    }

    public BlockPos getMachinePos() {
        return machine;
    }

    public BlockPos getCenter() {
        return center;
    }

    public BlockPos getSpawnPos() {
        return spawnPos;
    }

    public int getNumber() {
        return number;
    }

    public List<String> getPlayers() {
        return players;
    }

    public List<Tunnel> getTunnels() {
        return tunnels;
    }
}
