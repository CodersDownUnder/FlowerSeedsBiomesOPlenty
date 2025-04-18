package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public abstract class DustParticleOptionsBase implements ParticleOptions {
    public static final float MIN_SCALE = 0.01F;
    public static final float MAX_SCALE = 4.0F;
    protected final Vector3f color;
    protected final float scale;

    public DustParticleOptionsBase(Vector3f p_253672_, float p_253735_) {
        this.color = p_253672_;
        this.scale = Mth.clamp(p_253735_, 0.01F, 4.0F);
    }

    public static Vector3f readVector3f(StringReader p_254560_) throws CommandSyntaxException {
        p_254560_.expect(' ');
        float f = p_254560_.readFloat();
        p_254560_.expect(' ');
        float f1 = p_254560_.readFloat();
        p_254560_.expect(' ');
        float f2 = p_254560_.readFloat();
        return new Vector3f(f, f1, f2);
    }

    public static Vector3f readVector3f(FriendlyByteBuf p_254279_) {
        return new Vector3f(p_254279_.readFloat(), p_254279_.readFloat(), p_254279_.readFloat());
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf p_175809_) {
        p_175809_.writeFloat(this.color.x());
        p_175809_.writeFloat(this.color.y());
        p_175809_.writeFloat(this.color.z());
        p_175809_.writeFloat(this.scale);
    }

    @Override
    public String writeToString() {
        return String.format(
            Locale.ROOT,
            "%s %.2f %.2f %.2f %.2f",
            BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()),
            this.color.x(),
            this.color.y(),
            this.color.z(),
            this.scale
        );
    }

    public Vector3f getColor() {
        return this.color;
    }

    public float getScale() {
        return this.scale;
    }
}
