package kz.edu.assignment6.visitor;

import kz.edu.assignment6.model.*;
import java.util.ArrayList;
import java.util.List;

public final class SecurityAuditVisitor implements ProjectVisitor {
    private final List<Finding> findings = new ArrayList<>();

    @Override public void visit(SourceCodeFile file) {
        if (file.hasHardcodedCredentials()) findings.add(new Finding(Severity.CRITICAL, file.getPath(), "Hardcoded credentials detected"));
        if (file.isHighRisk() || file.getComplexity() >= 15)
            findings.add(new Finding(Severity.HIGH, file.getPath(), "High-risk source file / excessive complexity"));
    }
    @Override public void visit(TestFile file) { }
    @Override public void visit(ConfigurationFile file) {
        if (file.hasExposedCredentials()) findings.add(new Finding(Severity.CRITICAL, file.getPath(), "Credentials are exposed in configuration"));
        if (file.isProduction() && !file.isSecure()) findings.add(new Finding(Severity.CRITICAL, file.getPath(), "Insecure production configuration"));
    }
    @Override public void visit(DependencyFile file) {
        for (Dependency dependency : file.getDependencies()) {
            if (dependency.vulnerable()) findings.add(new Finding(Severity.CRITICAL, dependency.name(), "Known vulnerable dependency"));
        }
    }
    @Override public void visit(Directory directory) { directory.getChildren().forEach(child -> child.accept(this)); }

    public List<Finding> getFindings() { return List.copyOf(findings); }
    public long getCriticalIssueCount() { return findings.stream().filter(f -> f.severity() == Severity.CRITICAL).count(); }
    public int getSecurityScore() {
        int deductions = findings.stream().mapToInt(f -> switch (f.severity()) {
            case LOW -> 2; case MEDIUM -> 5; case HIGH -> 12; case CRITICAL -> 25;
        }).sum();
        return Math.max(0, 100 - deductions);
    }
    public String report() {
        return "SECURITY AUDIT\nScore: " + getSecurityScore() + "/100\nFindings: " + findings.size()
                + (findings.isEmpty() ? "\nNo security issues found." : "\n" + String.join("\n", findings.stream().map(Finding::toString).toList()));
    }
}
