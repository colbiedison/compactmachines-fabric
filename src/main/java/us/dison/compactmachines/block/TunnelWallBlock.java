package us.dison.compactmachines.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtString;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.block.entity.MachineBlockEntity;
import us.dison.compactmachines.block.entity.MachineWallBlockEntity;
import us.dison.compactmachines.block.entity.TunnelWallBlockEntity;
import us.dison.compactmachines.block.enums.TunnelDirection;
import us.dison.compactmachines.data.persistent.Room;
import us.dison.compactmachines.data.persistent.RoomManager;
import us.dison.compactmachines.data.persistent.tunnel.Tunnel;
import us.dison.compactmachines.util.TunnelUtil;

public class TunnelWallBlock extends AbstractWallBlock {

    public static final EnumProperty<TunnelDirection> CONNECTED_SIDE = EnumProperty.of("connected_side", TunnelDirection.class);
    private Tunnel tunnel;

    public TunnelWallBlock(Settings settings, boolean breakable) {
        super(settings, breakable);
        setDefaultState(getStateManager().getDefaultState()
                .with(CONNECTED_SIDE, TunnelDirection.NONE));
        
    }

    @Override 
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
            if (!(world.getBlockEntity(pos) instanceof TunnelWallBlockEntity wall)) return; 
            final MachineBlockEntity machineBlockEntity = wall.getMachineEntity();
            final World machineWorld = machineBlockEntity.getWorld();
            // don't ask
            machineWorld.updateNeighborsAlways(machineBlockEntity.getPos(), machineWorld.getBlockState(machineBlockEntity.getPos()).getBlock());
            CompactMachines.LOGGER.info("updating machine block");
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

        if (player.getStackInHand(hand).equals(ItemStack.EMPTY)) {
            if (world.isClient()) {

            } else {
                if (world == CompactMachines.cmWorld) {
                    RoomManager roomManager = CompactMachines.getRoomManager();
                    Room room = roomManager.getRoomByNumber(wall.getParentID());
                    if (player.isSneaking()) {
                        if (!(world.getBlockEntity(pos) instanceof TunnelWallBlockEntity tunnelWallEntity)) return ActionResult.FAIL;
                        world.setBlockState(pos, CompactMachines.BLOCK_WALL_UNBREAKABLE.getDefaultState());
                        if (!(world.getBlockEntity(pos) instanceof MachineWallBlockEntity wallEntity)) return ActionResult.FAIL;
                        wallEntity.setParentID(wall.getParentID());
                        Tunnel t = TunnelUtil.fromRoomAndPos(room, pos);
                        if (t == null) return ActionResult.FAIL;
                        roomManager.rmTunnel(room.getNumber(), t);
                        // Spawn tunnel item
                        ItemStack itemStack = CompactMachines.ITEM_TUNNEL.getDefaultStack();
                        itemStack.setSubNbt("type", NbtString.of(tunnelWallEntity.getTunnelType().asString()));
                        BlockPos itemPos = pos.offset(hit.getSide(), 1);
                        ItemEntity itemEntity = new ItemEntity(world, itemPos.getX() + 0.5, itemPos.getY(), itemPos.getZ() + 0.5, itemStack);
                        world.spawnEntity(itemEntity);
                    } else {
                        BlockState blockState = world.getBlockState(pos);
                        if (blockState.getBlock() instanceof TunnelWallBlock block && hand == Hand.MAIN_HAND) {
//                            Tunnel oldTunnel = block.getTunnel();
                            if (!(world.getBlockEntity(pos) instanceof TunnelWallBlockEntity tunnelEntity)) return ActionResult.FAIL;
                            Tunnel oldTunnel = tunnelEntity.getTunnel();
//                            block.setTunnel(new Tunnel(oldTunnel.getPos(), oldTunnel.getFace(), oldTunnel.getType(), oldTunnel.isConnected()));
                            TunnelDirection nextSide = TunnelUtil.nextSide(blockState.get(CONNECTED_SIDE));
                            world.setBlockState(pos, blockState.with(CONNECTED_SIDE, nextSide));
                            Tunnel newTunnel = TunnelUtil.rotate(oldTunnel);
                            block.setTunnel(newTunnel);
                            roomManager.updateTunnel(room.getNumber(), newTunnel);
                            player.sendMessage(
                                new TranslatableText("compactmachines.direction.side",
                                    new TranslatableText("compactmachines.direction."+nextSide.name().toLowerCase())
                                ), true
                            );
                        }
                    }

                    return ActionResult.SUCCESS;
                } else {
                    return ActionResult.FAIL;
                }
            }
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TunnelWallBlockEntity(pos, state);
    }

    public void setTunnel(Tunnel tunnel) {
        this.tunnel = tunnel;
    }

    public Tunnel getTunnel() {
        return tunnel;
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    
    }



}
