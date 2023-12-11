package kor.toxicity.font.api.parser;

import kor.toxicity.font.api.component.WidthComponent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface FontParser {
    @NotNull WidthComponent parse(@NotNull String string);
    @NotNull Builder builder();
    interface Builder {
        @NotNull Builder append(@NotNull String tag, @NotNull FontTagMapper mapper);
        @NotNull FontParser build();
    }
}
