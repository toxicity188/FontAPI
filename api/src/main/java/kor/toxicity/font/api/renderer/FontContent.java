package kor.toxicity.font.api.renderer;

import java.util.List;
import java.util.Map;

public record FontContent(
        int maxHeight,
        Map<FontImage, List<Character>> contents
) {
}
