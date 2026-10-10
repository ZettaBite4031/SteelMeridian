package dev.zettatech.steelmeridian.simulation.recipe;

import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
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

    public int indexOf(ProcessRecipe recipe) {
        if (recipe == null) {
            return -1;
        }

        int index = 0;
        for (ProcessRecipe candidate : recipes.values()) {
            if (candidate.id().equals(recipe.id())) {
                return index;
            }
            index++;
        }

        return -1;
    }

    public Optional<ProcessRecipe> byIndex(int index) {
        if (index < 0 || index >= recipes.size()) {
            return Optional.empty();
        }

        return recipes.values().stream().skip(index).findFirst();
    }
}


