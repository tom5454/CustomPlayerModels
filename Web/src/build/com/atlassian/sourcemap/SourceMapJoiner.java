package com.atlassian.sourcemap;

import java.util.ArrayList;
import java.util.List;

/**
 * Joins source maps.
 */
public class SourceMapJoiner
{
    static class SourceMapWithOffset
    {
        SourceMap sourceMap;
        int offset;
        int linesCount;

        public SourceMapWithOffset(SourceMap sourceMap, int linesCount, int offset){
            this.sourceMap = sourceMap;
            this.linesCount = linesCount;
            this.offset = offset;
        }
    }

    List<SourceMapWithOffset> sourceMaps = new ArrayList<SourceMapWithOffset>();

    /**
     * Create joined source map by joining multiple source maps, each of it additionally could have the offset.
     * @param sourceMap source map to add.
     * @param length number of lines of added source map.
     * @param offset offset of added source map (note - the offset is inside of its content, not outside).
     */
    public void addSourceMap(SourceMap sourceMap, int length, int offset)
    {
        sourceMaps.add(new SourceMapWithOffset(sourceMap, length, offset));
    }

    /**
     * Joins added source maps.
     * @return joined source map.
     */
    public SourceMap join()
    {
        final SourceMap joinedMap = new SourceMapImpl();
        int lineOffset = 0;
        for (SourceMapWithOffset sourceMapWithOffset : sourceMaps)
        {
            int offset = sourceMapWithOffset.offset;
            int linesCount = sourceMapWithOffset.linesCount;
            SourceMap sourceMap = sourceMapWithOffset.sourceMap;
            lineOffset += offset;
            final int finalLineOffset = lineOffset;
            // If source map is equal to null we skipping it, but adding its linesCount to next offset.
            if (sourceMap != null)
            {
                sourceMap.eachMapping(new SourceMap.EachMappingCallback()
                {
                    public void apply(Mapping mapping)
                    {
                        joinedMap.addMapping(
                            finalLineOffset + mapping.getGeneratedLine(),
                            mapping.getGeneratedColumn(),
                            mapping.getSourceLine(),
                            mapping.getSourceColumn(),
                            mapping.getSourceFileName(),
                            mapping.getSourceSymbolName()
                        );
                    }
                });
            }

            // Lines count already included before and after offsets.
            lineOffset += linesCount - offset;
        }
        return joinedMap;
    }
}
