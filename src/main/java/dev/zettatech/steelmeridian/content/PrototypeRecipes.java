package dev.zettatech.steelmeridian.content;

import dev.zettatech.steelmeridian.SteelMeridian;
import dev.zettatech.steelmeridian.simulation.recipe.ProcessCategory;
import dev.zettatech.steelmeridian.simulation.recipe.ProcessRecipe;
import dev.zettatech.steelmeridian.simulation.recipe.ProcessRecipeRegistry;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class PrototypeRecipes {
    public static final ProcessRecipeRegistry REGISTRY =
        new ProcessRecipeRegistry();

    public static final ProcessRecipe IRON_PROCESSING =
        new ProcessRecipe(
            Identifier.fromNamespaceAndPath(
                SteelMeridian.MOD_ID,
                "prototype_iron_processing"
            ),
            ProcessCategory.SMELTING,
            60,
            List.of(new ProcessRecipe.ItemAmount(Identifier.fromNamespaceAndPath("minecraft", "raw_iron"), 1)),
            List.of(new ProcessRecipe.ItemAmount(Identifier.fromNamespaceAndPath("minecraft","iron_ingot"),1))
        );

    static {
        REGISTRY.register(IRON_PROCESSING);
    }

    private PrototypeRecipes() {
    }
}
