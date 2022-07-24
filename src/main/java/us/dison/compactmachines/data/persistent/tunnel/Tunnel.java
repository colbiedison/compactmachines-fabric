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
                Codec.BOOL.fieldOf("connectedToFluid").forGetter(Tunnel::isConnectedToFluid),
                Codec.BOOL.fieldOf("connectedToItem").forGetter(Tunnel::isConnectedToItem),
                Codec.BOOL.fieldOf("connectedToEnergy").forGetter(Tunnel::isConnectedToEnergy),
                Codec.BOOL.fieldOf("outgoing").forGetter(Tunnel::isOutgoing)
        )
        .apply(instance, Tunnel::new)
    );

    private final BlockPos pos;
    private final TunnelType type;
    private TunnelDirection face;
    private boolean connectedToItem;
    private boolean connectedToFluid;
    private boolean connectedToEnergy;
    private boolean outgoing;
    public Tunnel(BlockPos pos, TunnelDirection face, TunnelType type, boolean connectedToFluid, boolean connectedToItem, boolean connectedToEnergy, boolean outgoing) {
        this.pos = pos;
        this.face = face;
        this.type = type;
        this.connectedToItem = connectedToItem;
        this.connectedToFluid = connectedToFluid;
        this.connectedToEnergy = connectedToEnergy;
        this.outgoing = outgoing;
    }

    @Override
    public String toString() {
        return String.format("Tunnel { pos: %s, face: %s, type: %s, connectedToItem: %b, connectedToFluid: %b, connectedToEnergy: %b  }",
                pos.toString(), face.asString(), type.asString(), connectedToItem, connectedToFluid, connectedToEnergy
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Tunnel other)) return false;
        if (this == other) return true;
        if (
                this.getPos().getX() == other.getPos().getX() &&
                this.getPos().getY() == other.getPos().getY() &&
                this.getPos().getZ() == other.getPos().getZ() &&
                this.getFace().name().equals(other.getFace().name()) &&
                this.getType().name().equals(other.getType().name()) &&
                this.isConnectedToItem() == other.isConnectedToItem() &&
                this.isConnectedToFluid() == other.isConnectedToFluid() &&
                this.isConnectedToEnergy() == other.isConnectedToEnergy()
        ) return true;

        return false;
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

    public boolean isConnectedToItem() {
        return connectedToItem;
    }
    public boolean isConnectedToFluid() {
        return connectedToFluid;
    }
    public boolean isConnectedToEnergy() {
        return connectedToEnergy;
    }
    public boolean isOutgoing() {
        return outgoing;
    }
    public Tunnel withConnectedToItem(boolean connected) {
        return new Tunnel(
                pos,
                face,
                type,
                connectedToFluid,
                connected,
                connectedToEnergy,
                outgoing);
    }
    public Tunnel withConnectedToFluid(boolean connected) {
        return new Tunnel(
                pos,
                face,
                type,
                connected,
                connectedToItem,
                connectedToEnergy,
                outgoing);
    }
    public Tunnel withConnectedToEnergy(boolean connected) {
        return new Tunnel(
                pos,
                face,
                type,
                connectedToFluid,
                connectedToItem,
                connected,
                outgoing);
    }
    public Tunnel withOutgoing(boolean outgoing) {
        return new Tunnel(
                this.pos,
                this.face,
                this.type,
                this.connectedToFluid,
                this.connectedToItem,
                this.connectedToEnergy,
                outgoing);
    }
    public Tunnel withFace(TunnelDirection face) {
        return new Tunnel(
                this.getPos(),
                face,
                this.getType(),
                this.isConnectedToFluid(),
                this.isConnectedToItem(),
                this.isConnectedToEnergy(),
                this.isOutgoing()
        );
    }

}
