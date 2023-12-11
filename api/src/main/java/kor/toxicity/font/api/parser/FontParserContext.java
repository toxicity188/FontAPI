package kor.toxicity.font.api.parser;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
public class FontParserContext {
    private final char content;
    private final int width;
    private final @NotNull Key key;
    private @Nullable TextColor color;
    private final Map<TextDecoration, TextDecoration.State> decorations = new EnumMap<>(TextDecoration.class);
}
