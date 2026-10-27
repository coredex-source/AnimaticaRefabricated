package io.github.foundationgames.animatica.animation;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
//? if >=26.3 {
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
//?} else {
/*import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
*///?}
import io.github.foundationgames.animatica.Animatica;
import io.github.foundationgames.animatica.config.AnimaticaConfig.PlayOnceRestart;
import io.github.foundationgames.animatica.util.TextureUtil;
import io.github.foundationgames.animatica.util.Utilities;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureContents;
//? if >=26.4 {
/*import net.minecraft.client.renderer.texture.TextureResources;
*///?} else {
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TickableTexture;
//?}
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

//? if >=26.4 {
/*public class AnimatedTexture implements AutoCloseable {
*///?} else {
public class AnimatedTexture extends DynamicTexture implements TickableTexture {
//?}
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
    public static final ExecutorService EXECUTORS = Executors.newFixedThreadPool(4, daemonThreadFactory());
    private static final int IDLE_TICKS = 10;

    public final Identifier targetId;
    public final Animation[] anims;
    private final NativeImage original;
    //? if >=26.4 {
    /*public final TextureResources textureResources;
    private final NativeImage pixels;
    *///?}
    private boolean closed = false;
    private boolean dirty = false;
    private volatile boolean drawn = false;
    private int idleTicks = IDLE_TICKS;
    private int frame = 0;

    private CompletableFuture<Void> frameWaitingOn = null;

    public static Optional<AnimatedTexture> tryCreate(ResourceManager resources, Identifier targetTexId, List<AnimationMeta> anims) {
        try {
            return Optional.of(new AnimatedTexture(targetTexId, resources, anims, TextureContents.load(resources, targetTexId)));
        } catch (IOException e) { Animatica.LOG.error(e); }

        return Optional.empty();
    }

    public AnimatedTexture(Identifier targetId, ResourceManager resources, List<AnimationMeta> metas, TextureContents contents) throws IOException {
        //? if <26.4 {
        super(targetId::toString, contents.image().getWidth(), contents.image().getHeight(), true);
        //?}
        var addressMode = contents.clamp() ? AddressMode.CLAMP_TO_EDGE : AddressMode.REPEAT;
        var filterMode = contents.blur() ? FilterMode.LINEAR : FilterMode.NEAREST;
        var sampler = RenderSystem.getSamplerCache().getSampler(addressMode, addressMode, filterMode, filterMode, false);
        //? if >=26.4 {
        /*this.pixels = new NativeImage(contents.image().getWidth(), contents.image().getHeight(), true);
        this.textureResources = TextureResources.from2dImage(targetId::toString, contents.image(), sampler);
        *///?} else {
        this.sampler = sampler;
        //?}

        this.targetId = targetId;
        this.anims = new Animation[metas.size()];
        for (int i = 0; i < metas.size(); i++) {
            this.anims[i] = new Animation(metas.get(i), resources);
        }
        this.original = contents.image();

        updateAndDraw(this.getPixels(), true, Minecraft.getInstance());
        this.upload();
    }

    //? if >=26.4 {
    /*public NativeImage getPixels() {
        return this.pixels;
    }

    public synchronized void upload() {
        if (!this.closed && !this.textureResources.texture().isClosed()) {
            this.pixels.writeToGpuTexture(RenderSystem.getDevice().createCommandEncoder(), this.textureResources.texture());
        }
    }
    *///?} else {
    @Override
    public synchronized void upload() {
        // An upload can still be pending when the texture is closed
        if (!this.closed) {
            super.upload();
        }
    }
    //?}

    public void markDrawn() {
        this.drawn = true;
    }

    public boolean canLoop() {
        for (var anim : anims) {
            if (!anim.isOnFrameZero()) {
                return false;
            }
        }
        // All animations for this texture are at zero again, so the frame counter can be reset
        return true;
    }

    private @Nullable CompletableFuture<Void> getFrameWaitingOn() {
        if (this.frameWaitingOn != null && this.frameWaitingOn.isDone()) {
            this.frameWaitingOn = null;
        }

        return this.frameWaitingOn;
    }

    public synchronized void updateAndDraw(NativeImage image, boolean force, Executor exec) {
        if (this.closed) return;
        boolean changed = false;

        boolean restartWhenDrawn = Animatica.CONFIG.playOnceRestart == PlayOnceRestart.WHEN_DRAWN;
        if (this.drawn) {
            this.drawn = false;
            this.idleTicks = 0;
        } else if (this.idleTicks < IDLE_TICKS) {
            this.idleTicks++;
            if (this.idleTicks == IDLE_TICKS && restartWhenDrawn) {
                for (var anim : anims) {
                    if (!anim.loop) anim.rewind();
                }
            }
        }
        boolean playOnceRunning = restartWhenDrawn ? this.idleTicks < IDLE_TICKS : !AnimationLoader.isLoadingOverlayShown();

        if (canLoop()) {
            if (frame > 0) {
                frame = 0;
            }
        } else if (frame <= 0) {
            changed = true;
        }

        for (var anim : anims) {
            if (anim.isChanged()) {
                changed = true;
                break;
            }
        }

        if (changed || force) {
            this.dirty = true;
        }

        if (this.dirty) {
            if (this.getFrameWaitingOn() == null) {
                this.dirty = false;

                var phases = Arrays.stream(anims).map(Animation::getCurrentPhase).toList();
                var phaseFrames = Arrays.stream(anims).mapToInt(Animation::getPhaseFrame).toArray();

                this.frameWaitingOn = CompletableFuture.runAsync(() -> {
                    synchronized (this) {
                        if (this.closed) return;
                        image.copyFrom(this.original);

                        for (int i = 0; i < anims.length; i++) {
                            var anim = anims[i];
                            var phase = phases.get(i);
                            if (phase instanceof InterpolatedPhase iPhase) {
                                TextureUtil.blendCopy(anim.sourceTexture, 0, iPhase.prevV, 0, iPhase.v, anim.width, anim.height, image, anim.targetX, anim.targetY, iPhase.blend.getBlend(phaseFrames[i]));
                            } else {
                                TextureUtil.copy(anim.sourceTexture, 0, phase.v, anim.width, anim.height, image, anim.targetX, anim.targetY);
                            }
                        }
                    }
                }, exec).thenRunAsync(this::upload, Minecraft.getInstance());
            }
        }

        for (var anim : anims) {
            if (anim.loop || playOnceRunning) {
                anim.advance();
            } else {
                anim.pause();
            }
        }
        frame++;
    }

    //? if <26.4 {
    @Override
    //?}
    public void tick() {
        if (Animatica.CONFIG.animatedTextures) {
            this.updateAndDraw(this.getPixels(), false, EXECUTORS);
        }
    }

    public static void shutdownExecutor() {
        EXECUTORS.shutdown();

        try {
            if (!EXECUTORS.awaitTermination(2, TimeUnit.SECONDS)) {
                EXECUTORS.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTORS.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static ThreadFactory daemonThreadFactory() {
        return task -> {
            var thread = new Thread(task, "Animatica animation worker #" + THREAD_COUNTER.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }

    @Override
    public synchronized void close() {
        if (this.closed) return;
        this.closed = true;

        for (var anim : anims) {
            anim.close();
        }

        this.original.close();
        //? if >=26.4 {
        /*this.pixels.close();
        *///?} else {
        super.close();
        //?}
    }

    // Represents an active animation from an animation meta file; progresses through phases while being drawn
    public static class Animation implements AutoCloseable {
        private final List<Phase> phases;
        public final NativeImage sourceTexture;
        public final int targetX;
        public final int targetY;
        public final int width;
        public final int height;
        public final boolean loop;
        private final int duration;

        private int frame = 0;
        private Phase currentPhase = null;
        private int phaseFrame = 0;
        private boolean changed = true;

        // Assembles all animation phases for one texture animation being baked
        public Animation(AnimationMeta meta, ResourceManager resources) throws IOException {
            this.targetX = meta.targetX();
            this.targetY = meta.targetY();
            this.width = meta.width();
            this.height = meta.height();
            this.loop = meta.loop();

            try (var source = resources.getResourceOrThrow(meta.source()).open()) {
                this.sourceTexture = NativeImage.read(source);
            }

            var phases = ImmutableList.<Phase>builder();
            int duration = 0;

            final int textureFrameCount = (int)Math.floor((float) sourceTexture.getHeight() / meta.height());
            final int animFrameCount = Math.max(textureFrameCount, meta.getGreatestUsedFrame() + 1);

            // The int array stored for each frame must contain the frame mapping and duration
            List<int[]> frames = new ArrayList<>();
            for (int f = 0; f < animFrameCount; f++) {
                if (f >= textureFrameCount && !meta.frameMapping().containsKey(f)) {
                    continue;
                }

                frames.add(new int[] {
                        meta.frameMapping().getOrDefault(f, f),
                        meta.frameDurations().getOrDefault(f, meta.defaultFrameDuration())
                });
            }

            for (int i = 0; i < frames.size(); i++) {
                int[] frame = frames.get(i);

                int fMap = frame[0];
                int fDuration = frame[1];

                int v = getVForFrame(fMap, textureFrameCount);
                int nextV = !this.loop && i == frames.size() - 1 ? v : getVForFrame(frames.get(Math.floorMod(i + 1, frames.size()))[0], textureFrameCount);

                if (meta.interpolate()) {
                    if (meta.interpolationDelay() > 0) {
                        // Adds a static version of the current phase as a "delay" before the next interpolated phase (if specified in animation)
                        phases.add(new Phase(meta.interpolationDelay(), v));
                        duration += meta.interpolationDelay();
                    }

                    // Add interpolated animation phase
                    final int interpolatedDuration = fDuration - meta.interpolationDelay();
                    phases.add(new InterpolatedPhase(interpolatedDuration, v, nextV, (phaseFrame) -> ((float) phaseFrame / interpolatedDuration)));
                    duration += interpolatedDuration;
                } else {
                    phases.add(new Phase(fDuration, v));
                    duration += fDuration;
                }
            }

            this.duration = duration;
            this.phases = phases.build();

            updateCurrentPhase();
        }

        public void updateCurrentPhase() {
            changed = false;
            int progress = frame;

            for (var phase : phases) {
                progress -= phase.duration; // Take away as much progress as each phase is long, until progress is below zero
                if (progress < 0) {
                    if (currentPhase != phase) {
                        // Marks baking anim as changed should it be in a new, unique phase
                        changed = true;
                    }
                    if (phase instanceof InterpolatedPhase iPhase && iPhase.hasChangingV()) changed = true; // Marks baking anim as changed should its current phase be changing

                    this.currentPhase = phase;
                    this.phaseFrame = phase.duration + progress; // Adding progress to the phase duration results in how far it is into the phase

                    return;
                }
            }
        }

        public Phase getCurrentPhase() {
            return currentPhase;
        }

        public int getPhaseFrame() {
            return phaseFrame;
        }

        public boolean isOnFrameZero() {
            return frame <= 0;
        }

        public boolean isChanged() {
            return changed;
        }

        public void advance() {
            if (!loop && frame >= duration - 1) {
                pause();
                return;
            }

            frame++;
            if (frame >= duration) {
                frame = 0;
            }
            updateCurrentPhase();
        }

        public void pause() {
            changed = false;
        }

        public void rewind() {
            frame = 0;
            updateCurrentPhase();
        }

        @Override
        public void close() {
            this.sourceTexture.close();
        }

        private int getVForFrame(int frame, int textureFrameCount) {
            return Utilities.clamp(frame * this.height, 0, (textureFrameCount - 1) * this.height);
        }
    }

    // Represents a phase that an animation is in (loosely defined by the animation file's "tile"s)
    // Base class represents the simplest possible type of animation phase, which only needs to generate
    // one texture to be used over a period of time
    public static class Phase {
        public final int duration;
        public final int v;

        public Phase(int duration, int v) {
            this.duration = duration;
            this.v = v;
        }

        @Override
        public String toString() {
            return "Animation Bakery Phase { v: "+this.v+" }";
        }
    }

    // A phase that blends between its previous phase and itself, requiring the generation of many
    // more textures to construct the blend animation
    public static class InterpolatedPhase extends Phase {
        public final int prevV;
        public final BlendInterpolator blend;

        public InterpolatedPhase(int duration, int v1, int v2, BlendInterpolator blend) {
            super(duration, v2);
            this.prevV = v1;
            this.blend = blend;
        }

        public boolean hasChangingV() {
            return this.prevV != this.v;
        }
    }

    @FunctionalInterface
    public interface BlendInterpolator {
        float getBlend(int phaseFrame);
    }
}
