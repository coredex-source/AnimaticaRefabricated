package io.github.foundationgames.animatica.config;

import com.mojang.serialization.Codec;
import io.github.foundationgames.animatica.Animatica;
import io.github.foundationgames.animatica.loaders.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public class AnimaticaConfig {
    public static String ANIMATED_TEXTURES_KEY = "animated_textures";
    public static String PLAY_ONCE_RESTART_KEY = "play_once_restart";

    public static final String FILE_NAME = "animatica.properties";

    private final OptionInstance<Boolean> animatedTexturesOption;
    public boolean animatedTextures;
    private final OptionInstance<PlayOnceRestart> playOnceRestartOption;
    public PlayOnceRestart playOnceRestart;

    public AnimaticaConfig() {
        try {
            load();
        } catch (IOException e) {
            Animatica.LOG.error("Error loading config during initialization!", e);
        }

        this.animatedTexturesOption = OptionInstance.createBoolean(
                "option.animatica.animated_textures",
                this.animatedTextures,
                value -> {
                    this.animatedTextures = value;
                    try {
                        this.save();
                    } catch (IOException e) { Animatica.LOG.error("Error saving config while changing in game!", e); }
                    Minecraft.getInstance().reloadResourcePacks();
                }
        );

        this.playOnceRestartOption = new OptionInstance<>(
                "option.animatica.play_once_restart",
                OptionInstance.cachedConstantTooltip(Component.translatable("option.animatica.play_once_restart.tooltip")),
                (caption, value) -> Options.genericValueLabel(caption, Component.translatable(value.translationKey())),
                new OptionInstance.Enum<>(List.of(PlayOnceRestart.values()), Codec.STRING.xmap(PlayOnceRestart::byKey, mode -> mode.key)),
                this.playOnceRestart,
                value -> {
                    this.playOnceRestart = value;
                    try {
                        this.save();
                    } catch (IOException e) { Animatica.LOG.error("Error saving config while changing in game!", e); }
                }
        );
    }

    public void writeTo(Properties properties) {
        properties.put(ANIMATED_TEXTURES_KEY, Boolean.toString(animatedTextures));
        properties.put(PLAY_ONCE_RESTART_KEY, playOnceRestart.key);
    }

    public void readFrom(Properties properties) {
        this.animatedTextures = boolFrom(properties.getProperty(ANIMATED_TEXTURES_KEY), true);
        this.playOnceRestart = PlayOnceRestart.byKey(properties.getProperty(PLAY_ONCE_RESTART_KEY));
    }

    public Path getFile() throws IOException {
        var file = Platform.getConfigDir().resolve(FILE_NAME);
        if (!Files.exists(file)) {
            Files.createFile(file);
        }

        return file;
    }

    public OptionInstance<Boolean> getAnimatedTexturesOption() {
        return animatedTexturesOption;
    }

    public OptionInstance<PlayOnceRestart> getPlayOnceRestartOption() {
        return playOnceRestartOption;
    }

    public void save() throws IOException {
        var file = getFile();
        var properties = new Properties();

        writeTo(properties);

        try (var out = Files.newOutputStream(file)) {
            properties.store(out, "Configuration file for Animatica");
        }
    }

    public void load() throws IOException {
        var file = getFile();
        var properties = new Properties();

        try (var in = Files.newInputStream(file)) {
            properties.load(in);
        }

        readFrom(properties);
    }

    private static boolean boolFrom(String s, boolean defaultVal) {
        return s == null ? defaultVal : "true".equals(s);
    }

    public enum PlayOnceRestart {
        WHEN_DRAWN("when_drawn"),
        ON_RELOAD("on_reload");

        public final String key;

        PlayOnceRestart(String key) {
            this.key = key;
        }

        public String translationKey() {
            return "option.animatica.play_once_restart." + key;
        }

        public static PlayOnceRestart byKey(String key) {
            for (var mode : values()) {
                if (mode.key.equals(key)) {
                    return mode;
                }
            }
            return WHEN_DRAWN;
        }
    }
}
