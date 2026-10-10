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
    private final Runnable onChanged;

    private MachineStatus status = MachineStatus.IDLE;

    private ProcessRecipe selectedRecipe;
    private ProcessRecipe activeRecipe;

    private long completedProcesses;
    private long completionTick = -1;

    public MachineState(SimulationScheduler scheduler) {
        this(scheduler, () -> {});
    }

    public MachineState(SimulationScheduler scheduler, Runnable onChanged) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.onChanged = Objects.requireNonNull(onChanged, "onChanged");

        input = new MachineInventory(
            DEFAULT_INVENTORY_CAPACITY,
            this::onInputChanged
        );

        output = new MachineInventory(
            DEFAULT_INVENTORY_CAPACITY,
            this::onOutputChanged
        );
    }

    private void stateChanged() {
        onChanged.run();
    }

    public boolean selectRecipe(ProcessRecipe recipe) {
        Objects.requireNonNull(recipe, "recipe");

        if (status != MachineStatus.IDLE) {
            return false;
        }

        selectedRecipe = recipe;
        stateChanged();

        tryStartSelectedRecipe();

        return true;
    }

    public boolean clearRecipe() {
        if (status != MachineStatus.IDLE) {
            return false;
        }

        selectedRecipe = null;
        stateChanged();

        return true;
    }

    public boolean canRunSelectedRecipe() {
        return selectedRecipe != null
            && status == MachineStatus.IDLE
            && hasInputs(selectedRecipe);
    }

    private void tryStartSelectedRecipe() {
        if (!canRunSelectedRecipe()) {
            return;
        }

        startSelectedRecipe();
    }

    private void startSelectedRecipe() {
        activeRecipe = selectedRecipe;
        status = MachineStatus.RUNNING;

        /*
         * Status becomes RUNNING before removing items so the inventory
         * callback cannot accidentally start another process.
         */
        consumeInput(activeRecipe);

        completionTick = scheduler.currentTick() + activeRecipe.durationTicks();

        scheduler.schedule(
            activeRecipe.durationTicks(),
            this::completeProcess
        );

        stateChanged();
    }

    private void completeProcess() {
        if (status != MachineStatus.RUNNING || activeRecipe == null) {
            return;
        }

        if (!canStoreOutputs(activeRecipe)) {
            status = MachineStatus.OUTPUT_BLOCKED;
            completionTick = -1;
            stateChanged();
            return;
        }

        finishProcess();
    }

    private void finishProcess() {
        /*
         * Make sure output callbacks cannot recursively treat this as a
         * blocked process while outputs are being inserted.
         */
        status = MachineStatus.RUNNING;

        produceOutputs(activeRecipe);

        completedProcesses++;
        activeRecipe = null;
        completionTick = -1;
        status = MachineStatus.IDLE;
        stateChanged();

        tryStartSelectedRecipe();
    }

    private void onInputChanged() {
        tryStartSelectedRecipe();
    }

    private void onOutputChanged() {
        if (status != MachineStatus.OUTPUT_BLOCKED
            || activeRecipe == null) {
            return;
        }

        if (canStoreOutputs(activeRecipe)) {
            finishProcess();
        }
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
        int requiredCapacity = amountsByItem(recipe.outputs())
            .values()
            .stream()
            .mapToInt(Integer::intValue)
            .sum();

        return output.remainingCapacity() >= requiredCapacity;
    }

    private void produceOutputs(ProcessRecipe recipe) {
        for (var entry : amountsByItem(recipe.outputs()).entrySet()) {
            output.insert(entry.getKey(), entry.getValue());
        }
    }

    private static Map<Identifier, Integer> amountsByItem(
        Iterable<ProcessRecipe.ItemAmount> amounts
    ) {
        Map<Identifier, Integer> totals = new HashMap<>();

        for (ProcessRecipe.ItemAmount amount : amounts) {
            totals.merge(
                amount.item(),
                amount.count(),
                Integer::sum
            );
        }

        return totals;
    }

    public MachineInventory inventory(MachineEndpoint endpoint) {
        return switch (endpoint) {
            case INPUT -> input;
            case OUTPUT -> output;
        };
    }

    public ProcessRecipe selectedRecipe() {
        return selectedRecipe;
    }

    public ProcessRecipe activeRecipe() {
        return activeRecipe;
    }

    public MachineStatus status() {
        return status;
    }

    public long completedProcesses() {
        return completedProcesses;
    }

    public long remainingProcessTicks() {
        if (status != MachineStatus.RUNNING || completionTick < 0) {
            return 0;
        }

        return Math.max(0, completionTick - scheduler.currentTick());
    }

    public boolean isRunning() {
        return status == MachineStatus.RUNNING;
    }

    public void restore(ProcessRecipe selectedRecipe, ProcessRecipe activeRecipe, MachineStatus status, long completedProcesses, long remainingTicks, Map<Identifier, Integer> inputItems, Map<Identifier, Integer> outputItems) {
        this.selectedRecipe = selectedRecipe;
        this.activeRecipe = activeRecipe;
        this.status = status;
        this.completedProcesses = completedProcesses;

        input.restore(inputItems, false);
        output.restore(outputItems, false);

        if (status == MachineStatus.RUNNING && activeRecipe != null) {
            long delay = Math.max(1, remainingTicks);
            completionTick = scheduler.currentTick() + delay;
            scheduler.schedule(delay, this::completeProcess);
        } else {
            completionTick = -1;
        }
    }
}
