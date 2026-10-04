package dev.zettatech.steelmeridian.simulation.machine;

import dev.zettatech.steelmeridian.simulation.recipe.ProcessRecipe;
import dev.zettatech.steelmeridian.simulation.scheduler.SimulationScheduler;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class MachineState {
    public static final int DEFAULT_INVENTORY_CAPACITY = 64;

    private final SimulationScheduler scheduler;
    private final MachineInventory input;
    private final MachineInventory output;

    private MachineStatus status = MachineStatus.IDLE;
    private ProcessRecipe activeRecipe;
    private long completedProcesses;

    public MachineState(SimulationScheduler scheduler) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");

        input = new MachineInventory(DEFAULT_INVENTORY_CAPACITY);
        output = new MachineInventory(DEFAULT_INVENTORY_CAPACITY, this::onOutputChanged);
    }

    public boolean start(ProcessRecipe recipe) {
        Objects.requireNonNull(recipe, "recipe");

        if (status != MachineStatus.IDLE) {
            return false;
        }

        if (!hasInputs(recipe)) {
            return false;
        }

        consumeInput(recipe);

        activeRecipe = recipe;
        status = MachineStatus.RUNNING;

        scheduler.schedule(recipe.durationTicks(), this::completeProcess);

        return true;
    }

    private void completeProcess() {
        if (status != MachineStatus.RUNNING) {
            return;
        }

        if (!canStoreOutputs(activeRecipe)) {
            status = MachineStatus.OUTPUT_BLOCKED;
            return;
        }

        produceOutputs(activeRecipe);

        completedProcesses++;
        activeRecipe = null;
        status = MachineStatus.IDLE;
    }

    private void onOutputChanged() {
        if (status != MachineStatus.OUTPUT_BLOCKED) {
            return;
        }

        if (!canStoreOutputs(activeRecipe)) {
            return;
        }

        produceOutputs(activeRecipe);

        completedProcesses++;
        activeRecipe = null;
        status = MachineStatus.IDLE;
    }

    private boolean hasInputs(ProcessRecipe recipe) {
        for (var entry : amountsByItem(recipe.inputs()).entrySet()) {
            if (!input.contains(entry.getKey(), entry.getValue())) {
                return false;
            }
        }

        return true;
    }

    private void consumeInput(ProcessRecipe recipe) {
        for (var entry : amountsByItem(recipe.inputs()).entrySet()) {
            input.remove(entry.getKey(), entry.getValue());
        }
    }

    private boolean canStoreOutputs(ProcessRecipe recipe) {
        int requireCapacity = amountsByItem(recipe.outputs()).values().stream().mapToInt(Integer::intValue).sum();
        return output.remainingCapacity() >= requireCapacity;
    }

    private void produceOutputs(ProcessRecipe recipe) {
        for (var entry : amountsByItem(recipe.outputs()).entrySet()) {
            output.insert(entry.getKey(), entry.getValue());
        }
    }

    private static Map<Identifier, Integer> amountsByItem(Iterable<ProcessRecipe.ItemAmount> amounts) {
        Map<Identifier, Integer> totals = new HashMap<>();
        for (ProcessRecipe.ItemAmount amount : amounts) {
            totals.merge(amount.item(), amount.count(), Integer::sum);
        }
        return totals;
    }

    public MachineInventory inventory(MachineEndpoint endpoint) {
        return switch (endpoint) {
            case INPUT -> input;
            case OUTPUT -> output;
        };
    }

    public MachineStatus status() {
        return status;
    }

    public ProcessRecipe activeRecipe() {
        return activeRecipe;
    }

    public long completedProcesses() {
        return completedProcesses;
    }

    public boolean isRunning() {
        return status == MachineStatus.RUNNING;
    }
}
