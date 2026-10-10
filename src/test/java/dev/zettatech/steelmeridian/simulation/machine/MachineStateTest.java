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
    void selectedRecipeWaitsForInputs() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);
        ProcessRecipe recipe = testRecipe(3);

        assertTrue(machine.selectRecipe(recipe));

        assertEquals(MachineStatus.IDLE, machine.status());
        assertSame(recipe, machine.selectedRecipe());
        assertNull(machine.activeRecipe());
        assertEquals(0, machine.completedProcesses());
    }

    @Test
    void machineStartsAutomaticallyWhenInputArrives() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);
        ProcessRecipe recipe = testRecipe(3);

        machine.selectRecipe(recipe);

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);

        assertEquals(MachineStatus.RUNNING, machine.status());
        assertSame(recipe, machine.selectedRecipe());
        assertSame(recipe, machine.activeRecipe());
    }

    @Test
    void startingRecipeConsumesInputs() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.selectRecipe(testRecipe(3));

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);

        assertEquals(0, machine.inventory(MachineEndpoint.INPUT).count(rawIron()));
    }

    @Test
    void machineDoesNotCompleteEarly() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.selectRecipe(testRecipe(3));

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);

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

        machine.selectRecipe(testRecipe(3));

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);

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

        machine.selectRecipe(testRecipe(1));

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);

        scheduler.tick();
        scheduler.tick();
        scheduler.tick();

        assertEquals(1, machine.completedProcesses());
        assertEquals(1, machine.inventory(MachineEndpoint.OUTPUT).count(ironIngot()));
    }

    @Test
    void recipeCannotBeChangedWhileRunning() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        ProcessRecipe first = testRecipe(3);
        ProcessRecipe second = secondRecipe(1);

        machine.selectRecipe(first);

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);

        assertEquals(MachineStatus.RUNNING, machine.status());

        assertFalse(machine.selectRecipe(second));

        assertSame(first, machine.selectedRecipe());
        assertSame(first, machine.activeRecipe());
    }

    @Test
    void selectedRecipeCanBeChangedWhileIdle() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        ProcessRecipe first = testRecipe(3);
        ProcessRecipe second = secondRecipe(1);

        assertTrue(machine.selectRecipe(first));
        assertTrue(machine.selectRecipe(second));

        assertSame(second, machine.selectedRecipe());
        assertNull(machine.activeRecipe());
    }

    @Test
    void selectedRecipeCanBeClearedWhileIdle() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.selectRecipe(testRecipe(3));

        assertTrue(machine.clearRecipe());

        assertNull(machine.selectedRecipe());
        assertEquals(MachineStatus.IDLE, machine.status());
    }

    @Test
    void selectedRecipeCannotBeClearedWhileRunning() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);
        ProcessRecipe recipe = testRecipe(3);

        machine.selectRecipe(recipe);

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);

        assertFalse(machine.clearRecipe());
        assertSame(recipe, machine.selectedRecipe());
    }

    @Test
    void outputDoesNotAppearBeforeCompletion() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.selectRecipe(testRecipe(2));

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);

        scheduler.tick();

        assertEquals(0, machine.inventory(MachineEndpoint.OUTPUT).count(ironIngot()));

        scheduler.tick();

        assertEquals(1, machine.inventory(MachineEndpoint.OUTPUT).count(ironIngot()));
    }

    @Test
    void machineWaitsWhenOutputIsBlocked() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);
        ProcessRecipe recipe = testRecipe(1);

        machine.inventory(MachineEndpoint.OUTPUT).insert(cobblestone(), 64);

        machine.selectRecipe(recipe);

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);

        scheduler.tick();

        assertEquals(MachineStatus.OUTPUT_BLOCKED, machine.status());
        assertSame(recipe, machine.activeRecipe());

        assertEquals(0, machine.inventory(MachineEndpoint.OUTPUT).count(ironIngot()));
        assertEquals(0, machine.completedProcesses());
    }

    @Test
    void blockedMachineCompletesWhenOutputSpaceBecomesAvailable() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.inventory(MachineEndpoint.OUTPUT).insert(cobblestone(), 64);

        machine.selectRecipe(testRecipe(1));

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);

        scheduler.tick();

        assertEquals(MachineStatus.OUTPUT_BLOCKED, machine.status());

        machine.inventory(MachineEndpoint.OUTPUT).remove(cobblestone(), 1);

        assertEquals(MachineStatus.IDLE, machine.status());
        assertNull(machine.activeRecipe());

        assertEquals(1, machine.inventory(MachineEndpoint.OUTPUT).count(ironIngot()));
        assertEquals(1, machine.completedProcesses());
    }

    @Test
    void recipeCannotBeChangedWhileOutputBlocked() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        ProcessRecipe first = testRecipe(1);
        ProcessRecipe second = secondRecipe(1);

        machine.inventory(MachineEndpoint.OUTPUT).insert(cobblestone(), 64);

        machine.selectRecipe(first);

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);

        scheduler.tick();

        assertEquals(MachineStatus.OUTPUT_BLOCKED, machine.status());

        assertFalse(machine.selectRecipe(second));
        assertSame(first, machine.selectedRecipe());
    }

    @Test
    void machineAutomaticallyProcessesAnotherRecipeWhenInputsRemain() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.selectRecipe(testRecipe(1));

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 2);

        assertEquals(MachineStatus.RUNNING, machine.status());

        scheduler.tick();

        assertEquals(1, machine.completedProcesses());

        /*
         * One raw iron was still waiting, so finishing the first process
         * should immediately start the second.
         */
        assertEquals(MachineStatus.RUNNING, machine.status());

        scheduler.tick();

        assertEquals(2, machine.completedProcesses());
        assertEquals(MachineStatus.IDLE, machine.status());

        assertEquals(2, machine.inventory(MachineEndpoint.OUTPUT).count(ironIngot()));
    }

    @Test
    void machineStartsAgainWhenNewInputArrivesAfterCompletion() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.selectRecipe(testRecipe(1));

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);

        scheduler.tick();

        assertEquals(MachineStatus.IDLE, machine.status());
        assertEquals(1, machine.completedProcesses());

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);

        assertEquals(MachineStatus.RUNNING, machine.status());

        scheduler.tick();

        assertEquals(2, machine.completedProcesses());
        assertEquals(MachineStatus.IDLE, machine.status());
    }

    @Test
    void remainingProcessTicksCountsDown() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.selectRecipe(testRecipe(3));

        machine.inventory(MachineEndpoint.INPUT).insert(rawIron(), 1);

        assertEquals(3, machine.remainingProcessTicks());

        scheduler.tick();

        assertEquals(2, machine.remainingProcessTicks());

        scheduler.tick();

        assertEquals(1, machine.remainingProcessTicks());

        scheduler.tick();

        assertEquals(0, machine.remainingProcessTicks());
    }

    @Test
    void restoreRestoresInventories() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        machine.restore(null, null, MachineStatus.IDLE,
            0, 0, java.util.Map.of(rawIron(), 4),
            java.util.Map.of(ironIngot(), 2) );

        assertEquals(4, machine.inventory(MachineEndpoint.INPUT).count(rawIron()));
        assertEquals(2, machine.inventory(MachineEndpoint.OUTPUT).count(ironIngot()));
    }

    @Test
    void restoreRestoresSelectedRecipe() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        ProcessRecipe recipe = testRecipe(3);

        machine.restore(recipe, null, MachineStatus.IDLE, 5, 0,
            java.util.Map.of(), java.util.Map.of());

        assertSame(recipe, machine.selectedRecipe());
        assertNull(machine.activeRecipe());
        assertEquals(MachineStatus.IDLE, machine.status());
        assertEquals(5, machine.completedProcesses());
    }

    @Test
    void restoredRunningProcessResumes() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        ProcessRecipe recipe = testRecipe(10);

        machine.restore(recipe, recipe, MachineStatus.RUNNING, 0, 3,
            java.util.Map.of(), java.util.Map.of());

        assertEquals(MachineStatus.RUNNING, machine.status());
        assertEquals(3, machine.remainingProcessTicks());

        scheduler.tick();
        scheduler.tick();

        assertEquals(MachineStatus.RUNNING, machine.status());
        assertEquals(0, machine.completedProcesses());

        scheduler.tick();

        assertEquals(MachineStatus.IDLE, machine.status());
        assertNull(machine.activeRecipe());
        assertEquals(1, machine.completedProcesses());
        assertEquals(1, machine.inventory(MachineEndpoint.OUTPUT).count(ironIngot()));
    }

    @Test
    void restoredBlockedProcessCompletesWhenSpaceBecomesAvailable() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        ProcessRecipe recipe = testRecipe(1);

        machine.restore(recipe, recipe, MachineStatus.OUTPUT_BLOCKED, 0, 0,
            java.util.Map.of(), java.util.Map.of(cobblestone(), 64));

        assertEquals(MachineStatus.OUTPUT_BLOCKED, machine.status());

        machine.inventory(MachineEndpoint.OUTPUT) .remove(cobblestone(), 1);

        assertEquals(MachineStatus.IDLE, machine.status());
        assertNull(machine.activeRecipe());
        assertEquals(1, machine.completedProcesses());
        assertEquals(1, machine.inventory(MachineEndpoint.OUTPUT).count(ironIngot()));
    }

    @Test
    void restoreDoesNotAccidentallyStartSelectedRecipe() {
        SimulationScheduler scheduler = new SimulationScheduler();
        MachineState machine = new MachineState(scheduler);

        ProcessRecipe recipe = testRecipe(3);

        machine.restore(recipe, null, MachineStatus.IDLE, 0, 0,
            java.util.Map.of(rawIron(), 1), java.util.Map.of());

        assertEquals(MachineStatus.IDLE, machine.status());
        assertNull(machine.activeRecipe());
        assertEquals(1, machine.inventory(MachineEndpoint.INPUT).count(rawIron()));
    }

    private static ProcessRecipe testRecipe(long durationTicks) {
        return new ProcessRecipe(testRecipeId(), ProcessCategory.SMELTING, durationTicks,
            List.of(new ProcessRecipe.ItemAmount(rawIron(), 1)),
            List.of(new ProcessRecipe.ItemAmount(ironIngot(), 1))
        );
    }

    private static ProcessRecipe secondRecipe(long durationTicks) {
        return new ProcessRecipe(
            Identifier.fromNamespaceAndPath("steelmeridian", "second_test_recipe"),
            ProcessCategory.SMELTING, durationTicks,
            List.of(new ProcessRecipe.ItemAmount(rawIron(), 1)),
            List.of(new ProcessRecipe.ItemAmount(ironIngot(), 1))
        );
    }

    private static Identifier testRecipeId() {
        return Identifier.fromNamespaceAndPath("steelmeridian", "test_recipe");
    }

    private static Identifier rawIron() {
        return Identifier.fromNamespaceAndPath("minecraft", "raw_iron");
    }

    private static Identifier ironIngot() {
        return Identifier.fromNamespaceAndPath("minecraft", "iron_ingot");
    }

    private static Identifier cobblestone() {
        return Identifier.fromNamespaceAndPath("minecraft", "cobblestone");
    }
}
