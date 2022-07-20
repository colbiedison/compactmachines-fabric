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
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.block.enums.MachineSize;
import us.dison.compactmachines.block.entity.MachineBlockEntity;
import us.dison.compactmachines.data.persistent.Room;
import us.dison.compactmachines.data.persistent.RoomManager;
import us.dison.compactmachines.data.persistent.tunnel.Tunnel;
import us.dison.compactmachines.data.persistent.tunnel.TunnelType;
import us.dison.compactmachines.item.PSDItem;
import us.dison.compactmachines.util.RedstoneUtil;
import us.dison.compactmachines.util.RoomUtil;

import java.util.ArrayList;

public class MachineBlock extends BlockWithEntity {
   
    public final MachineSize size;

    private int lastPlayerInsideWarning;
    public static boolean doPassWires = true;
    public MachineBlock(Settings settings, MachineSize machineSize) {
        super(settings);
        this.size = machineSize;
    }
    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        super.onUse(state, world, pos, player, hand, hit);
        if (!world.isClient()) {


            if (!(world.getBlockEntity(pos) instanceof MachineBlockEntity blockEntity)) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            if (serverPlayer.getStackInHand(hand).getItem() instanceof PSDItem) {
                // this code is to work around exiting, which is busted
                if (world == CompactMachines.cmWorld) {
                    serverPlayer.sendMessage(new TranslatableText("message.compactmachines.cannot_enter"), false);
                    return ActionResult.PASS;
                }
                
                RoomManager roomManager = CompactMachines.getRoomManager();

                if (blockEntity.getMachineID() == -1) { // make new room
                    int machineID = RoomUtil.nextID(roomManager);
                    blockEntity.setMachineID(machineID);
                    blockEntity.setOwner(serverPlayer.getUuid());
                    BlockPos roomCenterPos = RoomUtil.getCenterPosByID(machineID);
                    BlockPos spawnPos = roomCenterPos.add(0, -(size.getSize()/2d)+1, 0);
                    roomManager.addRoom(world.getRegistryKey().getValue(), serverPlayer.getUuidAsString(), pos, roomCenterPos, spawnPos, machineID, new ArrayList<>(), new ArrayList<>());
                    serverPlayer.sendMessage(new TranslatableText("message.compactmachines.generating_room"), true);
                    RoomUtil.generateRoom(CompactMachines.cmWorld, machineID, blockEntity.getSize());
                    serverPlayer.sendMessage(new TranslatableText("message.compactmachines.ready").formatted(Formatting.GREEN), true);
                } else { // teleport player into room
                    int id = blockEntity.getMachineID();
                    Room room = roomManager.getRoomByNumber(id);
                    if (room == null) {
                        CompactMachines.LOGGER.error("Player "+player.getDisplayName().asString()+" attempted to enter a machine with invalid id! (#"+id+")");
                        player.sendMessage(new TranslatableText("message.compactmachines.invalid_room").formatted(Formatting.RED), false);
                        return ActionResult.PASS;
                    }
                    BlockPos spawnPos = room.getSpawnPos();
                    CompactMachines.LOGGER.info("Teleporting player "+player.getDisplayName().asString()+" into machine #"+blockEntity.getMachineID()+" at: "+spawnPos.toShortString());
                    serverPlayer.teleport(CompactMachines.cmWorld, spawnPos.getX()+0.5d, spawnPos.getY()+1d, spawnPos.getZ()+0.5d, 0, 0);
                    roomManager.addPlayer(id, serverPlayer.getUuidAsString());
                    if (world == CompactMachines.cmWorld) {
                        
                    }
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
            ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, itemStack);
            if (blockEntity.getMachineID() != -1)
                blockEntity.setStackNbt(itemStack);
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
    @Override 
    public void neighborUpdate(BlockState state, World world,BlockPos pos, Block block, BlockPos fromPos,  boolean notify) {
        final RoomManager roomManager = CompactMachines.getRoomManager();
        if (world.getBlockEntity(pos) instanceof MachineBlockEntity machineBlockEntity
                && roomManager.getRoomByNumber(machineBlockEntity.getMachineID()) != null) {
            for (Tunnel tunnel : roomManager.getRoomByNumber(machineBlockEntity.getMachineID()).getTunnels()) {
                if (tunnel.getFace().toDirection() == null) continue;
                if (tunnel.getType() != TunnelType.REDSTONE) continue;
                // makes sense because only 1 wall tunnel instance exists
                TunnelWallBlock.doPassWires = false;
                CompactMachines.cmWorld.updateNeighborsAlways(tunnel.getPos(), CompactMachines.BLOCK_WALL_TUNNEL); 
                TunnelWallBlock.doPassWires = true;
            }
        }

    }
    @Override 
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }
    @Override 
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        Tunnel tunnel = getTunnelOf(pos, world, direction.getOpposite(), TunnelType.REDSTONE);
        if (tunnel != null) {
            return RedstoneUtil.getPower(CompactMachines.cmWorld, tunnel.getPos(), doPassWires);
        }
        return 0;
    }
    @Nullable 
    private Tunnel getTunnelOf(BlockPos pos, BlockView world, Direction direction, TunnelType tunnelType) {
        RoomManager roomManager = CompactMachines.getRoomManager();
        if (world.getBlockEntity(pos) instanceof MachineBlockEntity machineBlockEntity 
                && roomManager.getRoomByNumber(machineBlockEntity.getMachineID()) != null) {
            for (Tunnel tunnel : roomManager.getRoomByNumber(machineBlockEntity.getMachineID()).getTunnels()) {
                if (tunnel.getFace().toDirection() == direction 
                        && tunnel.getType() == tunnelType)
                    return tunnel; 
            }            
        }
        return null;
    }
    @Nullable
    public MachineBlockEntity getMachineEntity(BlockPos pos, BlockView world) {
        RoomManager roomManager = CompactMachines.getRoomManager();
        if (world.getBlockEntity(pos) instanceof MachineBlockEntity machineBlockEntity
                && roomManager.getRoomByNumber(machineBlockEntity.getMachineID()) != null) {
                    return machineBlockEntity;
                }
        return null;
    }

}
