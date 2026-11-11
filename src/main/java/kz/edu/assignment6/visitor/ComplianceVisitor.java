package kz.edu.assignment6.visitor;

import kz.edu.assignment6.model.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ComplianceVisitor implements ProjectVisitor {
    private final List<String> violations = new ArrayList<>();
    private final Set<String> testedSources = new HashSet<>();
    private final List<SourceCodeFile> productionSources = new ArrayList<>();

    @Override public void visit(SourceCodeFile file) { if (file.isProduction()) productionSources.add(file); }
    @Override public void visit(TestFile file) { if (!file.getTestedSourcePath().isBlank()) testedSources.add(file.getTestedSourcePath()); }
    @Override public void visit(ConfigurationFile file) {
        if (file.hasExposedCredentials()) violations.add(file.getPath() + ": exposed credentials are forbidden");
    }
    @Override public void visit(DependencyFile file) {
        for (Dependency dependency : file.getDependencies()) {
            if (!dependency.hasSpecifiedVersion()) violations.add(dependency.name() + ": dependency version must be pinned");
            if (dependency.prohibited()) violations.add(dependency.name() + ": prohibited dependency is not allowed");
        }
    }
    @Override public void visit(Directory directory) { directory.getChildren().forEach(child -> child.accept(this)); }

    public List<String> getViolations() {
        List<String> result = new ArrayList<>(violations);
        for (SourceCodeFile source : productionSources) {
            if (!testedSources.contains(source.getPath())) result.add(source.getPath() + ": production source has no mapped test file");
        }
        return List.copyOf(result);
    }
    public boolean isCompliant() { return getViolations().isEmpty(); }
    public String report() {
        List<String> all = getViolations();
        return "COMPLIANCE CHECK\nStatus: " + (all.isEmpty() ? "COMPLIANT" : "NON-COMPLIANT")
                + "\nViolations: " + all.size() + (all.isEmpty() ? "" : "\n- " + String.join("\n- ", all));
    }
}
