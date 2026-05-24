package com.sniklz.repelreforged.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import com.sniklz.repelreforged.RepelReforged;

public class RepelBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final int blockLevel;

    public RepelBlock(Properties properties, int blockLevel) {
        super(properties);
        this.blockLevel = blockLevel;
    }

    public int getBlockLevel() {
        return blockLevel;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        if (random.nextFloat() < 0.1F) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;

            level.addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0, 0.02, 0);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(4, 0, 4, 12, 9, 12);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isCrouching()) {
            // ONLY act on the server. The server will send the particles to the client.
            if (!level.isClientSide) {
                // Use the level passed in to get the TRUE gamerule value
                int baseRange = level.getGameRules().getInt(RepelReforged.REPEL_RANGE);
                int multiplier = getMultiplier(level); 
                int totalRange = baseRange * multiplier;

                // Debug to server console to verify
                RepelReforged.LOGGER.info("Server-side Repel Radius: " + totalRange);

                spawnServerCircleParticles((ServerLevel) level, pos, totalRange);
            }
            // Return SUCCESS so the arm swings and the client knows the action was handled
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private int getMultiplier(Level level) {
        // Access the gamerules from the provided level context
        return switch (this.blockLevel) {
            case 2 -> level.getGameRules().getInt(RepelReforged.SUPER_REPEL_RANGE_MULTIPLIER);
            case 3 -> level.getGameRules().getInt(RepelReforged.MAX_REPEL_RANGE_MULTIPLIER);
            default -> 1;
        };
    }

    private void spawnServerCircleParticles(ServerLevel level, BlockPos pos, int radius) {
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 0.2;
        double centerZ = pos.getZ() + 0.5;

        // Logic Check: If radius is 0, the circle is invisible. 
        if (radius <= 0) return;

        for (int i = 0; i < 360; i += 5) {
            double radians = Math.toRadians(i);
            double x = centerX + (radius * Math.cos(radians));
            double z = centerZ + (radius * Math.sin(radians));

            // sendParticles(particle, x, y, z, count, deltaX, deltaY, deltaZ, speed)
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, centerY, z, 1, 0, 0, 0, 0);
        }
    }
}
