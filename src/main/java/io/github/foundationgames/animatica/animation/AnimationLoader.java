package io.github.foundationgames.animatica.animation;

import io.github.foundationgames.animatica.Animatica;
//? if <26.4 {
import io.github.foundationgames.animatica.compat.IrisCompat;
//?}
import io.github.foundationgames.animatica.util.Flags;
import io.github.foundationgames.animatica.util.exception.PropertyParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

public final class AnimationLoader implements ResourceManagerReloadListener {
    public static final String[] ANIM_PATHS = {
            "animatica/anim",
            "mcpatcher/anim",
            "optifine/anim"
    };
    public static final Identifier ID = Animatica.id("animation_storage");

    public static final AnimationLoader INSTANCE = new AnimationLoader();

    private final Map<Identifier, Identifier> animationIds = new HashMap<>();
    private final Map<Identifier, AnimatedTexture> animatedTextures = new HashMap<>();

    private AnimationLoader() {
    }

    private static void findAllMCPAnimations(ResourceManager manager, BiConsumer<Identifier, Resource> action) {
        for (var path : ANIM_PATHS) {
            manager.listResources(path, p -> p.getPath().endsWith(".properties")).forEach(action);
        }
    }

    public @Nullable Identifier getAnimationId(Identifier id) {
        return animationIds.get(id);
    }

    public void markDrawn(Identifier animId) {
        var texture = this.animatedTextures.get(animId);
        if (texture != null && !isLoadingOverlayShown()) {
            texture.markDrawn();
        }
    }

    static boolean isLoadingOverlayShown() {
        //? if >=26.2 {
        return Minecraft.getInstance().gui.overlay() != null;
        //?} else {
        /*return Minecraft.getInstance().getOverlay() != null;
        *///?}
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        this.close();

        if (!Animatica.CONFIG.animatedTextures) {
            return;
        }

        Flags.ALLOW_INVALID_ID_CHARS = true;

        var animations = new HashMap<Identifier, List<AnimationMeta>>();

        findAllMCPAnimations(manager, (id, resource) -> {
            try {
                try (var resourceInputStream = resource.open()) {
                    var ppt = new Properties();
                    ppt.load(resourceInputStream);

                    var anim = AnimationMeta.of(id, ppt);

                    var targetId = anim.target();
                    if (!animations.containsKey(targetId)) animations.put(targetId, new ArrayList<>());
                    animations.get(targetId).add(anim);
                }
            } catch (IOException | PropertyParseException e) {
                Animatica.LOG.error(e.getMessage());
            }
        });

        for (var targetId : animations.keySet()) {
            AnimatedTexture.tryCreate(manager, targetId, animations.get(targetId))
                    .ifPresent(tex -> {
                        var animId = Identifier.fromNamespaceAndPath(targetId.getNamespace(), targetId.getPath() + "-anim");
                        this.animationIds.put(targetId, animId);
                        this.animatedTextures.put(animId, tex);
                        //? if >=26.4 {
                        /*Minecraft.getInstance().getTextureManager().register(animId, tex.textureResources);
                        *///?} else {
                        Minecraft.getInstance().getTextureManager().register(animId, tex);

                        if (Animatica.IRIS_COMPAT) {
                            IrisCompat.trackTexture(tex);
                        }
                        //?}
                    });
        }

        Flags.ALLOW_INVALID_ID_CHARS = false;
    }

    //? if >=26.4 {
    /*
    public void tick() {
        this.animatedTextures.values().forEach(AnimatedTexture::tick);
    }
    *///?}

    // Closes every animated texture
    public void close() {
        var textureManager = Minecraft.getInstance().getTextureManager();
        this.animatedTextures.forEach((id, texture) -> {
            texture.close();
            textureManager.release(id);
        });
        this.animatedTextures.clear();
        this.animationIds.clear();
    }
}
