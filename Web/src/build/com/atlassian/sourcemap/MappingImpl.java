package com.atlassian.sourcemap;

/**
 * Mapping for Source Map.
 */
class MappingImpl implements Mapping {
    private final int generatedLine;
    private final int generatedColumn;
    private final int sourceLine;
    private final int sourceColumn;
    private final String sourceFileName;
    private final String sourceSymbolName;

    public MappingImpl(int generatedLine, int generatedColumn, int sourceLine, int sourceColumn, String sourceFileName, String sourceSymbolName) {
        this.generatedLine = generatedLine;
        this.generatedColumn = generatedColumn;
        this.sourceLine = sourceLine;
        this.sourceColumn = sourceColumn;
        this.sourceFileName = sourceFileName;
        this.sourceSymbolName = sourceSymbolName;
    }

    public String toString() {
        return "Mapping " + generatedLine + ":" + generatedColumn + " -> " + sourceFileName + ":" + sourceLine + ":" + sourceColumn;
    }

    @Override
    public int getGeneratedLine() {
        return generatedLine;
    }

    @Override
    public int getGeneratedColumn() {
        return generatedColumn;
    }

    @Override
    public int getSourceLine() {
        return sourceLine;
    }

    @Override
    public int getSourceColumn() {
        return sourceColumn;
    }

    @Override
    public String getSourceFileName() {
        return sourceFileName;
    }

    @Override
    public String getSourceSymbolName() {
        return sourceSymbolName;
    }
}
