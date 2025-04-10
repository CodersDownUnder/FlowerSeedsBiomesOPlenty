package net.minecraft.world.entity.animal.goat;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class Goat extends Animal {
    public static final EntityDimensions LONG_JUMPING_DIMENSIONS = EntityDimensions.scalable(0.9F, 1.3F).scale(0.7F);
    private static final int ADULT_ATTACK_DAMAGE = 2;
    private static final int BABY_ATTACK_DAMAGE = 1;
    protected static final ImmutableList<SensorType<? extends Sensor<? super Goat>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES,
        SensorType.NEAREST_PLAYERS,
        SensorType.NEAREST_ITEMS,
        SensorType.NEAREST_ADULT,
        SensorType.HURT_BY,
        SensorType.GOAT_TEMPTATIONS
    );
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.PATH,
        MemoryModuleType.ATE_RECENTLY,
        MemoryModuleType.BREED_TARGET,
        MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS,
        MemoryModuleType.LONG_JUMP_MID_JUMP,
        MemoryModuleType.TEMPTING_PLAYER,
        MemoryModuleType.NEAREST_VISIBLE_ADULT,
        MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
        MemoryModuleType.IS_TEMPTED,
        MemoryModuleType.RAM_COOLDOWN_TICKS,
        MemoryModuleType.RAM_TARGET,
        MemoryModuleType.IS_PANICKING
    );
    public static final int GOAT_FALL_DAMAGE_REDUCTION = 10;
    public static final double GOAT_SCREAMING_CHANCE = 0.02;
    public static final double UNIHORN_CHANCE = 0.1F;
    private static final EntityDataAccessor<Boolean> DATA_IS_SCREAMING_GOAT = SynchedEntityData.defineId(Goat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HAS_LEFT_HORN = SynchedEntityData.defineId(Goat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HAS_RIGHT_HORN = SynchedEntityData.defineId(Goat.class, EntityDataSerializers.BOOLEAN);
    private boolean isLoweringHead;
    private int lowerHeadTick;

    public Goat(EntityType<? extends Goat> p_149352_, Level p_149353_) {
        super(p_149352_, p_149353_);
        this.getNavigation().setCanFloat(true);
        this.setPathfindingMalus(BlockPathTypes.POWDER_SNOW, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_POWDER_SNOW, -1.0F);
    }

    public ItemStack createHorn() {
        RandomSource randomsource = RandomSource.create((long)this.getUUID().hashCode());
        TagKey<Instrument> tagkey = this.isScreamingGoat() ? InstrumentTags.SCREAMING_GOAT_HORNS : InstrumentTags.REGULAR_GOAT_HORNS;
        HolderSet<Instrument> holderset = BuiltInRegistries.INSTRUMENT.getOrCreateTag(tagkey);
        return InstrumentItem.create(Items.GOAT_HORN, holderset.getRandomElement(randomsource).get());
    }

    @Override
    protected Brain.Provider<Goat> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> p_149371_) {
        return GoatAi.makeBrain(this.brainProvider().makeBrain(p_149371_));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.2F).add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void ageBoundaryReached() {
        if (this.isBaby()) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(1.0);
            this.removeHorns();
        } else {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.0);
            this.addHorns();
        }
    }

    @Override
    protected int calculateFallDamage(float p_149389_, float p_149390_) {
        return super.calculateFallDamage(p_149389_, p_149390_) - 10;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_AMBIENT : SoundEvents.GOAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_149387_) {
        return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_HURT : SoundEvents.GOAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_DEATH : SoundEvents.GOAT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos p_149382_, BlockState p_149383_) {
        this.playSound(SoundEvents.GOAT_STEP, 0.15F, 1.0F);
    }

    protected SoundEvent getMilkingSound() {
        return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_MILK : SoundEvents.GOAT_MILK;
    }

    @Nullable
    public Goat getBreedOffspring(ServerLevel p_149376_, AgeableMob p_149377_) {
        Goat goat = EntityType.GOAT.create(p_149376_);
        if (goat != null) {
            GoatAi.initMemories(goat, p_149376_.getRandom());
            AgeableMob ageablemob = (AgeableMob)(p_149376_.getRandom().nextBoolean() ? this : p_149377_);
            boolean flag = ageablemob instanceof Goat goat1 && goat1.isScreamingGoat() || p_149376_.getRandom().nextDouble() < 0.02;
            goat.setScreamingGoat(flag);
        }

        return goat;
    }

    @Override
    public Brain<Goat> getBrain() {
        return (Brain<Goat>)super.getBrain();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("goatBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        this.level().getProfiler().pop();
        this.level().getProfiler().push("goatActivityUpdate");
        GoatAi.updateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public int getMaxHeadYRot() {
        return 15;
    }

    @Override
    public void setYHeadRot(float p_149400_) {
        int i = this.getMaxHeadYRot();
        float f = Mth.degreesDifference(this.yBodyRot, p_149400_);
        float f1 = Mth.clamp(f, (float)(-i), (float)i);
        super.setYHeadRot(this.yBodyRot + f1);
    }

    @Override
    public SoundEvent getEatingSound(ItemStack p_149394_) {
        return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_EAT : SoundEvents.GOAT_EAT;
    }

    @Override
    public InteractionResult mobInteract(Player p_149379_, InteractionHand p_149380_) {
        ItemStack itemstack = p_149379_.getItemInHand(p_149380_);
        if (itemstack.is(Items.BUCKET) && !this.isBaby()) {
            p_149379_.playSound(this.getMilkingSound(), 1.0F, 1.0F);
            ItemStack itemstack1 = ItemUtils.createFilledResult(itemstack, p_149379_, Items.MILK_BUCKET.getDefaultInstance());
            p_149379_.setItemInHand(p_149380_, itemstack1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            InteractionResult interactionresult = super.mobInteract(p_149379_, p_149380_);
            if (interactionresult.consumesAction() && this.isFood(itemstack)) {
                this.level()
                    .playSound(null, this, this.getEatingSound(itemstack), SoundSource.NEUTRAL, 1.0F, Mth.randomBetween(this.level().random, 0.8F, 1.2F));
            }

            return interactionresult;
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor p_149365_,
        DifficultyInstance p_149366_,
        MobSpawnType p_149367_,
        @Nullable SpawnGroupData p_149368_,
        @Nullable CompoundTag p_149369_
    ) {
        RandomSource randomsource = p_149365_.getRandom();
        GoatAi.initMemories(this, randomsource);
        this.setScreamingGoat(randomsource.nextDouble() < 0.02);
        this.ageBoundaryReached();
        if (!this.isBaby() && (double)randomsource.nextFloat() < 0.1F) {
            EntityDataAccessor<Boolean> entitydataaccessor = randomsource.nextBoolean() ? DATA_HAS_LEFT_HORN : DATA_HAS_RIGHT_HORN;
            this.entityData.set(entitydataaccessor, false);
        }

        return super.finalizeSpawn(p_149365_, p_149366_, p_149367_, p_149368_, p_149369_);
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    public EntityDimensions getDimensions(Pose p_149361_) {
        return p_149361_ == Pose.LONG_JUMPING ? LONG_JUMPING_DIMENSIONS.scale(this.getScale()) : super.getDimensions(p_149361_);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_149385_) {
        super.addAdditionalSaveData(p_149385_);
        p_149385_.putBoolean("IsScreamingGoat", this.isScreamingGoat());
        p_149385_.putBoolean("HasLeftHorn", this.hasLeftHorn());
        p_149385_.putBoolean("HasRightHorn", this.hasRightHorn());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_149373_) {
        super.readAdditionalSaveData(p_149373_);
        this.setScreamingGoat(p_149373_.getBoolean("IsScreamingGoat"));
        this.entityData.set(DATA_HAS_LEFT_HORN, p_149373_.getBoolean("HasLeftHorn"));
        this.entityData.set(DATA_HAS_RIGHT_HORN, p_149373_.getBoolean("HasRightHorn"));
    }

    @Override
    public void handleEntityEvent(byte p_149356_) {
        if (p_149356_ == 58) {
            this.isLoweringHead = true;
        } else if (p_149356_ == 59) {
            this.isLoweringHead = false;
        } else {
            super.handleEntityEvent(p_149356_);
        }
    }

    @Override
    public void aiStep() {
        if (this.isLoweringHead) {
            ++this.lowerHeadTick;
        } else {
            this.lowerHeadTick -= 2;
        }

        this.lowerHeadTick = Mth.clamp(this.lowerHeadTick, 0, 20);
        super.aiStep();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_SCREAMING_GOAT, false);
        this.entityData.define(DATA_HAS_LEFT_HORN, true);
        this.entityData.define(DATA_HAS_RIGHT_HORN, true);
    }

    public boolean hasLeftHorn() {
        return this.entityData.get(DATA_HAS_LEFT_HORN);
    }

    public boolean hasRightHorn() {
        return this.entityData.get(DATA_HAS_RIGHT_HORN);
    }

    public boolean dropHorn() {
        boolean flag = this.hasLeftHorn();
        boolean flag1 = this.hasRightHorn();
        if (!flag && !flag1) {
            return false;
        } else {
            EntityDataAccessor<Boolean> entitydataaccessor;
            if (!flag) {
                entitydataaccessor = DATA_HAS_RIGHT_HORN;
            } else if (!flag1) {
                entitydataaccessor = DATA_HAS_LEFT_HORN;
            } else {
                entitydataaccessor = this.random.nextBoolean() ? DATA_HAS_LEFT_HORN : DATA_HAS_RIGHT_HORN;
            }

            this.entityData.set(entitydataaccessor, false);
            Vec3 vec3 = this.position();
            ItemStack itemstack = this.createHorn();
            double d0 = (double)Mth.randomBetween(this.random, -0.2F, 0.2F);
            double d1 = (double)Mth.randomBetween(this.random, 0.3F, 0.7F);
            double d2 = (double)Mth.randomBetween(this.random, -0.2F, 0.2F);
            ItemEntity itementity = new ItemEntity(this.level(), vec3.x(), vec3.y(), vec3.z(), itemstack, d0, d1, d2);
            this.level().addFreshEntity(itementity);
            return true;
        }
    }

    public void addHorns() {
        this.entityData.set(DATA_HAS_LEFT_HORN, true);
        this.entityData.set(DATA_HAS_RIGHT_HORN, true);
    }

    public void removeHorns() {
        this.entityData.set(DATA_HAS_LEFT_HORN, false);
        this.entityData.set(DATA_HAS_RIGHT_HORN, false);
    }

    public boolean isScreamingGoat() {
        return this.entityData.get(DATA_IS_SCREAMING_GOAT);
    }

    public void setScreamingGoat(boolean p_149406_) {
        this.entityData.set(DATA_IS_SCREAMING_GOAT, p_149406_);
    }

    public float getRammingXHeadRot() {
        return (float)this.lowerHeadTick / 20.0F * 30.0F * (float) (Math.PI / 180.0);
    }

    public static boolean checkGoatSpawnRules(
        EntityType<? extends Animal> p_218753_, LevelAccessor p_218754_, MobSpawnType p_218755_, BlockPos p_218756_, RandomSource p_218757_
    ) {
        return p_218754_.getBlockState(p_218756_.below()).is(BlockTags.GOATS_SPAWNABLE_ON) && isBrightEnoughToSpawn(p_218754_, p_218756_);
    }

    @Override
    protected Vector3f getPassengerAttachmentPoint(Entity p_294528_, EntityDimensions p_295589_, float p_295312_) {
        return new Vector3f(0.0F, p_295589_.height - 0.1875F * p_295312_, 0.0F);
    }
}
