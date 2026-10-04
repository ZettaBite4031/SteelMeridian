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
    void machineCannotStartWithoutInputs() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        assertFalse(machine.start(testRecipe(3)));
        assertEquals(MachineStatus.IDLE, machine.status());
        assertNull(machine.activeRecipe());
    }

    @Test
    void machineStartsRecipeWithRequiredInputs() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);
        ProcessRecipe recipe = testRecipe(3);

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);

        assertTrue(machine.start(recipe));
        assertEquals(MachineStatus.RUNNING, machine.status());
        assertSame(recipe, machine.activeRecipe());
        assertEquals(0, machine.completedProcesses());
    }

    @Test
    void startingRecipeConsumesInputs() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);

        assertEquals(1, machine.inventory(MachineEndpoint.INPUT).count(rawIron()));

        assertTrue(machine.start(testRecipe(3)));

        assertEquals(0, machine.inventory(MachineEndpoint.INPUT).count(rawIron()));
    }

    @Test
    void machineDoesNotCompleteEarly() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);
        machine.start(testRecipe(3));

        scheduler.tick();
        scheduler.tick();

        assertTrue(machine.isRunning());
        assertEquals(MachineStatus.RUNNING, machine.status());
        assertEquals(0, machine.inventory(MachineEndpoint.OUTPUT).count(ironIngot()));
        assertEquals(0, machine.completedProcesses());
    }

    @Test
    void machineCompletesAtCorrectTick() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);
        machine.start(testRecipe(3));

        scheduler.tick();
        scheduler.tick();
        scheduler.tick();

        assertEquals(MachineStatus.IDLE, machine.status());
        assertNull(machine.activeRecipe());
        assertEquals(1, machine.inventory(MachineEndpoint.OUTPUT).count(ironIngot()));
        assertEquals(1, machine.completedProcesses());
    }

    @Test
    void completedProcessOnlyCountsOnce() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);
        machine.start(testRecipe(1));

        scheduler.tick();
        scheduler.tick();
        scheduler.tick();

        assertEquals(1, machine.completedProcesses());
        assertEquals(1, machine.inventory(MachineEndpoint.OUTPUT).count(ironIngot()));
    }

    @Test
    void machineCannotStartAnotherRecipeWhileRunning() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        ProcessRecipe first = testRecipe(3);
        ProcessRecipe second = testRecipe(1);

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 2);

        assertTrue(machine.start(first));
        assertFalse(machine.start(second));

        assertSame(first, machine.activeRecipe());
        assertEquals(MachineStatus.RUNNING, machine.status());

        // Only the first recipe consumed input.
        assertEquals(1, machine.inventory(MachineEndpoint.INPUT).count(rawIron()));
    }

    @Test
    void outputDoesNotAppearBeforeCompletion() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);
        machine.start(testRecipe(2));

        scheduler.tick();

        assertEquals(0, machine.inventory(MachineEndpoint.OUTPUT).count(ironIngot()));

        scheduler.tick();

        assertEquals(1, machine.inventory(MachineEndpoint.OUTPUT).count(ironIngot()));
    }

    @Test
    void machineWaitsWhenOutputIsBlocked() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);
        machine.inventory(MachineEndpoint.OUTPUT).insert(cobblestone(), 64);

        assertTrue(machine.start(testRecipe(1)));

        scheduler.tick();

        assertEquals(MachineStatus.OUTPUT_BLOCKED, machine.status());
        assertEquals(testRecipeId(), machine.activeRecipe().id());
        assertEquals(0, machine.inventory(MachineEndpoint.OUTPUT).count(ironIngot()));
        assertEquals(0, machine.completedProcesses());
    }

    @Test
    void blockedMachineCompletesWhenOutputSpaceBecomesAvailable() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);
        machine.inventory(MachineEndpoint.OUTPUT).insert(cobblestone(), 64);

        assertTrue(machine.start(testRecipe(1)));

        scheduler.tick();

        assertEquals(MachineStatus.OUTPUT_BLOCKED, machine.status());

        machine.inventory(MachineEndpoint.OUTPUT).remove(cobblestone(), 1);

        assertEquals(MachineStatus.IDLE, machine.status());
        assertNull(machine.activeRecipe());
        assertEquals(1, machine.inventory(MachineEndpoint.OUTPUT).count(ironIngot()));
        assertEquals(1, machine.completedProcesses());
    }

    @Test
    void machineCannotStartWhileOutputBlocked() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 2);
        machine.inventory(MachineEndpoint.OUTPUT).insert(cobblestone(), 64);

        machine.start(testRecipe(1));
        scheduler.tick();

        assertEquals(MachineStatus.OUTPUT_BLOCKED, machine.status());
        assertFalse(machine.start(testRecipe(1)));
    }

    @Test
    void machineCanRunAnotherRecipeAfterCompletion() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 2);

        assertTrue(machine.start(testRecipe(1)));
        scheduler.tick();

        assertEquals(MachineStatus.IDLE, machine.status());
        assertEquals(1, machine.completedProcesses());

        assertTrue(machine.start(testRecipe(1)));
        scheduler.tick();

        assertEquals(MachineStatus.IDLE, machine.status());
        assertEquals(2, machine.completedProcesses());
        assertEquals(2, machine.inventory(MachineEndpoint.OUTPUT).count(ironIngot()));
    }

    private static ProcessRecipe testRecipe(long durationTicks) {
        return new ProcessRecipe(
            testRecipeId(),
            ProcessCategory.SMELTING,
            durationTicks,
            List.of(new ProcessRecipe.ItemAmount(rawIron(), 1)),
            List.of(new ProcessRecipe.ItemAmount(ironIngot(), 1))
        );
    }

    private static Identifier testRecipeId() {
        return Identifier.fromNamespaceAndPath(
            "steelmeridian",
            "test_recipe"
        );
    }

    private static Identifier rawIron() {
        return Identifier.fromNamespaceAndPath(
            "minecraft",
            "raw_iron"
        );
    }

    private static Identifier ironIngot() {
        return Identifier.fromNamespaceAndPath(
            "minecraft",
            "iron_ingot"
        );
    }

    private static Identifier cobblestone() {
        return Identifier.fromNamespaceAndPath(
            "minecraft",
            "cobblestone"
        );
    }
}
