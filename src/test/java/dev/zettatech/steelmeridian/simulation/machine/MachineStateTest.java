package dev.zettatech.steelmeridian.simulation.machine;

import dev.zettatech.steelmeridian.simulation.recipe.ProcessCategory;
import dev.zettatech.steelmeridian.simulation.recipe.ProcessRecipe;
import dev.zettatech.steelmeridian.simulation.scheduler.SimulationScheduler;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MachineStateTest {
    @Test
    void machineStartsRecipe() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);
        ProcessRecipe recipe = testRecipe(3);

        assertTrue(machine.start(recipe));

        assertEquals(MachineStatus.RUNNING, machine.status());
        assertSame(recipe, machine.activeRecipe());
        assertEquals(0, machine.completedProcesses());
    }

    @Test
    void machineDoesNotCompleteEarly() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.start(testRecipe(3));

        scheduler.tick();
        scheduler.tick();

        assertTrue(machine.isRunning());
        assertEquals(0, machine.completedProcesses());
    }

    @Test
    void machineCompletesAtCorrectTick() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.start(testRecipe(3));

        scheduler.tick();
        scheduler.tick();
        scheduler.tick();

        assertEquals(MachineStatus.IDLE, machine.status());
        assertNull(machine.activeRecipe());
        assertEquals(1, machine.completedProcesses());
    }

    @Test
    void machineCannotStartAnotherRecipeWhileRunning() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        ProcessRecipe first = testRecipe(3);
        ProcessRecipe second = testRecipe(1);

        assertTrue(machine.start(first));
        assertFalse(machine.start(second));

        assertSame(first, machine.activeRecipe());
    }

    @Test
    void completedProcessOnlyCountsOnce() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.start(testRecipe(1));

        scheduler.tick();
        scheduler.tick();
        scheduler.tick();

        assertEquals(1, machine.completedProcesses());
    }

    private static ProcessRecipe testRecipe(long durationTicks) {
        return new ProcessRecipe(
            Identifier.fromNamespaceAndPath("steelmeridian", "test_recipe"),
            ProcessCategory.SMELTING,
            durationTicks,
            List.of(item("minecraft", "raw_iron", 1)),
            List.of(item("minecraft", "iron_ingot", 1))
        );
    }

    private static ProcessRecipe.ItemAmount item(
        String namespace,
        String path,
        int count
    ) {
        return new ProcessRecipe.ItemAmount(
            Identifier.fromNamespaceAndPath(namespace, path),
            count
        );
    }
}
