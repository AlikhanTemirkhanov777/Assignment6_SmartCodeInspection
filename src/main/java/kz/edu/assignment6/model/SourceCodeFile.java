package kz.edu.assignment6.model;

import kz.edu.assignment6.visitor.ProjectVisitor;
import java.util.Objects;

public final class SourceCodeFile implements ProjectElement {
    private final String path;
    private final int linesOfCode;
    private final int complexity;
    private final boolean hardcodedCredentials;
    private final boolean highRisk;
    private final boolean production;

    public SourceCodeFile(String path, int linesOfCode, int complexity,
                          boolean hardcodedCredentials, boolean highRisk, boolean production) {
        this.path = requirePath(path);
        if (linesOfCode < 0 || complexity < 0) throw new IllegalArgumentException("Metrics cannot be negative");
        this.linesOfCode = linesOfCode;
        this.complexity = complexity;
        this.hardcodedCredentials = hardcodedCredentials;
        this.highRisk = highRisk;
        this.production = production;
    }

    private static String requirePath(String value) {
        Objects.requireNonNull(value, "path");
        if (value.isBlank()) throw new IllegalArgumentException("path cannot be blank");
        return value;
    }

    public String getPath() { return path; }
    public int getLinesOfCode() { return linesOfCode; }
    public int getComplexity() { return complexity; }
    public boolean hasHardcodedCredentials() { return hardcodedCredentials; }
    public boolean isHighRisk() { return highRisk; }
    public boolean isProduction() { return production; }

    @Override public void accept(ProjectVisitor visitor) { visitor.visit(this); }
}
