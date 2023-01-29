package com.atlassian.sourcemap;

/**
 * Mapping of position from generated file to source file.
 */
public interface Mapping
{
    public int getGeneratedLine();

    public int getGeneratedColumn();

    public int getSourceLine();

    public int getSourceColumn();

    public String getSourceFileName();

    public String getSourceSymbolName();
}