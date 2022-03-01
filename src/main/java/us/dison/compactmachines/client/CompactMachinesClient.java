package us.dison.compactmachines.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

import java.util.UUID;

import static us.dison.compactmachines.CompactMachines.ID_WALL_UNBREAKABLE;
import static us.dison.compactmachines.CompactMachines.MODID;

@Environment(EnvType.CLIENT)
public class CompactMachinesClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {

        // REGISTER machine tooltip callback
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (Registry.ITEM.getId(stack.getItem()).toString().startsWith(MODID + ":machine_") && stack.getNbt() != null) { // Machine number tooltip
                UUID owner = stack.getSubNbt("BlockEntityTag").getUuid("uuid");
                String playerName = owner.toString();
                ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
                if (handler != null) {
                    PlayerListEntry playerListEntry = handler.getPlayerListEntry(owner);
                    if (playerListEntry != null) {
                        playerName = playerListEntry.getProfile().getName();
                    }
                }

                lines.add(1, new TranslatableText("tooltip.compactmachines.machine.owner", playerName).formatted(Formatting.GRAY));
                lines.add(1, new TranslatableText("tooltip.compactmachines.machine.id", stack.getSubNbt("BlockEntityTag").getInt("number")).formatted(Formatting.GRAY));
            } else
            if (Registry.ITEM.getId(stack.getItem()).equals(ID_WALL_UNBREAKABLE)) { // Unbreakable wall tooltip
                lines.add(1, new TranslatableText("tooltip.compactmachines.details.solid_wall").formatted(Formatting.RED));
            }
        });



    }
}
