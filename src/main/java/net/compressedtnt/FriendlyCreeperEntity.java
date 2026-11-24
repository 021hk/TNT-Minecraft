package net.compressedtnt;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

/**
 * The friendly "bow-tie" creepers that spawn in the day like cows and sheep.
 *
 * <ul>
 *  <li>pink bow = female, blue bow = male; they never attack, but if a
 *      player damages one (even a punch) it explodes after 1.5 s like a
 *      normal creeper;</li>
 *  <li>they walk towards players holding bone meal OR coal;</li>
 *  <li>feed a male + a female with bone meal or coal to breed -> a baby
 *      creeper that grows up over time like a baby sheep;</li>
 *  <li>feeding coal to a baby has a 50% chance to speed up its growth.</li>
 * </ul>
 */
public class FriendlyCreeperEntity extends AnimalEntity {
        public enum Variant {
                PINK, BLUE
        }

        private static final TrackedData<Boolean> PRIMED =
                        DataTracker.registerData(FriendlyCreeperEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        private static final int FUSE_LENGTH = 30;

        private Variant variant = Variant.PINK;
        private int fuseTicks = -1;

        public FriendlyCreeperEntity(EntityType<? extends FriendlyCreeperEntity> type, World world) {
                super(type, world);
                this.variant = resolve(type);
        }

        private static Variant resolve(EntityType<?> type) {
                return type == CompressedTnt.FRIENDLY_CREEPER_BLUE ? Variant.BLUE : Variant.PINK;
        }

        public Variant variant() {
                return this.variant;
        }

        public void setVariant(Variant variant) {
                this.variant = variant;
        }

        @Override
        protected void initDataTracker(DataTracker.Builder builder) {
                super.initDataTracker(builder);
                builder.add(PRIMED, false);
        }

        @Override
        protected void initGoals() {
                this.goalSelector.add(0, new SwimGoal(this));
                this.goalSelector.add(2, new TemptGoal(this, 1.15, Ingredient.ofItems(Items.BONE_MEAL, Items.COAL), false));
                this.goalSelector.add(3, new AnimalMateGoal(this, 1.0));
                this.goalSelector.add(4, new FollowParentGoal(this, 1.1));
                this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
                this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
                this.goalSelector.add(6, new LookAroundGoal(this));
        }

        public static DefaultAttributeContainer.Builder createFriendlyCreeperAttributes() {
                return MobEntity.createMobAttributes()
                                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
        }

        @Override
        public boolean isBreedingItem(ItemStack stack) {
                return stack.isOf(Items.BONE_MEAL) || stack.isOf(Items.COAL);
        }

        /** breeding only works male + female (pink + blue) */
        @Override
        public boolean canBreedWith(AnimalEntity other) {
                return other instanceof FriendlyCreeperEntity friend && friend.variant != this.variant;
        }

        @Override
        public PassiveEntity createChild(ServerWorld world, PassiveEntity other) {
                if (!(other instanceof FriendlyCreeperEntity)) {
                        return null;
                }
                Variant v = this.random.nextBoolean() ? Variant.PINK : Variant.BLUE;
                EntityType<FriendlyCreeperEntity> type = v == Variant.PINK
                                ? CompressedTnt.FRIENDLY_CREEPER_PINK : CompressedTnt.FRIENDLY_CREEPER_BLUE;
                FriendlyCreeperEntity baby = new FriendlyCreeperEntity(type, world);
                baby.setVariant(v);
                return baby;
        }

        @Override
        public ActionResult interactMob(PlayerEntity player, Hand hand) {
                ItemStack stack = player.getStackInHand(hand);
                // coal on a baby: 50% chance to speed up growth
                if (this.isBaby() && stack.isOf(Items.COAL) && !this.getWorld().isClient) {
                        if (this.random.nextFloat() < 0.5f) {
                                this.growUp((int) (-this.getBreedingAge() / 10));
                                this.eat(player, hand, stack);
                        } else {
                                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.7f, 1.4f);
                        }
                        return ActionResult.success(this.getWorld().isClient);
                }
                return super.interactMob(player, hand);
        }

        public boolean isPrimed() {
                return this.dataTracker.get(PRIMED);
        }

        /** 0..1 swell progress used by the renderer while the fuse burns. */
        public float getPrimedProgress(float tickDelta) {
                if (this.fuseTicks < 0) {
                        return 0.0f;
                }
                float prev = Math.min((this.fuseTicks + 1) / (float) FUSE_LENGTH, 1.0f);
                float now = Math.min(this.fuseTicks / (float) FUSE_LENGTH, 1.0f);
                return MathHelper.lerp(tickDelta, prev, now);
        }

        @Override
        public boolean damage(DamageSource source, float amount) {
                boolean hurt = super.damage(source, amount);
                if (hurt && !this.getWorld().isClient && !this.isPrimed() && !this.isBaby()
                                && source.getAttacker() instanceof PlayerEntity) {
                        this.prime();
                }
                return hurt;
        }

        private void prime() {
                this.fuseTicks = FUSE_LENGTH;
                this.dataTracker.set(PRIMED, true);
                this.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0f, 1.0f);
                this.emitGameEvent(GameEvent.PRIME_FUSE);
        }

        @Override
        public void tick() {
                super.tick();
                if (this.getWorld().isClient || this.fuseTicks <= 0) {
                        return;
                }
                this.fuseTicks--;
                if (this.getWorld() instanceof ServerWorld server && this.random.nextInt(2) == 0) {
                        server.spawnParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.6, this.getZ(),
                                        1, 0.05, 0.05, 0.05, 0.01);
                }
                if (this.fuseTicks <= 0) {
                        this.explode();
                }
        }

        private void explode() {
                this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), 3.0f, false,
                                World.ExplosionSourceType.MOB);
                this.discard();
        }

        @Override
        protected SoundEvent getAmbientSound() {
                return null;
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) {
                return SoundEvents.ENTITY_CREEPER_HURT;
        }

        @Override
        protected SoundEvent getDeathSound() {
                return SoundEvents.ENTITY_CREEPER_DEATH;
        }

        @Override
        public void writeCustomDataToNbt(NbtCompound nbt) {
                super.writeCustomDataToNbt(nbt);
                nbt.putString("FriendlyVariant", this.variant.name());
        }

        @Override
        public void readCustomDataFromNbt(NbtCompound nbt) {
                super.readCustomDataFromNbt(nbt);
                try {
                        this.variant = Variant.valueOf(nbt.getString("FriendlyVariant"));
                } catch (IllegalArgumentException e) {
                        this.variant = resolve(this.getType());
                }
        }
}
