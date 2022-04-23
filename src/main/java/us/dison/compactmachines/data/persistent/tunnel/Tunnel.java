package us.dison.compactmachines.data.persistent.tunnel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import us.dison.compactmachines.block.enums.TunnelDirection;

public class Tunnel {

    public static final Codec<Tunnel> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Tunnel::getPos),
                TunnelDirection.CODEC.fieldOf("face").forGetter(Tunnel::getFace),
                TunnelType.CODEC.fieldOf("type").forGetter(Tunnel::getType),
                Codec.BOOL.fieldOf("isConnected").forGetter(Tunnel::isConnected)
        )
        .apply(instance, Tunnel::new)
    );

    private final BlockPos pos;
    private final TunnelType type;
    private TunnelDirection face;
    private boolean isConnected;

    public Tunnel(BlockPos pos, TunnelDirection face, TunnelType type, boolean isConnected) {
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

    public TunnelDirection getFace() {
        return face;
    }

    public boolean isConnected() {
        return isConnected;
    }

}