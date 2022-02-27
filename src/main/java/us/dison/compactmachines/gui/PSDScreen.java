package us.dison.compactmachines.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import us.dison.compactmachines.CompactMachines;

import java.util.List;

@Environment(EnvType.CLIENT)
public class PSDScreen extends Screen {

    public static final Identifier TEXTURE = new Identifier(CompactMachines.MODID, "textures/gui/psd_screen.png");

    public PSDScreen(Text title) {
        super(title);
    }


    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int j = (this.width - 256) / 2;
        int k = (this.height - 256) / 2 + 32;
        this.drawTexture(matrices, j, k, 0, 0, 256, 256);

        textRenderer.drawWithShadow(matrices,
                new TranslatableText(CompactMachines.MODID + ".psd.pages.machines.title"),
                j+17, k+16, Formatting.GOLD.getColorValue()
        );

//        List<OrderedText> textList = this.textRenderer.wrapLines(new TranslatableText(CompactMachines.MODID + ".psd.pages.machines"), 200);
//        for (int i = 0; i < textList.size(); i++) {
//            String text = textList.get(i);
//            textRenderer.drawWithShadow(matrices,
//                    text,
//                    i+17, j+16+8 + 8*i, Formatting.WHITE.getColorValue()
//            );
//        }
        MultilineText text = MultilineText.create(this.textRenderer, new TranslatableText(CompactMachines.MODID + ".psd.pages.machines"), 222);
        text.drawWithShadow(matrices, j+17, k+16+15, 10, Formatting.WHITE.getColorValue());

        super.render(matrices, mouseX, mouseY, delta);
    }

}
