package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BoatModel extends ListModel<Boat> implements WaterPatchModel {
    private static final String LEFT_PADDLE = "left_paddle";
    private static final String RIGHT_PADDLE = "right_paddle";
    private static final String WATER_PATCH = "water_patch";
    private static final String BOTTOM = "bottom";
    private static final String BACK = "back";
    private static final String FRONT = "front";
    private static final String RIGHT = "right";
    private static final String LEFT = "left";
    private final ModelPart leftPaddle;
    private final ModelPart rightPaddle;
    private final ModelPart waterPatch;
    private final ImmutableList<ModelPart> parts;

    public BoatModel(ModelPart p_250599_) {
        this.leftPaddle = p_250599_.getChild("left_paddle");
        this.rightPaddle = p_250599_.getChild("right_paddle");
        this.waterPatch = p_250599_.getChild("water_patch");
        this.parts = this.createPartsBuilder(p_250599_).build();
    }

    protected Builder<ModelPart> createPartsBuilder(ModelPart p_252283_) {
        Builder<ModelPart> builder = new Builder<>();
        builder.add(
            p_252283_.getChild("bottom"),
            p_252283_.getChild("back"),
            p_252283_.getChild("front"),
            p_252283_.getChild("right"),
            p_252283_.getChild("left"),
            this.leftPaddle,
            this.rightPaddle
        );
        return builder;
    }

    public static void createChildren(PartDefinition p_250572_) {
        int i = 32;
        int j = 6;
        int k = 20;
        int l = 4;
        int i1 = 28;
        p_250572_.addOrReplaceChild(
            "bottom",
            CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F),
            PartPose.offsetAndRotation(0.0F, 3.0F, 1.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        p_250572_.addOrReplaceChild(
            "back",
            CubeListBuilder.create().texOffs(0, 19).addBox(-13.0F, -7.0F, -1.0F, 18.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(-15.0F, 4.0F, 4.0F, 0.0F, (float) (Math.PI * 3.0 / 2.0), 0.0F)
        );
        p_250572_.addOrReplaceChild(
            "front",
            CubeListBuilder.create().texOffs(0, 27).addBox(-8.0F, -7.0F, -1.0F, 16.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(15.0F, 4.0F, 0.0F, 0.0F, (float) (Math.PI / 2), 0.0F)
        );
        p_250572_.addOrReplaceChild(
            "right",
            CubeListBuilder.create().texOffs(0, 35).addBox(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, 4.0F, -9.0F, 0.0F, (float) Math.PI, 0.0F)
        );
        p_250572_.addOrReplaceChild(
            "left", CubeListBuilder.create().texOffs(0, 43).addBox(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F), PartPose.offset(0.0F, 4.0F, 9.0F)
        );
        int j1 = 20;
        int k1 = 7;
        int l1 = 6;
        float f = -5.0F;
        p_250572_.addOrReplaceChild(
            "left_paddle",
            CubeListBuilder.create().texOffs(62, 0).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).addBox(-1.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F),
            PartPose.offsetAndRotation(3.0F, -5.0F, 9.0F, 0.0F, 0.0F, (float) (Math.PI / 16))
        );
        p_250572_.addOrReplaceChild(
            "right_paddle",
            CubeListBuilder.create().texOffs(62, 20).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).addBox(0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F),
            PartPose.offsetAndRotation(3.0F, -5.0F, -9.0F, 0.0F, (float) Math.PI, (float) (Math.PI / 16))
        );
        p_250572_.addOrReplaceChild(
            "water_patch",
            CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F),
            PartPose.offsetAndRotation(0.0F, -3.0F, 1.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
    }

    public static LayerDefinition createBodyModel() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        createChildren(partdefinition);
        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    public void setupAnim(Boat p_102269_, float p_102270_, float p_102271_, float p_102272_, float p_102273_, float p_102274_) {
        animatePaddle(p_102269_, 0, this.leftPaddle, p_102270_);
        animatePaddle(p_102269_, 1, this.rightPaddle, p_102270_);
    }

    public ImmutableList<ModelPart> parts() {
        return this.parts;
    }

    @Override
    public ModelPart waterPatch() {
        return this.waterPatch;
    }

    private static void animatePaddle(Boat p_170465_, int p_170466_, ModelPart p_170467_, float p_170468_) {
        float f = p_170465_.getRowingTime(p_170466_, p_170468_);
        p_170467_.xRot = Mth.clampedLerp((float) (-Math.PI / 3), (float) (-Math.PI / 12), (Mth.sin(-f) + 1.0F) / 2.0F);
        p_170467_.yRot = Mth.clampedLerp((float) (-Math.PI / 4), (float) (Math.PI / 4), (Mth.sin(-f + 1.0F) + 1.0F) / 2.0F);
        if (p_170466_ == 1) {
            p_170467_.yRot = (float) Math.PI - p_170467_.yRot;
        }
    }
}
