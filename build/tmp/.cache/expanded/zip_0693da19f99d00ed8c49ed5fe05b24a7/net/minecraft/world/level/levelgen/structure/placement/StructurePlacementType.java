package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface StructurePlacementType<SP extends StructurePlacement> {
    StructurePlacementType<RandomSpreadStructurePlacement> RANDOM_SPREAD = register("random_spread", RandomSpreadStructurePlacement.CODEC);
    StructurePlacementType<ConcentricRingsStructurePlacement> CONCENTRIC_RINGS = register("concentric_rings", ConcentricRingsStructurePlacement.CODEC);

    Codec<SP> codec();

    private static <SP extends StructurePlacement> StructurePlacementType<SP> register(String p_205047_, Codec<SP> p_205048_) {
        return Registry.register(BuiltInRegistries.STRUCTURE_PLACEMENT, p_205047_, () -> p_205048_);
    }
}
