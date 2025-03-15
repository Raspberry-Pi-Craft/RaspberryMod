package ru.alexander1248.raspberry.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.ProgressListener;
import ru.alexander1248.raspberry.client.gui.screens.UpdateProgressScreen;
import ru.alexander1248.raspberry.loader.PackIndexUpdater;
import ru.alexander1248.raspberry.loggers.AbstractMessenger;
import ru.alexander1248.raspberry.loggers.LoggerMessenger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static ru.alexander1248.raspberry.Raspberry.LOGGER;

public class RaspberryClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register(RaspberryClient::afterScreenInit);
    }
    public static void afterScreenInit(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
        if (screen instanceof TitleScreen) {
            AbstractMessenger messenger =  new LoggerMessenger(LOGGER);
            try {
                PackIndexUpdater.checkFiles(messenger);
            } catch (IOException e) {
                messenger.error("File IO error!", e);
            }
            afterTitleScreenInit(screen);
        }
    }
    private static void afterTitleScreenInit(Screen screen) {
        final List<ClickableWidget> buttons = Screens.getButtons(screen);
        final int spacing = 24;
        for (int i = 0; i < buttons.size(); i++) {
            ClickableWidget widget = buttons.get(i);
            if (widget instanceof ButtonWidget button) {
                if (button.visible)
                    widget.setY(widget.getY() - spacing / 2);

                if (buttonHasText(button, "menu.multiplayer") && PackIndexUpdater.isNeedUpdate()) {
                    buttons.set(i, ButtonWidget.builder(
                            Text.translatable("raspberry.update"),
                            (btn) -> {
                                UpdateProgressScreen progressScreen = new UpdateProgressScreen();
                                MinecraftClient.getInstance().setScreenAndRender(progressScreen);
                                new Thread(() -> update(progressScreen)).start();
                            }
                    ).dimensions(
                            button.getX(),
                            button.getY(),
                            button.getWidth(),
                            button.getHeight()
                    ).build());
                }
            }
        }
    }
    private static void update(ProgressListener listener) {
        AbstractMessenger messenger =  new LoggerMessenger(LOGGER);
        try {
            PackIndexUpdater.tryUpdateFiles(messenger, listener);
        } catch (IOException e) {
            messenger.error("File IO error!", e);
        } catch (InterruptedException e) {
            messenger.error("Interrupted!", e);
        }
    }

    public static boolean buttonHasText(Widget widget, String... translationKeys) {
        if (widget instanceof ButtonWidget button) {
            Text text = button.getMessage();
            TextContent textContent = text.getContent();

            return textContent instanceof TranslatableTextContent && Arrays.stream(translationKeys)
                    .anyMatch(s -> ((TranslatableTextContent) textContent).getKey().equals(s));
        }
        return false;
    }
}
