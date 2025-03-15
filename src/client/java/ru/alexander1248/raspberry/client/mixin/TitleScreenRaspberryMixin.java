package ru.alexander1248.raspberry.client.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TitleScreen.class)
public abstract class TitleScreenRaspberryMixin extends Screen {

    protected TitleScreenRaspberryMixin(Text title) {
        super(title);
    }

}
