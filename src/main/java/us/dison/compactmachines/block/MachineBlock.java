package us.dison.compactmachines.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.block.enums.MachineSize;
import us.dison.compactmachines.block.entity.MachineBlockEntity;
import us.dison.compactmachines.data.persistent.RoomManager;
import us.dison.compactmachines.item.PSDItem;
import us.dison.compactmachines.util.PlayerUtil;
import us.dison.compactmachines.util.RoomUtil;

import java.awt.*;
import java.util.Objects;


public class MachineBlock extends Block implements BlockEntityProvider {

    public MachineSize size;

    public MachineBlock(Settings settings, MachineSize machineSize) {
        super(settings);
        this.size = machineSize;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        super.onUse(state, world, pos, player, hand, hit);
        if (!world.isClient()) {
            if (!(world.getBlockEntity(pos) instanceof MachineBlockEntity blockEntity) || world == CompactMachines.cmWorld) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            if (serverPlayer.getStackInHand(hand).getItem() instanceof PSDItem) {
                RoomManager roomManager = CompactMachines.getRoomManager();
                serverPlayer.sendMessage(new LiteralText("Used PSD on a machine block!"), false);

                String ownerName = PlayerUtil.getPlayerNameFromUUID(world.getServer().getUserCache(), blockEntity.getOwner());
                if (ownerName == null) ownerName = "[UNKNOWN]";

                if (blockEntity.getMachineID() == -1) {
                    int machineID = RoomUtil.nextID(roomManager);
                    blockEntity.setMachineID(machineID);
                    blockEntity.setOwner(serverPlayer.getUuid());
                    BlockPos roomCenterPos = RoomUtil.getCenterPosByID(machineID);
                    ChunkPos roomChunkPos = new ChunkPos(roomCenterPos);
                    roomManager.addRoom(world.getRegistryKey().getValue(), serverPlayer.getUuidAsString(), pos, roomCenterPos, machineID);
                    RoomUtil.generateRoom(CompactMachines.cmWorld, machineID, blockEntity.getSize());
                } else {
                    BlockPos bp = RoomUtil.getCenterPosByID(blockEntity.getMachineID());
                    player.sendMessage(new LiteralText("Teleporting to machine #"+blockEntity.getMachineID()+" at: "+bp.toShortString()), false);
                    serverPlayer.teleport(CompactMachines.cmWorld, bp.getX(), bp.getY(), bp.getZ(), 0, 0);
                }

                serverPlayer.sendMessage(new LiteralText("Owner: "+ownerName), false);
            }
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
            player.sendMessage(new LiteralText(String.valueOf(blockEntity.getMachineID())), false);
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient()) {
            if (!(world.getBlockEntity(pos) instanceof MachineBlockEntity blockEntity)) return;
            if (blockEntity.getMachineID() != -1) {
                RoomManager roomManager = CompactMachines.getRoomManager();
                blockEntity.setOwner(placer.getUuid());
                roomManager.updateMachinePosAndOwner(blockEntity.getMachineID(), world.getRegistryKey().getValue(), pos, placer.getUuidAsString());
            }
        }
    }

}
