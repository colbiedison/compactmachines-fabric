package us.dison.compactmachines.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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

import java.util.List;

public class MachineWallBlock extends BlockWithEntity {

    private final boolean breakable;

    public MachineWallBlock(Settings settings, boolean breakable) {
        super(settings);
        this.breakable = breakable;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ActionResult s = super.onUse(state, world, pos, player, hand, hit);

        if (world != CompactMachines.cmWorld) return ActionResult.FAIL;
        if (world.isClient) return ActionResult.SUCCESS;
        if (world.getBlockEntity(pos) instanceof TunnelWallBlockEntity) return ActionResult.FAIL;
        if (!(world.getBlockEntity(pos) instanceof MachineWallBlockEntity wall)) return ActionResult.FAIL;
        if (world.getServer() == null) return ActionResult.FAIL;

        RoomManager roomManager = CompactMachines.getRoomManager();
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        Room room = roomManager.getRoomByNumber(wall.getParentID());
        if (room == null) return ActionResult.FAIL;
        ServerWorld machineWorld = world.getServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, room.getWorld()));
        BlockPos machinePos = room.getMachine();

        if (player.getStackInHand(hand).getItem() instanceof PSDItem) {
            CompactMachines.LOGGER.info("Teleporting player "+player.getDisplayName().asString()+" out of machine #"+room.getNumber()+" at: "+room.getCenter().toShortString());
            serverPlayer.teleport(machineWorld, machinePos.getX(), machinePos.getY(), machinePos.getZ(), 0, 0);
            roomManager.rmPlayer(room.getNumber(), serverPlayer.getUuidAsString());

            return ActionResult.SUCCESS;
        } else if (player.getStackInHand(hand).getItem() instanceof TunnelItem) {
            ItemStack stack = player.getStackInHand(hand);
            NbtCompound stackNbt = stack.getNbt();
            if (stackNbt != null) {
                NbtElement typeNbt = stackNbt.get("type");
                if (typeNbt != null) {
                    String strType = typeNbt.asString();
                    TunnelType type = TunnelType.byName(strType);
                    if (type != null) {
                        world.setBlockState(pos,
                                CompactMachines.BLOCK_WALL_TUNNEL.getDefaultState()
                        );

                    }
                }
            }

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
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, CompactMachines.MACHINE_WALL_BLOCK_ENTITY, MachineWallBlockEntity::tick);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MachineWallBlockEntity(pos, state, -1);
    }

    public boolean isBreakable() {
        return breakable;
    }
}
