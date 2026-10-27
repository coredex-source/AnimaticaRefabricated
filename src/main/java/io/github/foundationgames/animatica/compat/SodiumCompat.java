//? if <26.4 {
package io.github.foundationgames.animatica.compat;

import io.github.foundationgames.animatica.Animatica;
import io.github.foundationgames.animatica.config.AnimaticaConfig.PlayOnceRestart;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;

import java.io.IOException;

public class SodiumCompat implements ConfigEntryPoint {
    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        var config = Animatica.CONFIG;
        var animatedTexturesId = Animatica.id("animated_textures");
        StorageEventHandler save = () -> {
            try {
                config.save();
            } catch (IOException e) { Animatica.LOG.error("Error saving config from Sodium's options!", e); }
        };

        builder.registerModOptions(Animatica.NAMESPACE)
                .setNonTintedIcon(Animatica.id("icon.png"))
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("option.animatica.page"))
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createBooleanOption(animatedTexturesId)
                                        .setName(Component.translatable("option.animatica.animated_textures"))
                                        .setTooltip(Component.translatable("option.animatica.animated_textures.tooltip"))
                                        .setDefaultValue(true)
                                        .setBinding(value -> config.animatedTextures = value, () -> config.animatedTextures)
                                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                                        .setStorageHandler(save))
                                .addOption(builder.createEnumOption(Animatica.id("play_once_restart"), PlayOnceRestart.class)
                                        .setName(Component.translatable("option.animatica.play_once_restart"))
                                        .setTooltip(Component.translatable("option.animatica.play_once_restart.tooltip"))
                                        .setElementNameProvider(mode -> Component.translatable(mode.translationKey()))
                                        .setDefaultValue(PlayOnceRestart.WHEN_DRAWN)
                                        .setBinding(value -> config.playOnceRestart = value, () -> config.playOnceRestart)
                                        .setEnabledProvider(state -> state.readBooleanOption(animatedTexturesId), animatedTexturesId)
                                        .setStorageHandler(save))));
    }
}
//?}
