package dev.zettatech.steelmeridian.simulation.recipe;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;

public record ProcessRecipe(
    Identifier id,
    ProcessCategory category,
    long durationTicks,
    List<ItemAmount> inputs,
    List<ItemAmount> outputs
) {
    public ProcessRecipe {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(inputs, "inputs");
        Objects.requireNonNull(outputs, "outputs");

        if (durationTicks <= 0) {
            throw new IllegalArgumentException("durationTicks must be greater than zero");
        }

        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("recipe must have at least one input");
        }

        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("recipe must have at least one output");
        }

        inputs = List.copyOf(inputs);
        outputs = List.copyOf(outputs);
    }

    public record ItemAmount(Identifier item, int count) {
        public ItemAmount {
            Objects.requireNonNull(item, "item");

            if (count <= 0) {
                throw new IllegalArgumentException("count must be greater than zero");
            }
        }
    }
}
