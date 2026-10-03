package dev.zettatech.steelmeridian.simulation.scheduler;

import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;

public final class SimulationScheduler {
    private final PriorityQueue<ScheduledTask> tasks = new PriorityQueue<>(Comparator.comparingLong(ScheduledTask::dueTick).thenComparingLong(ScheduledTask::sequence));

    private long currentTick;
    private long nextSequence;

    public void schedule(long delayTicks, Runnable action) {
        if (delayTicks <= 0)
            throw new IllegalArgumentException("delayTicks must be greater than 0");

        Objects.requireNonNull(action, "action");

        tasks.add(new ScheduledTask(currentTick + delayTicks, nextSequence++, action));
    }

    public void tick() {
        currentTick++;

        while (!tasks.isEmpty() && tasks.peek().dueTick() <= currentTick) {
            ScheduledTask task = tasks.poll();
            if (!task.isCancelled()) task.execute();
        }
    }

    public void clear() {
        tasks.clear();
        currentTick = 0;
        nextSequence = 0;
    }

    public long currentTick() {
        return currentTick;
    }

    public int scheduledTaskCount() {
        return tasks.size();
    }


}
