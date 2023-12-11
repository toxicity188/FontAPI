package kor.toxicity.font.api;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface FontPlatform {
    @NotNull File getDataFolder();
    void log(@NotNull String log, @NotNull LoggerLevel level);
    @NotNull IFontAPI getFontAPI();
}
