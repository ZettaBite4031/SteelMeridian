package dev.zettatech.steelmeridian.content.machine;

import dev.zettatech.steelmeridian.SteelMeridian;
import dev.zettatech.steelmeridian.content.ModContent;
import dev.zettatech.steelmeridian.simulation.machine.MachineState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class PrototypeMachineBlockEntity extends BlockEntity {
    private final MachineState machineState;

    public PrototypeMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.PROTOTYPE_MACHINE_BLOCK_ENTITY.get(), pos, state);

        machineState = new MachineState(SteelMeridian.simulationScheduler());
    }

    public MachineState machineState() {
        return machineState;
    }
}
