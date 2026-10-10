package dev.zettatech.steelmeridian.content.machine;

import dev.zettatech.steelmeridian.content.ModContent;
import dev.zettatech.steelmeridian.content.PrototypeRecipes;
import dev.zettatech.steelmeridian.simulation.machine.MachineEndpoint;
import dev.zettatech.steelmeridian.simulation.machine.MachineState;
import dev.zettatech.steelmeridian.simulation.machine.MachineStatus;
import dev.zettatech.steelmeridian.simulation.recipe.ProcessRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public final class PrototypeMachineMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = 7;
    private static final int DATA_STATUS = 0;
    private static final int DATA_SELECTED_RECIPE = 1;
    private static final int DATA_ACTIVE_RECIPE = 2;
    private static final int DATA_REMAINING_TICKS = 3;
    private static final int DATA_TOTAL_TICKS = 4;
    private static final int DATA_INPUT_COUNT = 5;
    private static final int DATA_OUTPUT_COUNT = 6;

    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final BlockPos blockPos;

    public PrototypeMachineMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readBlockPos(), new SimpleContainerData(DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public PrototypeMachineMenu(int containerId, Inventory playerInventory, PrototypeMachineBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity.getBlockPos(), createMachineData(blockEntity.machineState()),
            ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()));
    }

    private PrototypeMachineMenu(int containerId, Inventory playerInventory, BlockPos blockPos, ContainerData data, ContainerLevelAccess access) {
        super(ModContent.PROTOTYPE_MACHINE_MENU.get(), containerId);
        this.data = data;
        this.access = access;
        this.blockPos = blockPos;

        checkContainerDataCount(data, DATA_COUNT);
        addDataSlots(data);

        addStandardInventorySlots(playerInventory, 8, 84);
    }

    private static ContainerData createMachineData(MachineState machine) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_STATUS -> machine.status().ordinal();
                    case DATA_SELECTED_RECIPE -> PrototypeRecipes.REGISTRY.indexOf(machine.selectedRecipe()) + 1;
                    case DATA_ACTIVE_RECIPE -> PrototypeRecipes.REGISTRY.indexOf(machine.activeRecipe()) + 1;
                    case DATA_REMAINING_TICKS -> (int) Math.min(Integer.MAX_VALUE, machine.remainingProcessTicks());
                    case DATA_TOTAL_TICKS -> machine.activeRecipe() == null
                        ? 0
                        : (int) Math.min(Integer.MAX_VALUE, machine.activeRecipe().durationTicks());
                    case DATA_INPUT_COUNT -> machine.inventory(MachineEndpoint.INPUT).size();
                    case DATA_OUTPUT_COUNT -> machine.inventory(MachineEndpoint.OUTPUT).size();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // Read-only synchronization. The server owns machine state.
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public MachineStatus status() {
        int ordinal = data.get(DATA_STATUS);
        MachineStatus[] values = MachineStatus.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : MachineStatus.IDLE;
    }

    public ProcessRecipe selectedRecipe() {
        return PrototypeRecipes.REGISTRY.byIndex(data.get(DATA_SELECTED_RECIPE) - 1).orElse(null);
    }

    public ProcessRecipe activeRecipe() {
        return PrototypeRecipes.REGISTRY.byIndex(data.get(DATA_ACTIVE_RECIPE) - 1).orElse(null);
    }

    public int remainingTicks() {
        return data.get(DATA_REMAINING_TICKS);
    }

    public int totalTicks() {
        return data.get(DATA_TOTAL_TICKS);
    }

    public int inputCount() {
        return data.get(DATA_INPUT_COUNT);
    }

    public int outputCount() {
        return data.get(DATA_OUTPUT_COUNT);
    }

    public float progress() {
        int total = totalTicks();
        return total <= 0 ? 0.f : 1.f - ((float) remainingTicks() / total);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, ModContent.PROTOTYPE_MACHINE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return ItemStack.EMPTY;
        }

        var slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack moved = stack.copy();

        if (slotIndex < 27) {
            if (!moveItemStackTo(stack, 27, 36, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < 36) {
            if (!moveItemStackTo(stack, 0, 27, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return moved;
    }
}
