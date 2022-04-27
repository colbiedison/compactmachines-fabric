package us.dison.compactmachines.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.block.entity.MachineWallBlockEntity;
import us.dison.compactmachines.block.entity.TunnelWallBlockEntity;
import us.dison.compactmachines.block.enums.TunnelDirection;
import us.dison.compactmachines.data.persistent.Room;
import us.dison.compactmachines.data.persistent.RoomManager;
import us.dison.compactmachines.data.persistent.tunnel.Tunnel;
import us.dison.compactmachines.data.persistent.tunnel.TunnelType;
import us.dison.compactmachines.item.PSDItem;
import us.dison.compactmachines.item.TunnelItem;
import us.dison.compactmachines.util.TunnelUtil;

import java.util.List;

public abstract class AbstractWallBlock extends BlockWithEntity {

    private final boolean breakable;

    public AbstractWallBlock(Settings settings, boolean breakable) {
        super(settings);
        this.breakable = breakable;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ActionResult s = super.onUse(state, world, pos, player, hand, hit);

        if (world != CompactMachines.cmWorld) return ActionResult.FAIL;
        if (world.getBlockEntity(pos) instanceof TunnelWallBlockEntity) return ActionResult.FAIL;
        if (!(world.getBlockEntity(pos) instanceof MachineWallBlockEntity wall)) return ActionResult.FAIL;

        RoomManager roomManager = CompactMachines.getRoomManager();
        Room room = roomManager.getRoomByNumber(wall.getParentID());
        if (room == null) return ActionResult.FAIL;

        if (player.getStackInHand(hand).getItem() instanceof TunnelItem) {
            ItemStack stack = player.getStackInHand(hand);
            NbtCompound stackNbt = stack.getNbt();
            TunnelType type = TunnelUtil.typeFromNbt(stackNbt);
            if (type != null) {
                world.breakBlock(pos, false);
                world.setBlockState(pos,
                        CompactMachines.BLOCK_WALL_TUNNEL.getDefaultState(),
                        Block.NOTIFY_ALL | Block.FORCE_STATE
                );
                if (world.getBlockEntity(pos) instanceof TunnelWallBlockEntity tunnelWall) {
                    tunnelWall.setParentID(wall.getParentID());
                    tunnelWall.setTunnelType(type);
                    tunnelWall.setConnected(false);
                    world.setBlockState(pos, world.getBlockState(pos));
                    Tunnel tunnel = new Tunnel(
                            wall.getPos(),
                            TunnelDirection.NONE,
                            type,
                            false
                    );
                    if (world.getBlockState(pos).getBlock() instanceof TunnelWallBlock block) {
                        block.setTunnel(tunnel);
                    }
                    roomManager.addTunnel(room.getNumber(), tunnel);
                }
            }

            return ActionResult.SUCCESS;
        }

        if (world.isClient) return ActionResult.SUCCESS;
        if (world.getServer() == null) return ActionResult.FAIL;

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        ServerWorld machineWorld = world.getServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, room.getWorld()));
        BlockPos machinePos = room.getMachine();

        if (player.getStackInHand(hand).getItem() instanceof PSDItem) {
            CompactMachines.LOGGER.info("Teleporting player "+player.getDisplayName().asString()+" out of machine #"+room.getNumber()+" at: "+room.getCenter().toShortString());
            serverPlayer.teleport(machineWorld, machinePos.getX(), machinePos.getY(), machinePos.getZ(), 0, 0);
            roomManager.rmPlayer(room.getNumber(), serverPlayer.getUuidAsString());

            return ActionResult.SUCCESS;
        }
        return s;
    }

    @SuppressWarnings("deprecation")
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (!this.isBreakable()) return 0.0f;
        return super.calcBlockBreakingDelta(state, player, world, pos);
    }

    @Override
    public float getHardness() {
        return this.breakable ? super.getHardness() : -1.0f;
    }

    @Override
    public float getBlastResistance() {
        return this.breakable ? super.getBlastResistance() : 3600000.0f;
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        if (!this.isBreakable()) return List.of(ItemStack.EMPTY);
        return super.getDroppedStacks(state, builder);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public abstract  <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type);

    @Nullable
    @Override
    public abstract BlockEntity createBlockEntity(BlockPos pos, BlockState state);

    public boolean isBreakable() {
        return breakable;
    }

}
