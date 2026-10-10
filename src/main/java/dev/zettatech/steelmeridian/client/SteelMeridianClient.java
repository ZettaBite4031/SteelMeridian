package dev.zettatech.steelmeridian.client;

import dev.zettatech.steelmeridian.SteelMeridian;
import dev.zettatech.steelmeridian.client.gui.PrototypeMachineScreen;
import dev.zettatech.steelmeridian.content.ModContent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid=SteelMeridian.MOD_ID, value=Dist.CLIENT)
public final class SteelMeridianClient {
    private SteelMeridianClient() {}

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModContent.PROTOTYPE_MACHINE_MENU.get(), PrototypeMachineScreen::new);
    }
}
