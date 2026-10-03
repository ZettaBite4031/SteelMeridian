package dev.zettatech.steelmeridian.simulation.recipe;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProcessRecipeTest {
    @Test
    void createsValidRecipe() {
        ProcessRecipe recipe = new ProcessRecipe(
            Identifier.fromNamespaceAndPath("steelmeridian", "iron_plate"),
            ProcessCategory.SMELTING,
            64,
            List.of(item("minecraft", "raw_iron", 1)),
            List.of(item("minecraft", "iron_ingot", 1))
        );

        assertEquals(
            Identifier.fromNamespaceAndPath("steelmeridian", "iron_plate"),
            recipe.id()
        );

        assertEquals(ProcessCategory.SMELTING, recipe.category());
        assertEquals(64, recipe.durationTicks());
        assertEquals(1, recipe.inputs().size());
        assertEquals(1, recipe.outputs().size());
    }

    @Test
    void rejectsInvalidDuration() {
        assertThrows(IllegalArgumentException.class, () ->
            new ProcessRecipe(
                Identifier.fromNamespaceAndPath("steelmeridian", "invalid"),
                ProcessCategory.SMELTING,
                0,
                List.of(item("minecraft", "raw_iron", 1)),
                List.of(item("minecraft", "iron_ingot", 1))
            )
        );
    }

    @Test
    void rejectsEmptyInputs() {
        assertThrows(IllegalArgumentException.class, () ->
            new ProcessRecipe(
                Identifier.fromNamespaceAndPath("steelmeridian", "invalid"),
                ProcessCategory.SMELTING,
                64,
                List.of(),
                List.of(item("minecraft", "iron_ingot", 1))
            )
        );
    }

    @Test
    void rejectsInvalidItemCount() {
        assertThrows(IllegalArgumentException.class, () ->
            item("minecraft", "raw_iron", 0)
        );
    }

    private static ProcessRecipe.ItemAmount item(String namespace, String path, int count) {
        return new ProcessRecipe.ItemAmount(
            Identifier.fromNamespaceAndPath(namespace, path),
            count
        );
    }
}
