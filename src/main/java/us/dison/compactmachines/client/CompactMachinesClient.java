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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import us.dison.compactmachines.CompactMachines;
import us.dison.compactmachines.block.TunnelWallBlock;
import us.dison.compactmachines.block.entity.TunnelWallBlockEntity;
import us.dison.compactmachines.block.enums.MachineSize;
import us.dison.compactmachines.data.persistent.tunnel.TunnelType;

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
            } else
            if (Registry.ITEM.getId(stack.getItem()).equals(ID_TUNNEL)) { // Tunnel item
                NbtCompound stackNbt = stack.getNbt();
                Text errMsg = null;
                if (stackNbt == null) {
                    errMsg = new TranslatableText("compactmachines.errors.no_data");
                } else {
                    NbtElement typeNbt = stackNbt.get("type");
                    if (typeNbt == null) {
                        errMsg = new TranslatableText("compactmachines.errors.no_type");
                    } else {
                        TunnelType type = TunnelType.byName(typeNbt.asString());
                        if (type == null) {
                            errMsg = new LiteralText(typeNbt.asString());
                        }
                    }
                }

                if (errMsg != null) {
                    lines.add(1, new TranslatableText("compactmachines.errors.unknown_tunnel_type", errMsg).formatted(Formatting.RED));
                }
            }
        });

        // Make Tunnel Wall Blocks have transparent render layers
        BlockRenderLayerMap.INSTANCE.putBlock(CompactMachines.BLOCK_WALL_TUNNEL, RenderLayer.getCutout());

        // Tint Tunnel Wall Blocks
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {

            if (!(world.getBlockEntity(pos) instanceof TunnelWallBlockEntity tunnelWall)) return 0xff00ff;
            Object ra = tunnelWall.getRenderAttachmentData();
            TunnelWallBlockEntity.RenderAttachmentData data = (TunnelWallBlockEntity.RenderAttachmentData) ra;
            TunnelType type = data.getType();
            if (type == null && world.getBlockState(pos).getBlock() instanceof TunnelWallBlock tunnelBlock) {
                type = tunnelBlock.getTunnel().getType();
            }
            boolean isConnected = data.isConnected();

            return switch (tintIndex) {
                case 0 -> type == null ? 0xff00ff : type.getColor();
                case 1 -> isConnected ? 0xaaaabb : 0x222233;
                default -> 0xff00ff;
            };
        }, BLOCK_WALL_TUNNEL);

        // Tint Tunnel items
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            NbtCompound stackNbt = stack.getNbt();
            if (stackNbt == null) return 0xff00ff;
            NbtElement typeNbt = stackNbt.get("type");
            if (typeNbt == null) return 0xff00ff;
            TunnelType type = TunnelType.byName(typeNbt.asString());
            if (type == null) return 0xff00ff;

            return switch (tintIndex) {
                case 0 -> type.getColor();
                default -> 0xff00ff;
            };
        }, ITEM_TUNNEL);
    }
}
