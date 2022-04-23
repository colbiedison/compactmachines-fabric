package us.dison.compactmachines.data.persistent.tunnel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Tunnel {

    public static final Codec<Tunnel> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Tunnel::getPos),
                Direction.CODEC.fieldOf("face").forGetter(Tunnel::getFace),
                TunnelType.CODEC.fieldOf("type").forGetter(Tunnel::getType),
                Codec.BOOL.fieldOf("isConnected").forGetter(Tunnel::isConnected)
        )
        .apply(instance, Tunnel::new)
    );

    private final BlockPos pos;
    private final TunnelType type;
    private Direction face;
    private boolean isConnected;

    public Tunnel(BlockPos pos, Direction face, TunnelType type, boolean isConnected) {
        this.pos = pos;
        this.face = face;
        this.type = type;
        this.isConnected = isConnected;
    }

    public BlockPos getPos() {
        return pos;
    }

    public TunnelType getType() {
        return type;
    }

    public Direction getFace() {
        return face;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setFace(Direction face) {
        this.face = face;
    }

}
