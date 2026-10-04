package dev.zettatech.steelmeridian;

import com.mojang.logging.LogUtils;
import dev.zettatech.steelmeridian.content.ModContent;
import dev.zettatech.steelmeridian.simulation.scheduler.SimulationScheduler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

@Mod(SteelMeridian.MOD_ID)
public final class SteelMeridian {
    public static final String MOD_ID = "steelmeridian";

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final SimulationScheduler SIMULATION_SCHEDULER = new SimulationScheduler();

    public SteelMeridian(IEventBus modBus) {
        modBus.addListener(this::registerCapabilities);
        ModContent.register(modBus);

        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onServerStopped);

        LOGGER.info("Steel Meridian initialized.");
    }

    private void onServerTick(ServerTickEvent.Post event) {
        SIMULATION_SCHEDULER.tick();
    }

    private void onServerStopped(ServerStoppedEvent event) {
        SIMULATION_SCHEDULER.clear();
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.Item.BLOCK,
            ModContent.PROTOTYPE_MACHINE_BLOCK_ENTITY.get(),
            (blockEntity, side) -> {
                if (side == null) {
                    return blockEntity.inputHandler();
                }
                return side.getAxis().isHorizontal() ? blockEntity.inputHandler() : blockEntity.outputHandler();
            }
        );
    }

    public static SimulationScheduler simulationScheduler() {
        return SIMULATION_SCHEDULER;
    }

}
