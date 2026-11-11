package kz.edu.assignment6.visitor;

import kz.edu.assignment6.model.*;

/** Estimates remediation effort using transparent, deterministic heuristic points. */
public final class TechnicalDebtVisitor implements ProjectVisitor {
    private int debtPoints;
    private int inspectedFiles;
    private int highDebtFiles;

    @Override public void visit(SourceCodeFile file) {
        inspectedFiles++;
        int points = Math.max(0, file.getComplexity() - 10) * 3;
        if (file.isHighRisk()) points += 20;
        if (file.hasHardcodedCredentials()) points += 50;
        if (points >= 20) highDebtFiles++;
        debtPoints += points;
    }

    @Override public void visit(TestFile file) {
        inspectedFiles++;
        debtPoints += (file.getTestsCount() - file.getPassedTests()) * 2;
    }

    @Override public void visit(ConfigurationFile file) {
        inspectedFiles++;
        if (!file.isSecure()) debtPoints += 10;
        if (file.hasExposedCredentials()) debtPoints += 50;
    }

    @Override public void visit(DependencyFile file) {
        inspectedFiles++;
        for (Dependency dependency : file.getDependencies()) {
            if (dependency.vulnerable()) debtPoints += 30;
            if (!dependency.hasSpecifiedVersion()) debtPoints += 3;
        }
    }

    @Override public void visit(Directory directory) {
        directory.getChildren().forEach(child -> child.accept(this));
    }

    public int getDebtPoints() { return debtPoints; }
    public int getInspectedFiles() { return inspectedFiles; }
    public int getHighDebtFiles() { return highDebtFiles; }
    public String report() {
        String level = debtPoints < 20 ? "LOW" : debtPoints < 60 ? "MEDIUM" : "HIGH";
        return "TECHNICAL DEBT\nEstimated points: " + debtPoints + "\nInspected files: " + inspectedFiles
                + "\nHigh-debt source files: " + highDebtFiles + "\nDebt level: " + level;
    }
}
