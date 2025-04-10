package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EnchantmentTableBlock extends BaseEntityBlock {
    public static final MapCodec<EnchantmentTableBlock> CODEC = simpleCodec(EnchantmentTableBlock::new);
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
    public static final List<BlockPos> BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-2, 0, -2, 2, 1, 2)
        .filter(p_207914_ -> Math.abs(p_207914_.getX()) == 2 || Math.abs(p_207914_.getZ()) == 2)
        .map(BlockPos::immutable)
        .toList();

    @Override
    public MapCodec<EnchantmentTableBlock> codec() {
        return CODEC;
    }

    public EnchantmentTableBlock(BlockBehaviour.Properties p_52953_) {
        super(p_52953_);
    }

    public static boolean isValidBookShelf(Level p_207910_, BlockPos p_207911_, BlockPos p_207912_) {
        return p_207910_.getBlockState(p_207911_.offset(p_207912_)).getEnchantPowerBonus(p_207910_, p_207911_.offset(p_207912_)) != 0
            && p_207910_.getBlockState(p_207911_.offset(p_207912_.getX() / 2, p_207912_.getY(), p_207912_.getZ() / 2))
                .is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState p_52997_) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState p_52988_, BlockGetter p_52989_, BlockPos p_52990_, CollisionContext p_52991_) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState p_221092_, Level p_221093_, BlockPos p_221094_, RandomSource p_221095_) {
        super.animateTick(p_221092_, p_221093_, p_221094_, p_221095_);

        for(BlockPos blockpos : BOOKSHELF_OFFSETS) {
            if (p_221095_.nextInt(16) == 0 && isValidBookShelf(p_221093_, p_221094_, blockpos)) {
                p_221093_.addParticle(
                    ParticleTypes.ENCHANT,
                    (double)p_221094_.getX() + 0.5,
                    (double)p_221094_.getY() + 2.0,
                    (double)p_221094_.getZ() + 0.5,
                    (double)((float)blockpos.getX() + p_221095_.nextFloat()) - 0.5,
                    (double)((float)blockpos.getY() - p_221095_.nextFloat() - 1.0F),
                    (double)((float)blockpos.getZ() + p_221095_.nextFloat()) - 0.5
                );
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState p_52986_) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos p_153186_, BlockState p_153187_) {
        return new EnchantmentTableBlockEntity(p_153186_, p_153187_);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153182_, BlockState p_153183_, BlockEntityType<T> p_153184_) {
        return p_153182_.isClientSide ? createTickerHelper(p_153184_, BlockEntityType.ENCHANTING_TABLE, EnchantmentTableBlockEntity::bookAnimationTick) : null;
    }

    @Override
    public InteractionResult use(BlockState p_52974_, Level p_52975_, BlockPos p_52976_, Player p_52977_, InteractionHand p_52978_, BlockHitResult p_52979_) {
        if (p_52975_.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            p_52977_.openMenu(p_52974_.getMenuProvider(p_52975_, p_52976_));
            return InteractionResult.CONSUME;
        }
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState p_52993_, Level p_52994_, BlockPos p_52995_) {
        BlockEntity blockentity = p_52994_.getBlockEntity(p_52995_);
        if (blockentity instanceof EnchantmentTableBlockEntity) {
            Component component = ((Nameable)blockentity).getDisplayName();
            return new SimpleMenuProvider(
                (p_207906_, p_207907_, p_207908_) -> new EnchantmentMenu(p_207906_, p_207907_, ContainerLevelAccess.create(p_52994_, p_52995_)), component
            );
        } else {
            return null;
        }
    }

    @Override
    public void setPlacedBy(Level p_52963_, BlockPos p_52964_, BlockState p_52965_, LivingEntity p_52966_, ItemStack p_52967_) {
        if (p_52967_.hasCustomHoverName()) {
            BlockEntity blockentity = p_52963_.getBlockEntity(p_52964_);
            if (blockentity instanceof EnchantmentTableBlockEntity) {
                ((EnchantmentTableBlockEntity)blockentity).setCustomName(p_52967_.getHoverName());
            }
        }
    }

    @Override
    public boolean isPathfindable(BlockState p_52969_, BlockGetter p_52970_, BlockPos p_52971_, PathComputationType p_52972_) {
        return false;
    }
}
