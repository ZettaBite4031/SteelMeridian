package dev.zettatech.steelmeridian;

import com.mojang.logging.LogUtils;
import dev.zettatech.steelmeridian.simulation.scheduler.SimulationScheduler;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

@Mod(SteelMeridian.MOD_ID)
public final class SteelMeridian {
    public static final String MOD_ID = "steelmeridian";

    private static final Logger LOGGER = LogUtils.getLogger();

    private final SimulationScheduler simulationScheduler = new SimulationScheduler();

    public SteelMeridian() {
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onServerStopped);

        LOGGER.info("Steel Meridian initialized.");
    }

    private void onServerTick(ServerTickEvent.Post event) {
        simulationScheduler.tick();
    }

    private void onServerStopped(ServerStoppedEvent event) {
        simulationScheduler.clear();
    }
}
