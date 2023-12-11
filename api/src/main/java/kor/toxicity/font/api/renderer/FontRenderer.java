package kor.toxicity.font.api.renderer;

import kor.toxicity.font.api.component.WidthComponent;
import kor.toxicity.font.api.parser.FontParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface FontRenderer {
    @NotNull FontContent getFontContent();
    @NotNull @Unmodifiable List<ComponentKey> getKeys();
    int getCharSpace();
    @NotNull @Unmodifiable List<FontParser> getParser();

    @NotNull Builder builder();
    interface Builder {
        @NotNull Builder align(@NotNull FontAlign align);
        @NotNull Builder append(@NotNull String[] strings);
        @NotNull WidthComponent build();
    }
}
