package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;

public class DistanceTrigger extends SimpleCriterionTrigger<DistanceTrigger.TriggerInstance> {
    @Override
    public Codec<DistanceTrigger.TriggerInstance> codec() {
        return DistanceTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_186166_, Vec3 p_186167_) {
        Vec3 vec3 = p_186166_.position();
        this.trigger(p_186166_, p_284572_ -> p_284572_.matches(p_186166_.serverLevel(), p_186167_, vec3));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player, Optional<LocationPredicate> startPosition, Optional<DistancePredicate> distance
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<DistanceTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_311408_ -> p_311408_.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(DistanceTrigger.TriggerInstance::player),
                        ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "start_position").forGetter(DistanceTrigger.TriggerInstance::startPosition),
                        ExtraCodecs.strictOptionalField(DistancePredicate.CODEC, "distance").forGetter(DistanceTrigger.TriggerInstance::distance)
                    )
                    .apply(p_311408_, DistanceTrigger.TriggerInstance::new)
        );

        public static Criterion<DistanceTrigger.TriggerInstance> fallFromHeight(
            EntityPredicate.Builder p_186198_, DistancePredicate p_186199_, LocationPredicate.Builder p_299057_
        ) {
            return CriteriaTriggers.FALL_FROM_HEIGHT
                .createCriterion(
                    new DistanceTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(p_186198_)), Optional.of(p_299057_.build()), Optional.of(p_186199_))
                );
        }

        public static Criterion<DistanceTrigger.TriggerInstance> rideEntityInLava(EntityPredicate.Builder p_186195_, DistancePredicate p_186196_) {
            return CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER
                .createCriterion(new DistanceTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(p_186195_)), Optional.empty(), Optional.of(p_186196_)));
        }

        public static Criterion<DistanceTrigger.TriggerInstance> travelledThroughNether(DistancePredicate p_186193_) {
            return CriteriaTriggers.NETHER_TRAVEL
                .createCriterion(new DistanceTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(p_186193_)));
        }

        public boolean matches(ServerLevel p_186189_, Vec3 p_186190_, Vec3 p_186191_) {
            if (this.startPosition.isPresent() && !this.startPosition.get().matches(p_186189_, p_186190_.x, p_186190_.y, p_186190_.z)) {
                return false;
            } else {
                return !this.distance.isPresent() || this.distance.get().matches(p_186190_.x, p_186190_.y, p_186190_.z, p_186191_.x, p_186191_.y, p_186191_.z);
            }
        }
    }
}
