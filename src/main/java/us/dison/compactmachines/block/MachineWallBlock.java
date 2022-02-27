package us.dison.compactmachines.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.block.entity.MachineWallBlockEntity;
import us.dison.compactmachines.data.persistent.RoomManager;
import us.dison.compactmachines.item.PSDItem;

public class MachineWallBlock extends Block implements BlockEntityProvider {

    private final boolean breakable;

    public MachineWallBlock(Settings settings, boolean breakable) {
        super(settings);
        this.breakable = breakable;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        super.onUse(state, world, pos, player, hand, hit);
        if (player.getStackInHand(hand).getItem() instanceof PSDItem) {
            if (world == CompactMachines.cmWorld) {
                if (!world.isClient()) {
                    ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                    serverPlayer.sendMessage(new LiteralText("Used PSD on machine wall!"), false);

                    if (!(world.getBlockEntity(pos) instanceof MachineWallBlockEntity wall)) return ActionResult.PASS;
                    if (world.getServer() == null) return ActionResult.PASS;

                    RoomManager.Room room = CompactMachines.getRoomManager().getRoomByNumber(wall.getParentID());
                    ServerWorld machineWorld = world.getServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, room.getWorld()));
                    BlockPos machinePos = room.getMachine();
                    serverPlayer.teleport(machineWorld, machinePos.getX(), machinePos.getY(), machinePos.getZ(), 0, 0);

                    return ActionResult.SUCCESS;
                }
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public float getHardness() {
        return this.breakable ? super.getHardness() : -1.0f;
    }

    @Override
    public float getBlastResistance() {
        return this.breakable ? super.getBlastResistance() : -1.0f;
    }

    public boolean isBreakable() {
        return breakable;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MachineWallBlockEntity(pos, state, -1);
    }
}
