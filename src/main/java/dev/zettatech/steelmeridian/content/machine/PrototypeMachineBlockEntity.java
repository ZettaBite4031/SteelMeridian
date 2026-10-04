package dev.zettatech.steelmeridian.content.machine;

import dev.zettatech.steelmeridian.SteelMeridian;
import dev.zettatech.steelmeridian.compat.item.MachineItemHandler;
import dev.zettatech.steelmeridian.content.ModContent;
import dev.zettatech.steelmeridian.simulation.machine.MachineEndpoint;
import dev.zettatech.steelmeridian.simulation.machine.MachineState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class PrototypeMachineBlockEntity extends BlockEntity {
    private final MachineState machineState;

    private final MachineItemHandler inputHandler;
    private final MachineItemHandler outputHandler;

    public PrototypeMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.PROTOTYPE_MACHINE_BLOCK_ENTITY.get(), pos, state);

        machineState = new MachineState(SteelMeridian.simulationScheduler());

        inputHandler = new MachineItemHandler(machineState, MachineEndpoint.INPUT);
        outputHandler = new MachineItemHandler(machineState, MachineEndpoint.OUTPUT);
    }

    public MachineState machineState() {
        return machineState;
    }

    public MachineItemHandler outputHandler() {
        return outputHandler;
    }

    public MachineItemHandler inputHandler() {
        return inputHandler;
    }
}
