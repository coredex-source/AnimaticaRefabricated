//? if <26.4 {
package io.github.foundationgames.animatica.compat;

//? if >=26.3 {
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.opengl.GlTexture;
//?} else {
/*import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
*///?}
import io.github.foundationgames.animatica.animation.AnimatedTexture;
import io.github.foundationgames.animatica.animation.AnimationLoader;
import net.irisshaders.iris.pbr.TextureTracker;
import net.irisshaders.iris.pbr.loader.PBRTextureLoader;
import net.irisshaders.iris.pbr.loader.PBRTextureLoaderRegistry;
import net.irisshaders.iris.pbr.loader.SimplePBRLoader;
import net.irisshaders.iris.pbr.texture.PBRType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

public enum IrisCompat {;
    public static void init() {
        PBRTextureLoaderRegistry.INSTANCE.register(AnimatedTexture.class, new AnimatedTexturePBRLoader()::loadAnimated);
    }

    public static void trackTexture(AnimatedTexture texture) {
        if (texture.getTexture() instanceof GlTexture glTexture) {
            TextureTracker.INSTANCE.trackTexture(glTexture.glId(), texture);
        }
    }

    private static class SharedTexture extends AbstractTexture {
        private final AbstractTexture source;

        private SharedTexture(AbstractTexture source) {
            this.source = source;
        }

        @Override
        public GpuTexture getTexture() {
            return source.getTexture();
        }

        @Override
        public GpuTextureView getTextureView() {
            return source.getTextureView();
        }

        @Override
        public GpuSampler getSampler() {
            return source.getSampler();
        }

        @Override
        public void close() {
        }
    }

    private static class AnimatedTexturePBRLoader extends SimplePBRLoader {
        public void loadAnimated(AnimatedTexture texture, ResourceManager resources, PBRTextureLoader.PBRTextureConsumer consumer) {
            var normal = createPBRTexture(texture.targetId, resources, PBRType.NORMAL);
            if (normal != null) {
                consumer.acceptNormalTexture(normal);
            }

            var specular = createPBRTexture(texture.targetId, resources, PBRType.SPECULAR);
            if (specular != null) {
                consumer.acceptSpecularTexture(specular);
            }
        }

        @Override
        protected @Nullable AbstractTexture createPBRTexture(Identifier targetId, ResourceManager resources, PBRType type) {
            var animId = AnimationLoader.INSTANCE.getAnimationId(targetId.withPath(type::appendSuffix));
            if (animId != null) {
                return new SharedTexture(Minecraft.getInstance().getTextureManager().getTexture(animId));
            }

            return super.createPBRTexture(targetId, resources, type);
        }
    }
}
//?}
