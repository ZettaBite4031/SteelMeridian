package dev.zettatech.steelmeridian.simulation.scheduler;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulationSchedulerTest {
    @Test
    void taskDoesNotExecuteEarly() {
        SimulationScheduler scheduler = new SimulationScheduler();
        AtomicInteger executions = new AtomicInteger();

        scheduler.schedule(3, executions::incrementAndGet);

        scheduler.tick();
        scheduler.tick();

        assertEquals(0, executions.get());
    }

    @Test
    void taskExecutesOnScheduledTick() {
        SimulationScheduler scheduler = new SimulationScheduler();
        AtomicInteger executions = new AtomicInteger();

        scheduler.schedule(3, executions::incrementAndGet);

        scheduler.tick();
        scheduler.tick();
        scheduler.tick();

        assertEquals(1, executions.get());
    }

    @Test
    void taskExecutesOnlyOnce() {
        SimulationScheduler scheduler = new SimulationScheduler();
        AtomicInteger executions = new AtomicInteger();

        scheduler.schedule(1, executions::incrementAndGet);

        scheduler.tick();
        scheduler.tick();
        scheduler.tick();

        assertEquals(1, executions.get());
    }

    @Test
    void tasksExecuteInScheduledOrder() {
        SimulationScheduler scheduler = new SimulationScheduler();
        List<Integer> order = new ArrayList<>();

        scheduler.schedule(3, () -> order.add(3));
        scheduler.schedule(1, () -> order.add(1));
        scheduler.schedule(2, () -> order.add(2));

        scheduler.tick();
        scheduler.tick();
        scheduler.tick();

        assertEquals(List.of(1, 2, 3), order);
    }

    @Test
    void tasksOnSameTickExecuteInInsertionOrder() {
        SimulationScheduler scheduler = new SimulationScheduler();
        List<Integer> order = new ArrayList<>();

        scheduler.schedule(1, () -> order.add(1));
        scheduler.schedule(1, () -> order.add(2));
        scheduler.schedule(1, () -> order.add(3));

        scheduler.tick();

        assertEquals(List.of(1, 2, 3), order);
    }

    @Test
    void emptySchedulerDoesNothing() {
        SimulationScheduler scheduler = new SimulationScheduler();

        scheduler.tick();

        assertEquals(1, scheduler.currentTick());
        assertEquals(0, scheduler.scheduledTaskCount());
    }
}
