package com.momort02.thelegendofmineda.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momort02.thelegendofmineda.TheLegendOfMineda;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class LegendarySwordShrineData extends SavedData {
    private static final Codec<LegendarySwordShrineData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("generated", false).forGetter(data -> data.generated))
            .apply(instance, LegendarySwordShrineData::new));

    public static final SavedDataType<LegendarySwordShrineData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(TheLegendOfMineda.MODID, "legendary_sword_shrine"),
            LegendarySwordShrineData::new,
            CODEC);

    private boolean generated;

    public LegendarySwordShrineData() {
        this(false);
    }

    private LegendarySwordShrineData(boolean generated) {
        this.generated = generated;
    }

    public boolean isGenerated() {
        return generated;
    }

    public void markGenerated() {
        if (!generated) {
            generated = true;
            setDirty();
        }
    }
}
