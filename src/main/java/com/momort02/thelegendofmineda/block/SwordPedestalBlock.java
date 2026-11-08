package com.momort02.thelegendofmineda.block;

import com.momort02.thelegendofmineda.TheLegendOfMineda;
import com.momort02.thelegendofmineda.block.entity.SwordPedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;

public class SwordPedestalBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2, 0, 2, 14, 2, 14),
            Block.box(3, 2, 3, 13, 4, 13));

    public SwordPedestalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SwordPedestalBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof SwordPedestalBlockEntity pedestal)) {
            return InteractionResult.PASS;
        }

        if (heldItem.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (heldItem.getItem() != TheLegendOfMineda.LEGENDARY_SWORD.get() || pedestal.hasSword()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        pedestal.setSword(heldItem.copyWithCount(1));
        if (!player.isCreative()) {
            heldItem.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof SwordPedestalBlockEntity pedestal) || !pedestal.hasSword()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack sword = pedestal.removeSword();
        if (!player.getInventory().add(sword)) {
            popResource(level, pos, sword);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void playerDestroy(
            ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack destroyedWith) {
        if (!player.isCreative() && blockEntity instanceof SwordPedestalBlockEntity pedestal) {
            ItemStack sword = pedestal.removeSword();
            if (!sword.isEmpty()) {
                popResource(level, pos, sword);
            }
        }
        super.playerDestroy(level, player, pos, state, blockEntity, destroyedWith);
    }
}
