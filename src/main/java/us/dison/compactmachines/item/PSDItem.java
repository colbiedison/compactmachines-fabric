package us.dison.compactmachines.item;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.block.entity.MachineWallBlockEntity;
import us.dison.compactmachines.data.persistent.RoomManager;
import us.dison.compactmachines.gui.PSDScreen;

public class PSDItem extends Item {
    public PSDItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        super.use(world, user, hand);
        if (world.isClient()) {
            if (user.isSneaking()) return TypedActionResult.pass(user.getStackInHand(hand));
            MinecraftClient.getInstance().setScreen(new PSDScreen(new TranslatableText("compactmachines.psd.pages.machines.title")));
        }

        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        super.useOnBlock(context);
        if (context.getWorld().isClient()) {
            return ActionResult.FAIL;
        } else  {

            if (!context.getPlayer().isSneaking()) return ActionResult.PASS;
            if (!((context.getWorld() instanceof ServerWorld serverWorld))) return ActionResult.FAIL;
            if (serverWorld != CompactMachines.cmWorld) return ActionResult.FAIL;
            BlockEntity blockEntity = serverWorld.getBlockEntity(context.getBlockPos());
            if (!(blockEntity instanceof MachineWallBlockEntity wallBlockEntity)) return ActionResult.FAIL;

            RoomManager roomManager = CompactMachines.getRoomManager();
            RoomManager.Room room = roomManager.getRoomByNumber(wallBlockEntity.getParentID());
            if (room == null) return ActionResult.FAIL;

            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) context.getPlayer();

            if (room.getSpawnPos() != wallBlockEntity.getPos()) {
                if ( // it's a floor block
                        serverWorld.getBlockState(wallBlockEntity.getPos().add(0, 1, 0)).getBlock() != CompactMachines.BLOCK_WALL_UNBREAKABLE
                        && wallBlockEntity.getPos().getY() < room.getCenter().getY()
                ) {
                    roomManager.updateSpawnPos(room.getNumber(), wallBlockEntity.getPos());
                    serverPlayer.sendMessage(new TranslatableText("message.compactmachines.spawnpoint_set"), true);
                    return ActionResult.SUCCESS;
                }

                return ActionResult.FAIL;
            }

            return ActionResult.SUCCESS;
        }
    }
}
