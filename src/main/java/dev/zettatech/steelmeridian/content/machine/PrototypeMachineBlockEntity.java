package dev.zettatech.steelmeridian.content.machine;

import dev.zettatech.steelmeridian.SteelMeridian;
import dev.zettatech.steelmeridian.compat.item.MachineItemHandler;
import dev.zettatech.steelmeridian.content.ModContent;
import dev.zettatech.steelmeridian.content.PrototypeRecipes;
import dev.zettatech.steelmeridian.simulation.machine.MachineEndpoint;
import dev.zettatech.steelmeridian.simulation.machine.MachineState;
import dev.zettatech.steelmeridian.simulation.machine.MachineStatus;
import dev.zettatech.steelmeridian.simulation.recipe.ProcessRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashMap;
import java.util.Map;

public final class PrototypeMachineBlockEntity extends BlockEntity {
    private final MachineState machineState;

    private final MachineItemHandler inputHandler;
    private final MachineItemHandler outputHandler;

    public PrototypeMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.PROTOTYPE_MACHINE_BLOCK_ENTITY.get(), pos, state);

        machineState = new MachineState(SteelMeridian.simulationScheduler(), this::setChanged);

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

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        MachineState machine = machineState;
        if (machine.selectedRecipe() != null) {
            output.putString("selected_recipe", machine.selectedRecipe().id().toString());
        }

        if (machine.activeRecipe() != null) {
            output.putString("active_recipe", machine.activeRecipe().id().toString());
        }

        output.putString("status", machine.status().name());
        output.putLong("completed_processes", machine.completedProcesses());
        output.putLong("remaining_ticks", machine.remainingProcessTicks());

        writeInventory(output, "input", machine.inventory(MachineEndpoint.INPUT).snapshot());
        writeInventory(output, "output", machine.inventory(MachineEndpoint.OUTPUT).snapshot());
    }

    private static void writeInventory(ValueOutput output, String key, Map<Identifier, Integer> items) {
        ValueOutput.ValueOutputList list = output.childrenList(key);
        for (var entry : items.entrySet()) {
            ValueOutput child = list.addChild();
            child.putString("item", entry.getKey().toString());
            child.putInt("count", entry.getValue());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        ProcessRecipe selectedRecipe = readRecipe(input, "selected_recipe");
        ProcessRecipe activeRecipe = readRecipe(input, "active_recipe");

        MachineStatus status;
        try {
            status = MachineStatus.valueOf(input.getStringOr("status", MachineStatus.IDLE.name()));
        } catch (IllegalArgumentException ignored) {
            status = MachineStatus.IDLE;
        }

        long completedProcesses = input.getLongOr("completed_processes", 0);
        long remainingTicks = input.getLongOr("remaining_ticks", 0);

        Map<Identifier, Integer> inputItems = readInventory(input, "input");
        Map<Identifier, Integer> outputItems = readInventory(input, "output");

        machineState.restore(selectedRecipe, activeRecipe, status, completedProcesses, remainingTicks, inputItems, outputItems);
    }

    private static ProcessRecipe readRecipe(ValueInput input, String key) {
        String value = input.getStringOr(key, "");
        if (value.isEmpty()) {
            return null;
        }

        Identifier id;
        try {
            id = Identifier.parse(value);
        } catch (Exception ignored) {
            return null;
        }

        return PrototypeRecipes.REGISTRY.get(id).orElse(null);
    }

    private static Map<Identifier, Integer> readInventory(ValueInput input, String key) {
        Map<Identifier, Integer> items = new HashMap<>();
        for (ValueInput child : input.childrenListOrEmpty(key)) {
            String itemString = child.getStringOr("item", "");
            int count = child.getIntOr("count", 0);
            if (itemString.isEmpty() || count <= 0) {
                continue;
            }

            Identifier item;
            try {
                item = Identifier.parse(itemString);
            } catch (Exception ignored) {
                continue;
            }

            items.merge(item, count, Integer::sum);
        }

        return items;
    }
}
