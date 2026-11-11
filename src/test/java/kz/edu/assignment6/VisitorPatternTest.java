package kz.edu.assignment6;

import kz.edu.assignment6.demo.DemoApplication;
import kz.edu.assignment6.model.*;
import kz.edu.assignment6.visitor.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VisitorPatternTest {
    @Test
    void sourceElementDispatchesToCorrectVisitMethod() {
        class RecordingVisitor implements ProjectVisitor {
            String visited = "";
            public void visit(SourceCodeFile file) { visited = "source:" + file.getPath(); }
            public void visit(TestFile file) { visited = "test"; }
            public void visit(ConfigurationFile file) { visited = "config"; }
            public void visit(DependencyFile file) { visited = "dependency"; }
            public void visit(Directory directory) { visited = "directory"; }
        }
        RecordingVisitor visitor = new RecordingVisitor();
        new SourceCodeFile("A.java", 10, 2, false, false, true).accept(visitor);
        assertEquals("source:A.java", visitor.visited);
    }

    @Test
    void securityVisitorFindsHardcodedCredentialsAsCritical() {
        Directory root = new Directory("root").add(new SourceCodeFile("Secret.java", 20, 3, true, false, true));
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        root.accept(visitor);
        assertEquals(1, visitor.getCriticalIssueCount());
        assertEquals(75, visitor.getSecurityScore());
    }

    @Test
    void securityVisitorFindsVulnerableDependency() {
        Directory root = new Directory("root").add(new DependencyFile("pom.xml",
                List.of(new Dependency("old-lib", "1.0", true, false, "MIT"))));
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        root.accept(visitor);
        assertTrue(visitor.report().contains("vulnerable dependency"));
    }

    @Test
    void securityVisitorFindsInsecureProductionConfiguration() {
        Directory root = new Directory("root").add(new ConfigurationFile("prod.properties", true, false, false, 5));
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        root.accept(visitor);
        assertEquals(1, visitor.getCriticalIssueCount());
    }

    @Test
    void safeProjectHasFullSecurityScore() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        new Directory("empty").accept(visitor);
        assertEquals(100, visitor.getSecurityScore());
        assertTrue(visitor.getFindings().isEmpty());
    }

    @Test
    void statisticsAggregatesNestedProject() {
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        DemoApplication.createFintechProject().accept(visitor);
        assertEquals(5, visitor.getTotalFiles());
        assertEquals(2, visitor.getSourceFiles());
        assertEquals(2, visitor.getTestFiles());
        assertEquals(2, visitor.getDependencies());
        assertEquals(405, visitor.getLinesOfCode());
    }

    @Test
    void statisticsCalculatesAverageComplexityAndTestRatio() {
        Directory root = new Directory("root")
                .add(new SourceCodeFile("A.java", 10, 4, false, false, true))
                .add(new SourceCodeFile("B.java", 10, 8, false, false, true))
                .add(new TestFile("ATest.java", 5, 2, 2, "A.java"));
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        root.accept(visitor);
        assertEquals(6.0, visitor.getAverageComplexity(), 0.001);
        assertEquals(0.5, visitor.getTestSourceRatio(), 0.001);
    }

    @Test
    void qualityGatePassesWhenAllThresholdsAreMet() {
        Directory root = new Directory("root")
                .add(new SourceCodeFile("A.java", 10, 5, false, false, true))
                .add(new TestFile("ATest.java", 10, 4, 4, "A.java"));
        QualityGateVisitor visitor = new QualityGateVisitor();
        root.accept(visitor);
        assertTrue(visitor.isReadyForRelease());
        assertTrue(visitor.getChecks().values().stream().allMatch(Boolean::booleanValue));
    }

    @Test
    void qualityGateFailsForComplexityAndInsufficientTests() {
        Directory root = new Directory("root")
                .add(new SourceCodeFile("A.java", 10, 22, false, false, true))
                .add(new SourceCodeFile("B.java", 10, 20, false, false, true))
                .add(new TestFile("ATest.java", 5, 1, 1, "A.java"));
        QualityGateVisitor visitor = new QualityGateVisitor();
        root.accept(visitor);
        assertFalse(visitor.isReadyForRelease());
        assertFalse(visitor.getChecks().get("Complexity"));
        assertFalse(visitor.getChecks().get("Tests"));
    }

    @Test
    void qualityGateFailsForCriticalSecurityIssue() {
        Directory root = new Directory("root").add(new SourceCodeFile("A.java", 10, 1, true, false, true));
        QualityGateVisitor visitor = new QualityGateVisitor();
        root.accept(visitor);
        assertFalse(visitor.getChecks().get("Security"));
    }

    @Test
    void complianceDetectsUnpinnedAndProhibitedDependencies() {
        Directory root = new Directory("root").add(new DependencyFile("pom.xml", List.of(
                new Dependency("floating-lib", "LATEST", false, false, "MIT"),
                new Dependency("blocked-lib", "2.0", false, true, "Commercial")
        )));
        ComplianceVisitor visitor = new ComplianceVisitor();
        root.accept(visitor);
        assertEquals(2, visitor.getViolations().size());
        assertFalse(visitor.isCompliant());
    }

    @Test
    void complianceDetectsExposedCredentialsAndMissingProductionTests() {
        Directory root = new Directory("root")
                .add(new SourceCodeFile("Payment.java", 50, 4, false, false, true))
                .add(new ConfigurationFile("app.properties", true, true, true, 3));
        ComplianceVisitor visitor = new ComplianceVisitor();
        root.accept(visitor);
        assertEquals(2, visitor.getViolations().size());
        assertTrue(visitor.getViolations().stream().anyMatch(v -> v.contains("no mapped test")));
    }

    @Test
    void nestedDirectoriesAreTraversedRecursively() {
        Directory deep = new Directory("deep").add(new SourceCodeFile("Deep.java", 7, 2, false, false, false));
        Directory root = new Directory("root").add(new Directory("middle").add(deep));
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        root.accept(visitor);
        assertEquals(1, visitor.getSourceFiles());
        assertEquals(7, visitor.getLinesOfCode());
    }

    @Test
    void emptyDirectoryHasZeroMetricsAndDoesNotDivideByZero() {
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        new Directory("empty").accept(visitor);
        assertEquals(0, visitor.getTotalFiles());
        assertEquals(0.0, visitor.getAverageComplexity());
        assertEquals(0.0, visitor.getTestSourceRatio());
    }

    @Test
    void technicalDebtVisitorProducesHigherDebtForComplexCode() {
        Directory low = new Directory("low").add(new SourceCodeFile("Simple.java", 100, 3, false, false, false));
        Directory high = new Directory("high").add(new SourceCodeFile("Complex.java", 100, 18, false, true, false));
        TechnicalDebtVisitor lowVisitor = new TechnicalDebtVisitor();
        TechnicalDebtVisitor highVisitor = new TechnicalDebtVisitor();
        low.accept(lowVisitor);
        high.accept(highVisitor);
        assertTrue(highVisitor.getDebtPoints() > lowVisitor.getDebtPoints());
        assertEquals(1, highVisitor.getHighDebtFiles());
    }

    @Test
    void configurationAndDependencyEdgeCasesAreHandled() {
        assertThrows(IllegalArgumentException.class, () -> new SourceCodeFile("A.java", -1, 1, false, false, false));
        assertThrows(IllegalArgumentException.class, () -> new TestFile("ATest.java", 10, 2, 3, "A.java"));
        assertFalse(new Dependency("lib", "", false, false, "MIT").hasSpecifiedVersion());
    }

    @Test
    void demoProjectMeetsQualityGateButReportsMissingTestCoverage() {
        Directory root = DemoApplication.createFintechProject();
        QualityGateVisitor quality = new QualityGateVisitor();
        root.accept(quality);
        ComplianceVisitor compliance = new ComplianceVisitor();
        root.accept(compliance);
        assertTrue(quality.isReadyForRelease());
        assertFalse(compliance.isCompliant());
    }
}
