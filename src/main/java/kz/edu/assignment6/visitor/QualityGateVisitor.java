package kz.edu.assignment6.visitor;

import kz.edu.assignment6.model.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;

public final class QualityGateVisitor implements ProjectVisitor {
    private final double maxAverageComplexity;
    private final double minTestSourceRatio;
    private int sourceFiles;
    private int totalTests;
    private long complexityTotal;
    private int criticalSecurityIssues;

    public QualityGateVisitor() { this(10.0, 0.40); }
    public QualityGateVisitor(double maxAverageComplexity, double minTestSourceRatio) {
        if (maxAverageComplexity < 0 || minTestSourceRatio < 0) throw new IllegalArgumentException("Thresholds cannot be negative");
        this.maxAverageComplexity = maxAverageComplexity;
        this.minTestSourceRatio = minTestSourceRatio;
    }

    @Override public void visit(SourceCodeFile file) {
        sourceFiles++; complexityTotal += file.getComplexity();
        if (file.hasHardcodedCredentials()) criticalSecurityIssues++;
    }
    @Override public void visit(TestFile file) { totalTests += file.getTestsCount(); }
    @Override public void visit(ConfigurationFile file) {
        if (file.hasExposedCredentials() || (file.isProduction() && !file.isSecure())) criticalSecurityIssues++;
    }
    @Override public void visit(DependencyFile file) {
        criticalSecurityIssues += (int) file.getDependencies().stream().filter(Dependency::vulnerable).count();
    }
    @Override public void visit(Directory directory) { directory.getChildren().forEach(child -> child.accept(this)); }

    public double getAverageComplexity() { return sourceFiles == 0 ? 0.0 : (double) complexityTotal / sourceFiles; }
    public double getTestSourceRatio() { return sourceFiles == 0 ? 0.0 : (double) totalTests / sourceFiles; }
    public int getCriticalSecurityIssues() { return criticalSecurityIssues; }
    public Map<String, Boolean> getChecks() {
        Map<String, Boolean> checks = new LinkedHashMap<>();
        checks.put("Security", criticalSecurityIssues == 0);
        checks.put("Complexity", getAverageComplexity() <= maxAverageComplexity);
        checks.put("Tests", getTestSourceRatio() >= minTestSourceRatio);
        return Collections.unmodifiableMap(checks);
    }
    public boolean isReadyForRelease() { return getChecks().values().stream().allMatch(Boolean::booleanValue); }
    public String report() {
        StringBuilder result = new StringBuilder("QUALITY GATE\n");
        getChecks().forEach((name, pass) -> result.append(name).append(": ").append(pass ? "PASS" : "FAIL").append('\n'));
        return result.append("FINAL RESULT: ").append(isReadyForRelease() ? "PASSED" : "FAILED").toString();
    }
}
