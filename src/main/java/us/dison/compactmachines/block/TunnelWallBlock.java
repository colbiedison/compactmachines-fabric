package us.dison.compactmachines.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.block.entity.MachineWallBlockEntity;
import us.dison.compactmachines.block.entity.TunnelWallBlockEntity;
import us.dison.compactmachines.block.enums.TunnelDirection;
import us.dison.compactmachines.data.persistent.tunnel.Tunnel;
import us.dison.compactmachines.item.PSDItem;
import us.dison.compactmachines.item.TunnelItem;

public class TunnelWallBlock extends MachineWallBlock {

    public static final EnumProperty<TunnelDirection> CONNECTED_SIDE = EnumProperty.of("connected_side", TunnelDirection.class);

    private Tunnel tunnel;

    public TunnelWallBlock(Settings settings, boolean breakable) {
        super(settings, breakable);
        setDefaultState(getStateManager().getDefaultState().with(CONNECTED_SIDE, TunnelDirection.NONE));
    }

    public TunnelWallBlock(Settings settings, boolean breakable, Tunnel tunnel) {
        this(settings, false);
        this.tunnel = tunnel;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(CONNECTED_SIDE);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, CompactMachines.TUNNEL_WALL_BLOCK_ENTITY, TunnelWallBlockEntity::tick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof TunnelWallBlockEntity wall)) return ActionResult.FAIL;
        if (player.getStackInHand(hand).getItem() instanceof PSDItem) {
            return super.onUse(state, world, pos, player, hand, hit);
        } else if (player.getStackInHand(hand).getItem() instanceof TunnelItem) {
            if (world.isClient()) {
                return ActionResult.PASS;
            } else {
                if (world == CompactMachines.cmWorld) {
                    return ActionResult.SUCCESS;
                } else {
                    return ActionResult.FAIL;
                }
            }
        } else if (player.getStackInHand(hand).equals(ItemStack.EMPTY)) {
            if (world.isClient()) {
                return ActionResult.PASS;
            } else {
                if (world == CompactMachines.cmWorld && player.isSneaking()) {
                    world.setBlockState(pos, CompactMachines.BLOCK_WALL_UNBREAKABLE.getDefaultState());
                    if (world.getBlockEntity(pos) instanceof MachineWallBlockEntity machineWall)
                        machineWall.setParentID(wall.getParentID());

                    return ActionResult.SUCCESS;
                } else {
                    return ActionResult.FAIL;
                }
            }
        }

        return ActionResult.FAIL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TunnelWallBlockEntity(pos, state, -1);
    }

    public void setTunnel(Tunnel tunnel) {
        this.tunnel = tunnel;
    }

    public Tunnel getTunnel() {
        return tunnel;
    }

}
