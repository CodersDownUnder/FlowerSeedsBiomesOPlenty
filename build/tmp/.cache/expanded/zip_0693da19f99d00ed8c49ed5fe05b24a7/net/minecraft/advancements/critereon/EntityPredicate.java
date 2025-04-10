package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public record EntityPredicate(
    Optional<EntityTypePredicate> entityType,
    Optional<DistancePredicate> distanceToPlayer,
    Optional<LocationPredicate> location,
    Optional<LocationPredicate> steppingOnLocation,
    Optional<MobEffectsPredicate> effects,
    Optional<NbtPredicate> nbt,
    Optional<EntityFlagsPredicate> flags,
    Optional<EntityEquipmentPredicate> equipment,
    Optional<EntitySubPredicate> subPredicate,
    Optional<EntityPredicate> vehicle,
    Optional<EntityPredicate> passenger,
    Optional<EntityPredicate> targetedEntity,
    Optional<String> team,
    Optional<net.neoforged.neoforge.common.advancements.critereon.ICustomEntityPredicate> customLogic
) {
    public static final Codec<EntityPredicate> VANILLA_CODEC = ExtraCodecs.recursive(
        "EntityPredicate",
        p_297888_ -> RecordCodecBuilder.create(
                p_297890_ -> p_297890_.group(
                            ExtraCodecs.strictOptionalField(EntityTypePredicate.CODEC, "type").forGetter(EntityPredicate::entityType),
                            ExtraCodecs.strictOptionalField(DistancePredicate.CODEC, "distance").forGetter(EntityPredicate::distanceToPlayer),
                            ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "location").forGetter(EntityPredicate::location),
                            ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "stepping_on").forGetter(EntityPredicate::steppingOnLocation),
                            ExtraCodecs.strictOptionalField(MobEffectsPredicate.CODEC, "effects").forGetter(EntityPredicate::effects),
                            ExtraCodecs.strictOptionalField(NbtPredicate.CODEC, "nbt").forGetter(EntityPredicate::nbt),
                            ExtraCodecs.strictOptionalField(EntityFlagsPredicate.CODEC, "flags").forGetter(EntityPredicate::flags),
                            ExtraCodecs.strictOptionalField(EntityEquipmentPredicate.CODEC, "equipment").forGetter(EntityPredicate::equipment),
                            ExtraCodecs.strictOptionalField(EntitySubPredicate.CODEC, "type_specific").forGetter(EntityPredicate::subPredicate),
                            ExtraCodecs.strictOptionalField(p_297888_, "vehicle").forGetter(EntityPredicate::vehicle),
                            ExtraCodecs.strictOptionalField(p_297888_, "passenger").forGetter(EntityPredicate::passenger),
                            ExtraCodecs.strictOptionalField(p_297888_, "targeted_entity").forGetter(EntityPredicate::targetedEntity),
                            ExtraCodecs.strictOptionalField(Codec.STRING, "team").forGetter(EntityPredicate::team)
                        )
                        .apply(p_297890_, EntityPredicate::new)
            )
    );
    public static final Codec<EntityPredicate> CODEC = ExtraCodecs.<net.neoforged.neoforge.common.advancements.critereon.ICustomEntityPredicate, EntityPredicate>either(
            net.neoforged.neoforge.registries.NeoForgeRegistries.ENTITY_PREDICATE_SERIALIZERS.byNameCodec()
                    .dispatch(
                            net.neoforged.neoforge.common.advancements.critereon.ICustomEntityPredicate::codec,
                            java.util.function.Function.identity()),
            VANILLA_CODEC
        ).xmap(either -> either.map(EntityPredicate::new, p -> p),
          // Serialize using dispatch codec if custom logic is present, otherwise use vanilla codec
          predicate -> predicate.customLogic.<com.mojang.datafixers.util.Either<net.neoforged.neoforge.common.advancements.critereon.ICustomEntityPredicate, EntityPredicate>>map(
                com.mojang.datafixers.util.Either::left).orElseGet(() -> com.mojang.datafixers.util.Either.right(predicate)));
    public static final Codec<ContextAwarePredicate> ADVANCEMENT_CODEC = ExtraCodecs.withAlternative(ContextAwarePredicate.CODEC, CODEC, EntityPredicate::wrap);

    public EntityPredicate(Optional<EntityTypePredicate> entityType, Optional<DistancePredicate> distanceToPlayer, Optional<LocationPredicate> location, Optional<LocationPredicate> steppingOnLocation, Optional<MobEffectsPredicate> effects, Optional<NbtPredicate> nbt, Optional<EntityFlagsPredicate> flags, Optional<EntityEquipmentPredicate> equipment, Optional<EntitySubPredicate> subPredicate, Optional<net.minecraft.advancements.critereon.EntityPredicate> vehicle, Optional<net.minecraft.advancements.critereon.EntityPredicate> passenger, Optional<net.minecraft.advancements.critereon.EntityPredicate> targetedEntity, Optional<String> team) {
        this(entityType, distanceToPlayer, location, steppingOnLocation, effects, nbt, flags, equipment, subPredicate, vehicle, passenger, targetedEntity, team, Optional.empty());
    }

    public EntityPredicate(net.neoforged.neoforge.common.advancements.critereon.ICustomEntityPredicate customLogic) {
        this(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(customLogic));
    }

    public static ContextAwarePredicate wrap(EntityPredicate.Builder p_298222_) {
        return wrap(p_298222_.build());
    }

    public static Optional<ContextAwarePredicate> wrap(Optional<EntityPredicate> p_298604_) {
        return p_298604_.map(EntityPredicate::wrap);
    }

    public static List<ContextAwarePredicate> wrap(EntityPredicate.Builder... p_298584_) {
        return Stream.of(p_298584_).map(EntityPredicate::wrap).toList();
    }

    public static ContextAwarePredicate wrap(EntityPredicate p_286570_) {
        LootItemCondition lootitemcondition = LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, p_286570_).build();
        return new ContextAwarePredicate(List.of(lootitemcondition));
    }

    public boolean matches(ServerPlayer p_36612_, @Nullable Entity p_36613_) {
        return this.matches(p_36612_.serverLevel(), p_36612_.position(), p_36613_);
    }

    public boolean matches(ServerLevel p_36608_, @Nullable Vec3 p_36609_, @Nullable Entity p_36610_) {
        if (p_36610_ != null && this.customLogic.isPresent()) {
            return this.customLogic.get().test(p_36608_, p_36609_, p_36610_);
        }
        if (p_36610_ == null) {
            return false;
        } else if (this.entityType.isPresent() && !this.entityType.get().matches(p_36610_.getType())) {
            return false;
        } else {
            if (p_36609_ == null) {
                if (this.distanceToPlayer.isPresent()) {
                    return false;
                }
            } else if (this.distanceToPlayer.isPresent()
                && !this.distanceToPlayer.get().matches(p_36609_.x, p_36609_.y, p_36609_.z, p_36610_.getX(), p_36610_.getY(), p_36610_.getZ())) {
                return false;
            }

            if (this.location.isPresent() && !this.location.get().matches(p_36608_, p_36610_.getX(), p_36610_.getY(), p_36610_.getZ())) {
                return false;
            } else {
                if (this.steppingOnLocation.isPresent()) {
                    Vec3 vec3 = Vec3.atCenterOf(p_36610_.getOnPos());
                    if (!this.steppingOnLocation.get().matches(p_36608_, vec3.x(), vec3.y(), vec3.z())) {
                        return false;
                    }
                }

                if (this.effects.isPresent() && !this.effects.get().matches(p_36610_)) {
                    return false;
                } else if (this.nbt.isPresent() && !this.nbt.get().matches(p_36610_)) {
                    return false;
                } else if (this.flags.isPresent() && !this.flags.get().matches(p_36610_)) {
                    return false;
                } else if (this.equipment.isPresent() && !this.equipment.get().matches(p_36610_)) {
                    return false;
                } else if (this.subPredicate.isPresent() && !this.subPredicate.get().matches(p_36610_, p_36608_, p_36609_)) {
                    return false;
                } else if (this.vehicle.isPresent() && !this.vehicle.get().matches(p_36608_, p_36609_, p_36610_.getVehicle())) {
                    return false;
                } else if (this.passenger.isPresent()
                    && p_36610_.getPassengers().stream().noneMatch(p_297893_ -> this.passenger.get().matches(p_36608_, p_36609_, p_297893_))) {
                    return false;
                } else if (this.targetedEntity.isPresent()
                    && !this.targetedEntity.get().matches(p_36608_, p_36609_, p_36610_ instanceof Mob ? ((Mob)p_36610_).getTarget() : null)) {
                    return false;
                } else {
                    if (this.team.isPresent()) {
                        Team team = p_36610_.getTeam();
                        if (team == null || !this.team.get().equals(team.getName())) {
                            return false;
                        }
                    }

                    return true;
                }
            }
        }
    }

    public static LootContext createContext(ServerPlayer p_36617_, Entity p_36618_) {
        LootParams lootparams = new LootParams.Builder(p_36617_.serverLevel())
            .withParameter(LootContextParams.THIS_ENTITY, p_36618_)
            .withParameter(LootContextParams.ORIGIN, p_36617_.position())
            .create(LootContextParamSets.ADVANCEMENT_ENTITY);
        return new LootContext.Builder(lootparams).create(Optional.empty());
    }

    public static class Builder {
        private Optional<EntityTypePredicate> entityType = Optional.empty();
        private Optional<DistancePredicate> distanceToPlayer = Optional.empty();
        private Optional<LocationPredicate> location = Optional.empty();
        private Optional<LocationPredicate> steppingOnLocation = Optional.empty();
        private Optional<MobEffectsPredicate> effects = Optional.empty();
        private Optional<NbtPredicate> nbt = Optional.empty();
        private Optional<EntityFlagsPredicate> flags = Optional.empty();
        private Optional<EntityEquipmentPredicate> equipment = Optional.empty();
        private Optional<EntitySubPredicate> subPredicate = Optional.empty();
        private Optional<EntityPredicate> vehicle = Optional.empty();
        private Optional<EntityPredicate> passenger = Optional.empty();
        private Optional<EntityPredicate> targetedEntity = Optional.empty();
        private Optional<String> team = Optional.empty();

        public static EntityPredicate.Builder entity() {
            return new EntityPredicate.Builder();
        }

        public EntityPredicate.Builder of(EntityType<?> p_36637_) {
            this.entityType = Optional.of(EntityTypePredicate.of(p_36637_));
            return this;
        }

        public EntityPredicate.Builder of(TagKey<EntityType<?>> p_204078_) {
            this.entityType = Optional.of(EntityTypePredicate.of(p_204078_));
            return this;
        }

        public EntityPredicate.Builder entityType(EntityTypePredicate p_36647_) {
            this.entityType = Optional.of(p_36647_);
            return this;
        }

        public EntityPredicate.Builder distance(DistancePredicate p_36639_) {
            this.distanceToPlayer = Optional.of(p_36639_);
            return this;
        }

        public EntityPredicate.Builder located(LocationPredicate.Builder p_298230_) {
            this.location = Optional.of(p_298230_.build());
            return this;
        }

        public EntityPredicate.Builder steppingOn(LocationPredicate.Builder p_298232_) {
            this.steppingOnLocation = Optional.of(p_298232_.build());
            return this;
        }

        public EntityPredicate.Builder effects(MobEffectsPredicate.Builder p_298981_) {
            this.effects = p_298981_.build();
            return this;
        }

        public EntityPredicate.Builder nbt(NbtPredicate p_36655_) {
            this.nbt = Optional.of(p_36655_);
            return this;
        }

        public EntityPredicate.Builder flags(EntityFlagsPredicate.Builder p_298456_) {
            this.flags = Optional.of(p_298456_.build());
            return this;
        }

        public EntityPredicate.Builder equipment(EntityEquipmentPredicate.Builder p_299134_) {
            this.equipment = Optional.of(p_299134_.build());
            return this;
        }

        public EntityPredicate.Builder equipment(EntityEquipmentPredicate p_36641_) {
            this.equipment = Optional.of(p_36641_);
            return this;
        }

        public EntityPredicate.Builder subPredicate(EntitySubPredicate p_218801_) {
            this.subPredicate = Optional.of(p_218801_);
            return this;
        }

        public EntityPredicate.Builder vehicle(EntityPredicate.Builder p_299103_) {
            this.vehicle = Optional.of(p_299103_.build());
            return this;
        }

        public EntityPredicate.Builder passenger(EntityPredicate.Builder p_298577_) {
            this.passenger = Optional.of(p_298577_.build());
            return this;
        }

        public EntityPredicate.Builder targetedEntity(EntityPredicate.Builder p_299292_) {
            this.targetedEntity = Optional.of(p_299292_.build());
            return this;
        }

        public EntityPredicate.Builder team(String p_36659_) {
            this.team = Optional.of(p_36659_);
            return this;
        }

        public EntityPredicate build() {
            return new EntityPredicate(
                this.entityType,
                this.distanceToPlayer,
                this.location,
                this.steppingOnLocation,
                this.effects,
                this.nbt,
                this.flags,
                this.equipment,
                this.subPredicate,
                this.vehicle,
                this.passenger,
                this.targetedEntity,
                this.team
            );
        }
    }
}
