package dev.zettatech.steelmeridian;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(SteelMeridian.MOD_ID)
public final class SteelMeridian {
    public static final String MOD_ID = "steelmeridian";

    private static final Logger LOGGER = LogUtils.getLogger();

    public SteelMeridian() {
        LOGGER.info("Hello from Steel Meridian!");
    }
}
