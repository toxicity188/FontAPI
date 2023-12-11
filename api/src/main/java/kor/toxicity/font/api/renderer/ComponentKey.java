package kor.toxicity.font.api.renderer;

import kor.toxicity.font.api.component.WidthComponent;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

public record ComponentKey(
        @NotNull Key key,
        @NotNull @Unmodifiable Map<String, WidthComponent> imageMap
) {
}
