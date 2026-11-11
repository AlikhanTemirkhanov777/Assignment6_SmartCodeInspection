package kz.edu.assignment6.model;

import kz.edu.assignment6.visitor.ProjectVisitor;
import java.util.Objects;

public final class TestFile implements ProjectElement {
    private final String path;
    private final int linesOfCode;
    private final int testsCount;
    private final int passedTests;
    private final String testedSourcePath;

    public TestFile(String path, int linesOfCode, int testsCount, int passedTests, String testedSourcePath) {
        this.path = Objects.requireNonNull(path, "path");
        if (path.isBlank()) throw new IllegalArgumentException("path cannot be blank");
        if (linesOfCode < 0 || testsCount < 0 || passedTests < 0 || passedTests > testsCount)
            throw new IllegalArgumentException("Invalid test metrics");
        this.linesOfCode = linesOfCode;
        this.testsCount = testsCount;
        this.passedTests = passedTests;
        this.testedSourcePath = testedSourcePath == null ? "" : testedSourcePath;
    }

    public String getPath() { return path; }
    public int getLinesOfCode() { return linesOfCode; }
    public int getTestsCount() { return testsCount; }
    public int getPassedTests() { return passedTests; }
    public String getTestedSourcePath() { return testedSourcePath; }

    @Override public void accept(ProjectVisitor visitor) { visitor.visit(this); }
}
