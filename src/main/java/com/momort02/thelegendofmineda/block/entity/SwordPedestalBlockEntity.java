package com.momort02.thelegendofmineda.block.entity;

import com.momort02.thelegendofmineda.TheLegendOfMineda;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SwordPedestalBlockEntity extends BlockEntity {
    private ItemStack sword = ItemStack.EMPTY;

    public SwordPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(TheLegendOfMineda.SWORD_PEDESTAL_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStack getSword() {
        return sword;
    }

    public boolean hasSword() {
        return !sword.isEmpty();
    }

    public void setSword(ItemStack sword) {
        this.sword = sword;
        setChanged();
        syncBlock();
    }

    public ItemStack removeSword() {
        ItemStack removedSword = sword;
        sword = ItemStack.EMPTY;
        setChanged();
        syncBlock();
        return removedSword;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        sword = input.read("Sword", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!sword.isEmpty()) {
            output.store("Sword", ItemStack.CODEC, sword);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private void syncBlock() {
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
