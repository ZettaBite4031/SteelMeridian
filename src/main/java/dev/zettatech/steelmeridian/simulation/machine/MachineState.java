package dev.zettatech.steelmeridian.simulation.machine;

import dev.zettatech.steelmeridian.simulation.recipe.ProcessRecipe;
import dev.zettatech.steelmeridian.simulation.scheduler.SimulationScheduler;

import java.util.Objects;

public final class MachineState {
    private final SimulationScheduler scheduler;

    private MachineStatus status = MachineStatus.IDLE;
    private ProcessRecipe activeRecipe;
    private long completedProcesses;

    public MachineState(SimulationScheduler scheduler) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    }

    public boolean start(ProcessRecipe recipe) {
        Objects.requireNonNull(recipe, "recipe");

        if (status == MachineStatus.RUNNING) {
            return false;
        }

        activeRecipe = recipe;
        status = MachineStatus.RUNNING;

        scheduler.schedule(recipe.durationTicks(), this::completeProcess);

        return true;
    }

    private void completeProcess() {
        if (status != MachineStatus.RUNNING) {
            return;
        }

        completedProcesses++;
        activeRecipe = null;
        status = MachineStatus.IDLE;
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
