package us.dison.compactmachines.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.block.enums.MachineSize;
import us.dison.compactmachines.block.entity.MachineBlockEntity;
import us.dison.compactmachines.data.persistent.RoomManager;
import us.dison.compactmachines.item.PSDItem;
import us.dison.compactmachines.util.RoomUtil;

import java.util.ArrayList;


public class MachineBlock extends BlockWithEntity {

    public final MachineSize size;

    private int lastPlayerInsideWarning;

    public MachineBlock(Settings settings, MachineSize machineSize) {
        super(settings);
        this.size = machineSize;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        super.onUse(state, world, pos, player, hand, hit);
        if (!world.isClient()) {

            if (!(world.getBlockEntity(pos) instanceof MachineBlockEntity blockEntity) || world == CompactMachines.cmWorld) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            if (serverPlayer.getStackInHand(hand).getItem() instanceof PSDItem) {

                RoomManager roomManager = CompactMachines.getRoomManager();

                if (blockEntity.getMachineID() == -1) { // make new room
                    int machineID = RoomUtil.nextID(roomManager);
                    blockEntity.setMachineID(machineID);
                    blockEntity.setOwner(serverPlayer.getUuid());
                    BlockPos roomCenterPos = RoomUtil.getCenterPosByID(machineID);
                    roomManager.addRoom(world.getRegistryKey().getValue(), serverPlayer.getUuidAsString(), pos, roomCenterPos, machineID, new ArrayList<>());
                    serverPlayer.sendMessage(new TranslatableText("message.compactmachines.generating_room"), true);
                    RoomUtil.generateRoom(CompactMachines.cmWorld, machineID, blockEntity.getSize());
                    serverPlayer.sendMessage(new TranslatableText("message.compactmachines.ready").formatted(Formatting.GREEN), true);
                } else { // teleport player into room
                    int id = blockEntity.getMachineID();
                    if (!roomManager.roomExists(id)) {
                        CompactMachines.LOGGER.error("Player "+player.getDisplayName().asString()+" attempted to enter a machine with invalid id! (#"+id+")");
                        player.sendMessage(new TranslatableText("message.compactmachines.invalid_room").formatted(Formatting.RED), false);
                        return ActionResult.PASS;
                    }
                    BlockPos bp = RoomUtil.getCenterPosByID(id);
                    bp = bp.add(0, -(size.getSize()/2d)+1, 0);
                    CompactMachines.LOGGER.info("Teleporting player "+player.getDisplayName().asString()+" into machine #"+blockEntity.getMachineID()+" at: "+bp.toShortString());
                    serverPlayer.teleport(CompactMachines.cmWorld, bp.getX()+0.5d, bp.getY(), bp.getZ()+0.5d, 0, 0);
                    roomManager.addPlayer(id, serverPlayer.getUuidAsString());
                    return ActionResult.SUCCESS;
                }
            } else {
                return ActionResult.FAIL;
            }
        }
        else {
            if (!(world.getBlockEntity(pos) instanceof MachineBlockEntity blockEntity) || world == CompactMachines.cmWorld) return ActionResult.PASS;
            if (!(player instanceof ClientPlayerEntity clientPlayer)) return ActionResult.PASS;

            if (clientPlayer.getStackInHand(hand).getItem() instanceof PSDItem) {

                RoomManager roomManager = CompactMachines.getRoomManager();

                if (blockEntity.getMachineID() == -1) {
                    int machineID = RoomUtil.nextID(roomManager);
                    blockEntity.setMachineID(machineID);
                    blockEntity.setOwner(clientPlayer.getUuid());
                }
                return ActionResult.SUCCESS;
            }

            return ActionResult.FAIL;
        }

        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MachineBlockEntity(pos, state, this.size);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        if (!(world.getBlockEntity(pos) instanceof MachineBlockEntity blockEntity)) return;
        if (!world.isClient()) {
            if (blockEntity.getMachineID() == -1 && player.isCreative()) return;
            ItemStack itemStack = this.asItem().getDefaultStack();
            blockEntity.setStackNbt(itemStack);
            ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, itemStack);
            world.spawnEntity(itemEntity);
        }
    }

    @Override
    public ItemStack getPickStack(BlockView blockView, BlockPos pos, BlockState state) {
        ItemStack normalStack = super.getPickStack(blockView, pos, state);

        if (!(blockView.getBlockEntity(pos).getWorld() instanceof ClientWorld clientWorld)) return normalStack;
        if (!(clientWorld.getBlockEntity(pos) instanceof MachineBlockEntity blockEntity)) return normalStack;
        if (blockEntity.getMachineID() == -1) return normalStack;

        ItemStack itemStack = this.asItem().getDefaultStack();
        blockEntity.setStackNbt(itemStack);

        return itemStack;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (!(world.getBlockEntity(pos) instanceof MachineBlockEntity blockEntity)) return;

        blockEntity.setOwner(placer.getUuid());
        blockEntity.markDirty();
        if (!world.isClient()) {
            if (blockEntity.getMachineID() != -1) {
                RoomManager roomManager = CompactMachines.getRoomManager();
                roomManager.updateMachinePosAndOwner(blockEntity.getMachineID(), world.getRegistryKey().getValue(), pos, placer.getUuidAsString());
            }
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, CompactMachines.MACHINE_BLOCK_ENTITY, MachineBlockEntity::tick);
    }

    @SuppressWarnings("deprecation")
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float original = super.calcBlockBreakingDelta(state, player, world, pos);
        RoomManager roomManager = CompactMachines.getRoomManager();
        // Don't let the machine be broken if there is a player inside
        if (world.getBlockEntity(pos) instanceof MachineBlockEntity machineBlockEntity
                && roomManager.getRoomByNumber(machineBlockEntity.getMachineID()) != null) {
            if (roomManager.getRoomByNumber(machineBlockEntity.getMachineID()).getPlayers().size() < 1) return original;
            if (player instanceof ServerPlayerEntity serverPlayer) {
                // Send warning message every 10 ticks
                int now = serverPlayer.server.getTicks();
                if (now >= lastPlayerInsideWarning+10) {
                    serverPlayer.sendMessage(new TranslatableText("message.compactmachines.player_inside").formatted(Formatting.RED), true);
                    lastPlayerInsideWarning = now;
                }
            }
            return 0.0f;
        }

        return original;
    }

}
