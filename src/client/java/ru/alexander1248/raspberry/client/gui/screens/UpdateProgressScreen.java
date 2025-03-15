package ru.alexander1248.raspberry.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.Text;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class UpdateProgressScreen extends Screen implements ProgressListener {
    @Nullable
    private Text title;
    @Nullable
    private Text task;
    private int progress;
    private boolean done;

    public UpdateProgressScreen() {
        super(NarratorManager.EMPTY);
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    protected boolean hasUsageText() {
        return false;
    }

    public void setTitle(Text title) {
        this.setTitleAndTask(title);
    }

    public void setTitleAndTask(Text title) {
        this.title = title;
        this.setTask(Text.translatable("menu.working"));
    }

    public void setTask(Text task) {
        this.task = task;
        this.progressStagePercentage(0);
    }

    public void progressStagePercentage(int percentage) {
        this.progress = percentage;
    }

    public void setDone() {
        this.done = true;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.done) {
            super.render(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.translatable("gui.done"),
                    this.width / 2,
                    90,
                    16777215
            );

            return;
        }

        super.render(context, mouseX, mouseY, delta);
        if (title != null)
            context.drawCenteredTextWithShadow(textRenderer, title, width >> 1, 70, 16777215);

        if (task != null)
            context.drawCenteredTextWithShadow(textRenderer, Text.empty().append(task), width >> 1, 90, 16777215);

        context.drawCenteredTextWithShadow(textRenderer, Text.empty().append(progress + " %"), width >> 1, 110, 16777215);
        renderProgressBar(context, width >> 1, 130, width >> 1, 10, 1);
    }
    private void renderProgressBar(DrawContext context, int x, int y, int width, int height, float opacity) {
        int i = MathHelper.ceil((float)(width - 2) * this.progress / 100);
        int j = Math.round(opacity * 255.0F);
        int k = ColorHelper.getArgb(j, 255, 255, 255);
        int halfX = width >>> 1;
        int minX = x - halfX;
        int maxX = x + halfX;
        int minY = y;
        int maxY = y + height;
        context.fill(minX + 2, minY + 2, minX + i, maxY - 2, k);
        context.fill(minX + 1, minY, maxX - 1, minY + 1, k);
        context.fill(minX + 1, maxY, maxX - 1, maxY - 1, k);
        context.fill(minX, minY, minX + 1, maxY, k);
        context.fill(maxX, minY, maxX - 1, maxY, k);
    }
}

