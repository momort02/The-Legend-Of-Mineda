package com.momort02.thelegendofmineda.network;

import com.momort02.thelegendofmineda.TheLegendOfMineda;
import com.momort02.thelegendofmineda.item.LegendarySwordCharge;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LegendarySwordChargePayload(boolean charging) implements CustomPacketPayload {
    public static final Type<LegendarySwordChargePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TheLegendOfMineda.MODID, "legendary_sword_charge"));
    public static final StreamCodec<ByteBuf, LegendarySwordChargePayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, LegendarySwordChargePayload::charging, LegendarySwordChargePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, LegendarySwordChargePayload::handle);
    }

    private static void handle(LegendarySwordChargePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                if (payload.charging()) {
                    LegendarySwordCharge.start(player);
                } else {
                    LegendarySwordCharge.cancel(player);
                }
            }
        });
    }
}
