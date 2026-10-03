package dev.zettatech.steelmeridian.simulation.scheduler;

final class ScheduledTask {
    private final long dueTick;
    private final long sequence;
    private final Runnable action;

    private boolean cancelled;

    ScheduledTask(long dueTick, long sequence, Runnable action) {
        this.dueTick = dueTick;
        this.sequence = sequence;
        this.action = action;
    }

    long dueTick() {
        return dueTick;
    }

    long sequence() {
        return sequence;
    }

    boolean isCancelled() {
        return cancelled;
    }

    void cancel() {
        cancelled = true;
    }

    void execute() {
        action.run();
    }
}
