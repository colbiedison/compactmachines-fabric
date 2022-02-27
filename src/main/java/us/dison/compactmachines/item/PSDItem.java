package us.dison.compactmachines.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import us.dison.compactmachines.gui.PSDScreen;

public class PSDItem extends Item {
    public PSDItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        super.use(world, user, hand);
        if (!world.isClient()) {
            user.sendMessage(new LiteralText("Used PSD!"), false);
        } else {
            MinecraftClient.getInstance().setScreen(new PSDScreen(new LiteralText("asdf")));
        }

        return TypedActionResult.success(user.getStackInHand(hand));
    }

}
