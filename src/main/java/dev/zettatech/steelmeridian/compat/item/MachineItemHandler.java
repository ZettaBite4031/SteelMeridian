package dev.zettatech.steelmeridian.compat.item;

import dev.zettatech.steelmeridian.simulation.machine.MachineEndpoint;
import dev.zettatech.steelmeridian.simulation.machine.MachineInventory;
import dev.zettatech.steelmeridian.simulation.machine.MachineState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Map;
import java.util.Objects;


public final class MachineItemHandler extends SnapshotJournal<Map<Identifier, Integer>> implements ResourceHandler<ItemResource> {
    private final MachineInventory inventory;
    private final MachineEndpoint endpoint;

    public MachineItemHandler(MachineState machine, MachineEndpoint endpoint) {
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.inventory = machine.inventory(endpoint);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public ItemResource getResource(int index) {
        Objects.checkIndex(index, size());

        Identifier itemId = inventory.firstItem();

        if (itemId == null) {
            return ItemResource.EMPTY;
        }

        Item item = BuiltInRegistries.ITEM.getValue(itemId);

        return ItemResource.of(item);
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());

        Identifier itemId = inventory.firstItem();

        return itemId == null ? 0 : inventory.count(itemId);
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        return inventory.capacity();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        Objects.checkIndex(index, size());

        return endpoint == MachineEndpoint.INPUT && !resource.isEmpty();
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());

        if (endpoint != MachineEndpoint.INPUT || amount <= 0 || resource.isEmpty()) {
            return 0;
        }

        Identifier id = BuiltInRegistries.ITEM.getKey(resource.getItem());

        int insertable = Math.min(amount, inventory.remainingCapacity());

        if (insertable <= 0) {
            return 0;
        }

        updateSnapshots(transaction);

        return inventory.insert(id, insertable) ? insertable : 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());

        if (endpoint != MachineEndpoint.OUTPUT || amount <= 0 || resource.isEmpty()) {
            return 0;
        }

        Identifier id = BuiltInRegistries.ITEM.getKey(resource.getItem());

        int extractable = Math.min(amount, inventory.count(id));

        if (extractable <= 0) {
            return 0;
        }

        updateSnapshots(transaction);

        return inventory.remove(id, extractable) ? extractable : 0;
    }

    @Override
    protected Map<Identifier, Integer> createSnapshot() {
        return inventory.snapshot();
    }

    @Override
    protected void revertToSnapshot(Map<Identifier, Integer> snapshot) {
        inventory.restore(snapshot);
    }
}
