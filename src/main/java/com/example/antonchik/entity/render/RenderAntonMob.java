package com.example.antonchik.entity.render;

import com.example.antonchik.Antonchik;
import com.example.antonchik.entity.EntityAntonMob;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Renders {@link EntityAntonMob} from a custom Wavefront OBJ ({@link ObjModel}), binding the
 * per-material diffuse texture declared in the model's {@code .mtl} ({@link MtlLib}).
 */
public class RenderAntonMob extends Render<EntityAntonMob>
{
    private static final ResourceLocation MODEL =
        new ResourceLocation(Antonchik.MODID, "models/entity/antonchik.obj");
    private static final ResourceLocation MATERIALS =
        new ResourceLocation(Antonchik.MODID, "textures/entity/anton/untitled.mtl");
    /** Base path the .mtl's diffuse filenames are resolved against. */
    private static final String TEXTURE_PATH = "textures/entity/anton/";
    /** Fallback texture when a material has no diffuse map. */
    private static final ResourceLocation FALLBACK_TEXTURE =
        new ResourceLocation(Antonchik.MODID, TEXTURE_PATH + "remy_body_diffuse.png");

    /**
     * Uniform scale applied to the model. The source OBJ is ~5.97 units tall with its base at
     * y=0, so 1.8 / 5.97 ≈ 0.30 fits the entity's 1.8-high bounding box. Tweak to taste.
     */
    private static final float MODEL_SCALE = 0.30F;

    private ObjModel model;
    private MtlLib materials;
    private boolean loadFailed;

    public RenderAntonMob(RenderManager manager)
    {
        super(manager);
    }

    @Override
    public void doRender(EntityAntonMob entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        if (!ensureLoaded())
        {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        // Face the model in the entity's body direction (this model is authored facing north/-Z).
        float bodyYaw = interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
        GlStateManager.rotate(-bodyYaw, 0.0F, 1.0F, 0.0F);

        applyWalkAnimation(entity, partialTicks);

        GlStateManager.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        // OBJ exports often have inconsistent winding; draw both sides so nothing is culled away.
        GlStateManager.disableCull();

        model.render(this::bindMaterial);

        GlStateManager.enableCull();
        GlStateManager.disableRescaleNormal();

        GlStateManager.popMatrix();

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    /**
     * Procedural walk: since the OBJ is a single static mesh with no skeleton, the whole body
     * bobs up/down and rocks forward/back in time with the entity's stride ({@code limbSwing}),
     * scaled by how fast it is moving ({@code limbSwingAmount}). Pivots at the feet (model y=0).
     */
    private void applyWalkAnimation(EntityAntonMob entity, float partialTicks)
    {
        float amount = entity.prevLimbSwingAmount
            + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
        if (amount <= 0.0F)
        {
            return;
        }
        amount = Math.min(amount, 1.0F);

        float stride = (entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks)) * 0.6662F;
        float bob = MathHelper.abs(MathHelper.cos(stride)) * amount * 0.12F;
        float rock = MathHelper.cos(stride) * amount * 5.0F;

        GlStateManager.translate(0.0F, bob, 0.0F);
        GlStateManager.rotate(rock, 1.0F, 0.0F, 0.0F);
    }

    private void bindMaterial(String material)
    {
        ResourceLocation texture = materials.getDiffuse(material);
        bindTexture(texture != null ? texture : FALLBACK_TEXTURE);
    }

    private boolean ensureLoaded()
    {
        if (model != null)
        {
            return true;
        }
        if (loadFailed)
        {
            return false;
        }
        try
        {
            materials = MtlLib.load(MATERIALS, Antonchik.MODID, TEXTURE_PATH);
            model = ObjModel.load(MODEL);
            System.out.println("[Antonchik] Loaded entity OBJ model: " + model.summary());
            return true;
        }
        catch (IOException e)
        {
            loadFailed = true;
            System.err.println("[Antonchik] Failed to load entity OBJ model: " + e.getMessage());
            return false;
        }
    }

    private static float interpolateRotation(float prev, float current, float partialTicks)
    {
        float delta = MathHelper.wrapDegrees(current - prev);
        return prev + partialTicks * delta;
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityAntonMob entity)
    {
        return FALLBACK_TEXTURE;
    }
}
