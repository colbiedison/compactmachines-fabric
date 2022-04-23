package us.dison.compactmachines.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.block.TunnelWallBlock;
import us.dison.compactmachines.block.enums.MachineSize;

import java.util.UUID;

import static us.dison.compactmachines.CompactMachines.*;

@Environment(EnvType.CLIENT)
public class CompactMachinesClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {

        // REGISTER machine tooltip callback
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {

            if (Registry.ITEM.getId(stack.getItem()).toString().startsWith(MODID + ":machine_")) { // Machine number tooltip
                MachineSize machineSize = null;
                String strSize = Registry.ITEM.getId(stack.getItem()).getPath().split("_")[1];
                if (strSize != null) {
                    machineSize = MachineSize.getFromSize(strSize);
                }

                if (machineSize != null)
                    lines.add(1, new TranslatableText("tooltip.compactmachines.machine.size", machineSize.getSize(), machineSize.getSize(), machineSize.getSize()).formatted(Formatting.GRAY));

                if (stack.getNbt() != null) {
                    UUID owner = stack.getSubNbt("BlockEntityTag").getUuid("uuid");
                    String playerName = owner.toString();
                    ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
                    if (handler != null) {
                        PlayerListEntry playerListEntry = handler.getPlayerListEntry(owner);
                        if (playerListEntry != null) {
                            playerName = playerListEntry.getProfile().getName();
                        }
                    }
                    if (context.isAdvanced()) lines.add(1, new TranslatableText("tooltip.compactmachines.machine.owner", playerName).formatted(Formatting.GRAY));
                    lines.add(1, new TranslatableText("tooltip.compactmachines.machine.id", stack.getSubNbt("BlockEntityTag").getInt("number")).formatted(Formatting.GRAY));
                }

            } else
            if (Registry.ITEM.getId(stack.getItem()).equals(ID_WALL_UNBREAKABLE)) { // Unbreakable wall tooltip
                lines.add(1, new TranslatableText("tooltip.compactmachines.details.solid_wall").formatted(Formatting.RED));
            }
        });

        // Make Tunnel Wall Blocks have transparent render layers
        BlockRenderLayerMap.INSTANCE.putBlock(CompactMachines.BLOCK_WALL_TUNNEL, RenderLayer.getCutout());

        // Tint Tunnel Wall Blocks
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            TunnelWallBlock block = (TunnelWallBlock)state.getBlock();
            if (block.getTunnel() == null) return 0xff00ff;

            return switch (tintIndex) {
                case 0 -> block.getTunnel().getType().getColor();
                case 1 -> block.getTunnel().isConnected() ? 0x2222aa : 0x222255;
                default -> 0xff00ff;
            };
        }, BLOCK_WALL_TUNNEL);
    }
}
