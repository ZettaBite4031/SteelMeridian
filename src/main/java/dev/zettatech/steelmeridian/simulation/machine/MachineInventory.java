package dev.zettatech.steelmeridian.simulation.machine;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class MachineInventory {
    private final Map<Identifier, Integer> items = new HashMap<>();
    private final int capacity;
    private final Runnable onChanged;

    public MachineInventory(int capacity) {
        this(capacity, () -> {});
    }

    public MachineInventory(int capacity, Runnable onChanged) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be greater than zero");
        }

        this.capacity = capacity;
        this.onChanged = Objects.requireNonNull(onChanged, "onChanged");
    }

    public int count(Identifier item) {
        return items.getOrDefault(item, 0);
    }

    public int size() {
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int remainingCapacity() {
        return capacity - size();
    }

    public boolean canInsert(int count) {
        return count > 0 && remainingCapacity() >= count;
    }

    public boolean insert(Identifier item, int count) {
        Objects.requireNonNull(item, "item");

        if (!canInsert(count)) {
            return false;
        }

        items.merge(item, count, Integer::sum);
        onChanged.run();

        return true;
    }

    public boolean remove(Identifier item, int count) {
        Objects.requireNonNull(item, "item");

        if (count <= 0 || count(item) < count) {
            return false;
        }

        int remaining = count(item) - count;

        if (remaining == 0) {
            items.remove(item);
        } else {
            items.put(item, remaining);
        }

        onChanged.run();

        return true;
    }

    public boolean contains(Identifier item, int count) {
        return count > 0 && count(item) >= count;
    }

    public int capacity() {
        return capacity;
    }

    public Identifier firstItem() {
        return items.keySet().stream().findFirst().orElse(null);
    }

    public Map<Identifier, Integer> snapshot() {
        return Map.copyOf(items);
    }

    public void restore(Map<Identifier, Integer> snapshot) {
        items.clear();
        items.putAll(snapshot);
        onChanged.run();
    }
}
