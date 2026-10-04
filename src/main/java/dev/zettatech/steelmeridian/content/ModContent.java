package dev.zettatech.steelmeridian.content;

import com.mojang.serialization.MapCodec;
import dev.zettatech.steelmeridian.SteelMeridian;
import dev.zettatech.steelmeridian.content.machine.PrototypeMachineBlock;
import dev.zettatech.steelmeridian.content.machine.PrototypeMachineBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public final class ModContent {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SteelMeridian.MOD_ID);

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SteelMeridian.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SteelMeridian.MOD_ID);

    public static final DeferredRegister<MapCodec<? extends Block>> BLOCK_TYPES = DeferredRegister.create(Registries.BLOCK_TYPE, SteelMeridian.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<PrototypeMachineBlock>> PROTOTYPE_MACHINE_BLOCK_TYPE = BLOCK_TYPES.register("prototype_machine", () -> PrototypeMachineBlock.CODEC);

    public static final DeferredBlock<PrototypeMachineBlock> PROTOTYPE_MACHINE = BLOCKS.registerBlock("prototype_machine", PrototypeMachineBlock::new);

    public static final DeferredItem<BlockItem> PROTOTYPE_MACHINE_ITEM = ITEMS.registerSimpleBlockItem(PROTOTYPE_MACHINE);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PrototypeMachineBlockEntity>> PROTOTYPE_MACHINE_BLOCK_ENTITY = BLOCK_ENTITIES.register("prototype_machine", () -> new BlockEntityType<>(PrototypeMachineBlockEntity::new, false, PROTOTYPE_MACHINE.get()));

    private ModContent() {}

    public static void register(IEventBus modBus) {
        BLOCK_TYPES.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }
}
