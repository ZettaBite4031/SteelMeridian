package dev.zettatech.steelmeridian.simulation.recipe;

import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ProcessRecipeRegistry {
    private final Map<Identifier, ProcessRecipe> recipes = new LinkedHashMap<>();

    public void register(ProcessRecipe recipe) {
        Objects.requireNonNull(recipe, "recipe");

        if (recipes.putIfAbsent(recipe.id(), recipe) != null) {
            throw new IllegalArgumentException("Duplicate process recipe: " + recipe.id());
        }
    }

    public Optional<ProcessRecipe> get(Identifier id) {
        return Optional.ofNullable(recipes.get(id));
    }

    public Collection<ProcessRecipe> all() {
        return recipes.values();
    }

    public Collection<ProcessRecipe> byCategory(ProcessCategory category) {
        return recipes.values().stream().filter(recipe -> recipe.category() == category).toList();
    }
}


