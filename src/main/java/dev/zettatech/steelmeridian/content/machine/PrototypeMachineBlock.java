package dev.zettatech.steelmeridian.content.machine;

import com.mojang.serialization.MapCodec;
import dev.zettatech.steelmeridian.content.PrototypeRecipes;
import dev.zettatech.steelmeridian.simulation.machine.MachineEndpoint;
import dev.zettatech.steelmeridian.simulation.machine.MachineState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class PrototypeMachineBlock extends Block implements EntityBlock {
    public static final MapCodec<PrototypeMachineBlock> CODEC = BlockBehaviour.simpleCodec(PrototypeMachineBlock::new);

    public PrototypeMachineBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PrototypeMachineBlockEntity(pos, state);
    }

    @Override
    public @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof PrototypeMachineBlockEntity machine ? machine : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
            && level.getBlockEntity(pos) instanceof PrototypeMachineBlockEntity blockEntity) {

            serverPlayer.openMenu(blockEntity, buffer -> buffer.writeBlockPos(pos));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos) instanceof PrototypeMachineBlockEntity blockEntity)) {
            return InteractionResult.PASS;
        }

        MachineState machine = blockEntity.machineState();

        if (stack.is(Items.RAW_IRON)) {
            boolean inserted = machine.inventory(MachineEndpoint.INPUT).insert(Identifier.fromNamespaceAndPath("minecraft", "raw_iron"), 1);
            if (inserted) {
                stack.shrink(1);
                player.displayClientMessage(Component.literal("Inserted 1 raw iron."), false);

                return InteractionResult.SUCCESS_SERVER;
            }

            player.displayClientMessage(Component.literal("Input inventory is full"), false);

            return InteractionResult.SUCCESS_SERVER;
        }

        if (stack.is(Items.STICK)) {
            boolean selected = machine.selectRecipe(PrototypeRecipes.IRON_PROCESSING);
            player.displayClientMessage(Component.literal(selected ? "Selected prototype iron recipe." : "Recipe could not be changed"), false);

            return InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}
