package kor.toxicity.font.api.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FontTagMapper {
    @NotNull FontParserTag apply(@NotNull String[] args, @Nullable FontParserContext context);
}
