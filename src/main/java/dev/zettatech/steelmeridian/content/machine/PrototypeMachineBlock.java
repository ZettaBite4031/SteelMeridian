package dev.zettatech.steelmeridian.content.machine;

import com.mojang.serialization.MapCodec;
import dev.zettatech.steelmeridian.content.ModContent;
import dev.zettatech.steelmeridian.simulation.machine.MachineState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import dev.zettatech.steelmeridian.simulation.machine.MachineEndpoint;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PrototypeMachineBlockEntity blockEntity) {
            MachineState machine = blockEntity.machineState();
            player.displayClientMessage(
                Component.literal(
                    "Machine State: " + machine.status()
                        + " | Input: " + machine.inventory(MachineEndpoint.INPUT).size()
                        + " | Output: " + machine.inventory(MachineEndpoint.OUTPUT).size()
                        + " | Completed: " + machine.completedProcesses()
                ),
                false
            );
        }

        return InteractionResult.SUCCESS;
    }
}
