package com.atlassian.sourcemap;

import java.io.IOException;
import java.io.InputStream;

/**
 * Helpers for converting source maps.
 */
public class Util
{
    /**
     * Generate source map comment for JS or CSS.
     * @param type "css" or "js" string.
     */
    public static String generateSourceMapComment(String sourceMapUrl, String type) {
        if ("js".equals(type))
            return "//# sourceMappingURL=" + sourceMapUrl;
        else if ("css".equals(type))
            return "/*# sourceMappingURL=" + sourceMapUrl + " */";
        else
            throw new RuntimeException("invalid source map type " + type);
    }


    /**
     * Generates 1 to 1 mapping, it's needed in order to create source map for batch. When source maps of individual
     * resources joined into the batch source map - if some of resources doesn't have source map then the 1 to 1 source
     * map would be generated for it.
     * @param source source content.
     * @param sourceUrl source url.
     * @return 1 to 1 source map.
     */
    public static SourceMap create1to1SourceMap(CharSequence source, String sourceUrl) {
        SourceMap map = new SourceMapImpl();
        int line = 0;
        map.addMapping(line, 0, line, 0, sourceUrl);

        for (int i = 0; i < source.length(); i++ ) {
            if (source.charAt(i) == '\n') {
                line += 1;
                map.addMapping(line, 0, line, 0, sourceUrl);
            }
        }
        return map;
    }

    /**
     * Helper to count newlines in content.
     */
    public static int countLines(InputStream stream) {
        try {
            int c = stream.read();
            int counter = 0;
            while (c != -1) {
                if (c == '\n') counter += 1;
                c = stream.read();
            }
            return counter + 1;
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    /**
     * Helper to count newlines in content.
     */
    public static int countLines(CharSequence stream) {
        int counter = 0;
        for (int i = 0; i < stream.length(); i++) {
            if (stream.charAt(i) == '\n') counter += 1;
        }
        return counter + 1;
    }

    /**
     * Create new source map by adding offset to existing.
     * @return new source map with offset.
     */
    public static SourceMap offset(SourceMap sourceMap, int offset) {
        return new SourceMapImpl(sourceMap, offset);
    }

    /**
     * If multiple transformations applied to the source each of it could generate its own source map. Rebase allows to
     * unite all this maps and generate the final map. It's done by rebasing each map on the map of the previous
     * transformation.
     *
     * @param sourceMap current source map.
     * @param previousSourceMap map from previous transformation.
     */
    public static SourceMap rebase(SourceMap sourceMap, final SourceMap previousSourceMap) {
        final SourceMap rebasedMap = new SourceMapImpl();
        sourceMap.eachMapping(new SourceMap.EachMappingCallback() {
            public void apply(Mapping mapping) {
                Mapping rebasedMapping = previousSourceMap.getMapping(mapping.getSourceLine(), mapping.getSourceColumn());
                if (rebasedMapping != null)  {
                    rebasedMap.addMapping(
                        mapping.getGeneratedLine(),
                        mapping.getGeneratedColumn(),
                        rebasedMapping.getSourceLine(),
                        rebasedMapping.getSourceColumn(),
                        rebasedMapping.getSourceFileName(),
                        rebasedMapping.getSourceSymbolName()
                    );
                }
            }
        });
        return rebasedMap;
    }

    /**
     * Join multiple source map.
     * @return helper to join mutliple source map.
     */
    public static SourceMapJoiner joiner() {
        return new SourceMapJoiner();
    }
}