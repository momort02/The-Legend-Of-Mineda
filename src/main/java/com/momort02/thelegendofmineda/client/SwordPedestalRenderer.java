package com.momort02.thelegendofmineda.client;

import com.momort02.thelegendofmineda.block.entity.SwordPedestalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SwordPedestalRenderer implements BlockEntityRenderer<SwordPedestalBlockEntity, SwordPedestalRenderState> {
    private final ItemModelResolver itemModelResolver;

    public SwordPedestalRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public SwordPedestalRenderState createRenderState() {
        return new SwordPedestalRenderState();
    }

    @Override
    public void extractRenderState(
            SwordPedestalBlockEntity blockEntity,
            SwordPedestalRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        var displayStack = blockEntity.getSword().copy();
        if (!displayStack.isEmpty()) {
            displayStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        itemModelResolver.updateForTopItem(
                state.sword, displayStack, ItemDisplayContext.NONE, blockEntity.getLevel(), null, (int) blockEntity.getBlockPos().asLong());
    }

    @Override
    public void submit(SwordPedestalRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.sword.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0F, 0.5F);
        state.sword.submit(poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
