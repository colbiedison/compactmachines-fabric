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
import us.dison.compactmachines.block.entity.TunnelWallBlockEntity;
import us.dison.compactmachines.data.persistent.Room;
import us.dison.compactmachines.data.persistent.RoomManager;
import us.dison.compactmachines.gui.PSDScreen;

public class TunnelItem extends Item {
    public TunnelItem(Settings settings) {
        super(settings);
    }

//    @Override
//    public ActionResult useOnBlock(ItemUsageContext context) {
//        super.useOnBlock(context);
//        if (context.getWorld().isClient()) {
//            return ActionResult.FAIL;
//        } else  {
//
//            if (!((context.getWorld() instanceof ServerWorld serverWorld))) return ActionResult.FAIL;
//            if (serverWorld != CompactMachines.cmWorld) return ActionResult.FAIL;
//            BlockEntity blockEntity = serverWorld.getBlockEntity(context.getBlockPos());
//            if (blockEntity instanceof TunnelWallBlockEntity tunnelBlockEntity) {
//                return ActionResult.FAIL;
//            } else if (blockEntity instanceof MachineWallBlockEntity wallBlockEntity) {
//                if (context.getPlayer().isSneaking()) {
//                    return ActionResult.FAIL;
//                } else {
//                    // add tunnel
//                    RoomManager roomManager = CompactMachines.getRoomManager();
//                    Room room = roomManager.getRoomByNumber(wallBlockEntity.getParentID());
//                    if (room == null) return ActionResult.FAIL;
//
//                    ServerPlayerEntity serverPlayer = (ServerPlayerEntity) context.getPlayer();
//
//                    return ActionResult.SUCCESS;
//                }
//            } else {
//                return ActionResult.FAIL;
//            }
//
//        }
//    }
}
