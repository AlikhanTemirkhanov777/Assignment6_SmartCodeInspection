package kz.edu.assignment6.visitor;

import kz.edu.assignment6.model.*;

public final class ProjectStatisticsVisitor implements ProjectVisitor {
    private int totalFiles;
    private int sourceFiles;
    private int testFiles;
    private int totalTests;
    private int linesOfCode;
    private int dependencies;
    private long complexityTotal;

    @Override public void visit(SourceCodeFile file) {
        totalFiles++;
        sourceFiles++;
        linesOfCode += file.getLinesOfCode();
        complexityTotal += file.getComplexity();
    }

    @Override public void visit(TestFile file) {
        totalFiles++;
        testFiles++;
        totalTests += file.getTestsCount();
        linesOfCode += file.getLinesOfCode();
    }

    @Override public void visit(ConfigurationFile file) { totalFiles++; }

    @Override public void visit(DependencyFile file) {
        totalFiles++;
        dependencies += file.getDependencies().size();
    }

    @Override public void visit(Directory directory) {
        directory.getChildren().forEach(child -> child.accept(this));
    }

    public int getTotalFiles() { return totalFiles; }
    public int getSourceFiles() { return sourceFiles; }
    public int getTestFiles() { return testFiles; }
    public int getTotalTests() { return totalTests; }
    public int getLinesOfCode() { return linesOfCode; }
    public int getDependencies() { return dependencies; }

    public double getAverageComplexity() {
        return sourceFiles == 0 ? 0.0 : (double) complexityTotal / sourceFiles;
    }

    /** Number of individual tests divided by the number of production source files. */
    public double getTestSourceRatio() {
        return sourceFiles == 0 ? 0.0 : (double) totalTests / sourceFiles;
    }

    public String report() {
        return String.format(java.util.Locale.ROOT,
                "PROJECT STATISTICS%nFiles: %d (source: %d, test files: %d)%n"
                        + "Total tests: %d%nLines of code: %d%nDependencies: %d%n"
                        + "Average complexity: %.2f%nTest/source ratio: %.2f",
                totalFiles, sourceFiles, testFiles, totalTests, linesOfCode, dependencies,
                getAverageComplexity(), getTestSourceRatio());
    }
}
