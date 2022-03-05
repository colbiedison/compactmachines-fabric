package us.dison.compactmachines.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.block.entity.TunnelWallBlockEntity;
import us.dison.compactmachines.data.persistent.tunnel.Tunnel;

public class TunnelWallBlock extends MachineWallBlock {

    private Tunnel tunnel;

    public TunnelWallBlock(Settings settings, boolean breakable) {
        super(settings, breakable);
    }

    public TunnelWallBlock(Settings settings, boolean breakable, Tunnel tunnel) {
        this(settings, false);
        this.tunnel = tunnel;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, CompactMachines.TUNNEL_WALL_BLOCK_ENTITY, TunnelWallBlockEntity::tick);
    }

    public Tunnel getTunnel() {
        return tunnel;
    }

}
