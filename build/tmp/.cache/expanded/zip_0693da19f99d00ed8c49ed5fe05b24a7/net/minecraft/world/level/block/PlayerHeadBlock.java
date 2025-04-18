package net.minecraft.world.level.block;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerHeadBlock extends SkullBlock {
    public static final MapCodec<PlayerHeadBlock> CODEC = simpleCodec(PlayerHeadBlock::new);

    @Override
    public MapCodec<PlayerHeadBlock> codec() {
        return CODEC;
    }

    public PlayerHeadBlock(BlockBehaviour.Properties p_55177_) {
        super(SkullBlock.Types.PLAYER, p_55177_);
    }

    @Override
    public void setPlacedBy(Level p_55179_, BlockPos p_55180_, BlockState p_55181_, @Nullable LivingEntity p_55182_, ItemStack p_55183_) {
        super.setPlacedBy(p_55179_, p_55180_, p_55181_, p_55182_, p_55183_);
        BlockEntity blockentity = p_55179_.getBlockEntity(p_55180_);
        if (blockentity instanceof SkullBlockEntity skullblockentity) {
            GameProfile gameprofile = null;
            if (p_55183_.hasTag()) {
                CompoundTag compoundtag = p_55183_.getTag();
                if (compoundtag.contains("SkullOwner", 10)) {
                    gameprofile = NbtUtils.readGameProfile(compoundtag.getCompound("SkullOwner"));
                } else if (compoundtag.contains("SkullOwner", 8) && !Util.isBlank(compoundtag.getString("SkullOwner"))) {
                    gameprofile = new GameProfile(Util.NIL_UUID, compoundtag.getString("SkullOwner"));
                }
            }

            skullblockentity.setOwner(gameprofile);
        }
    }
}
