package us.dison.compactmachines.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

import static us.dison.compactmachines.CompactMachines.ID_WALL_UNBREAKABLE;
import static us.dison.compactmachines.CompactMachines.MODID;

@Environment(EnvType.CLIENT)
public class CompactMachinesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        // REGISTER machine tooltip callback
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (Registry.ITEM.getId(stack.getItem()).toString().startsWith(MODID + ":machine_") && stack.getNbt() != null) { // Machine number tooltip
                lines.add(1, new LiteralText("ID: " + stack.getSubNbt("BlockEntityTag").getInt("number")).formatted(Formatting.GRAY));
            } else
            if (Registry.ITEM.getId(stack.getItem()).equals(ID_WALL_UNBREAKABLE)) { // Unbreakable wall tooltip
                lines.add(1, new LiteralText("Unbreakable").formatted(Formatting.RED));
            }
        });

    }
}
