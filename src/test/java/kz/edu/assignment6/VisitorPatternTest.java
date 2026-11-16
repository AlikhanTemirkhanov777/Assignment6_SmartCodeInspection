package kz.edu.assignment6;

import kz.edu.assignment6.demo.DemoApplication;
import kz.edu.assignment6.model.*;
import kz.edu.assignment6.visitor.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VisitorPatternTest {
    @Test
    void allFiveElementsDispatchToTheirMatchingVisitMethod() {
        class RecordingVisitor implements ProjectVisitor {
            String visited;
            public void visit(SourceCodeFile f) { visited = "source:" + f.getPath(); }
            public void visit(TestFile f) { visited = "test"; }
            public void visit(ConfigurationFile f) { visited = "config"; }
            public void visit(DependencyFile f) { visited = "dependency"; }
            public void visit(Directory d) { visited = "directory"; }
        }
        RecordingVisitor visitor = new RecordingVisitor();
        new SourceCodeFile("A.java", 10, 2, false, false, true).accept(visitor);
        assertEquals("source:A.java", visitor.visited);
        new TestFile("ATest.java", 5, 1, 1, "A.java").accept(visitor);
        assertEquals("test", visitor.visited);
        new ConfigurationFile("app.properties", true, true, false, 2).accept(visitor);
        assertEquals("config", visitor.visited);
        new DependencyFile("pom.xml", List.of()).accept(visitor);
        assertEquals("dependency", visitor.visited);
        new Directory("nested").accept(visitor);
        assertEquals("directory", visitor.visited);
    }

    @Test
    void securityDetectsHardcodedCredentials() {
        SecurityAuditVisitor v = new SecurityAuditVisitor();
        new SourceCodeFile("Secret.java", 20, 3, true, false, true).accept(v);
        assertEquals(1L, v.getCriticalIssueCount());
        assertEquals(75, v.getSecurityScore());
        assertTrue(v.report().contains("Hardcoded credentials"));
    }

    @Test
    void securityDetectsVulnerableDependencies() {
        SecurityAuditVisitor v = new SecurityAuditVisitor();
        new DependencyFile("pom.xml", List.of(new Dependency("old-lib", "1.0", true, false, "MIT"))).accept(v);
        assertEquals(1L, v.getCriticalIssueCount());
        assertTrue(v.report().contains("Known vulnerable dependency"));
    }

    @Test
    void securityDetectsInsecureProductionConfiguration() {
        SecurityAuditVisitor v = new SecurityAuditVisitor();
        new ConfigurationFile("prod.properties", true, false, false, 5).accept(v);
        assertEquals(1L, v.getCriticalIssueCount());
    }

    @Test
    void securityAndQualityCountTwoIndependentConfigIssues() {
        Directory d = new Directory("app").add(new ConfigurationFile("prod.properties", true, false, true, 5));
        SecurityAuditVisitor security = new SecurityAuditVisitor();
        QualityGateVisitor quality = new QualityGateVisitor();
        d.accept(security);
        d.accept(quality);
        assertEquals(2L, security.getCriticalIssueCount());
        assertEquals(2, quality.getCriticalSecurityIssues());
        assertFalse(quality.getChecks().get("Security"));
    }

    @Test
    void securityScoreIsNeverNegative() {
        Directory d = new Directory("root");
        for (int i = 0; i < 10; i++)
            d.add(new SourceCodeFile("Secret" + i + ".java", 4, 2, true, false, true));
        SecurityAuditVisitor v = new SecurityAuditVisitor();
        d.accept(v);
        assertEquals(0, v.getSecurityScore());
        assertEquals(10L, v.getCriticalIssueCount());
    }

    @Test
    void statisticsAggregateNestedProject() {
        ProjectStatisticsVisitor v = new ProjectStatisticsVisitor();
        DemoApplication.createFintechProject().accept(v);
        assertEquals(5, v.getTotalFiles());
        assertEquals(2, v.getSourceFiles());
        assertEquals(1, v.getTestFiles());
        assertEquals(12, v.getTotalTests());
        assertEquals(12, v.getPassedTests());
        assertEquals(405, v.getLinesOfCode());
        assertEquals(2, v.getDependencies());
        assertEquals(8.5, v.getAverageComplexity(), 0.001);
        assertEquals(0.5, v.getTestSourceRatio(), 0.001);
        assertEquals(6.0, v.getTestCaseSourceRatio(), 0.001);
    }

    @Test
    void testFileRatioIsDifferentFromIndividualTestCountRatio() {
        Directory root = new Directory("root")
                .add(new SourceCodeFile("A.java", 10, 4, false, false, true))
                .add(new SourceCodeFile("B.java", 10, 8, false, false, true))
                .add(new TestFile("ATest.java", 5, 7, 6, "A.java"));
        ProjectStatisticsVisitor v = new ProjectStatisticsVisitor();
        root.accept(v);
        assertEquals(6.0, v.getAverageComplexity(), 0.001);
        assertEquals(0.5, v.getTestSourceRatio(), 0.001);
        assertEquals(3.5, v.getTestCaseSourceRatio(), 0.001);
        assertEquals(7, v.getTotalTests());
        assertEquals(6, v.getPassedTests());
    }

    @Test
    void qualityGatePassesAtThresholdBoundary() {
        Directory root = new Directory("root")
                .add(new SourceCodeFile("A.java", 30, 10, false, false, true))
                .add(new SourceCodeFile("B.java", 30, 10, false, false, true))
                .add(new TestFile("ATest.java", 10, 2, 2, "A.java"));
        QualityGateVisitor v = new QualityGateVisitor(10.0, 0.50);
        root.accept(v);
        assertTrue(v.isReadyForRelease());
        assertEquals(0.5, v.getTestSourceRatio(), 0.001);
    }

    @Test
    void qualityGateFailsForHighComplexityAndFewTests() {
        Directory root = new Directory("root")
                .add(new SourceCodeFile("A.java", 10, 22, false, false, true))
                .add(new SourceCodeFile("B.java", 10, 20, false, false, true));
        QualityGateVisitor v = new QualityGateVisitor();
        root.accept(v);
        assertFalse(v.isReadyForRelease());
        assertFalse(v.getChecks().get("Complexity"));
        assertFalse(v.getChecks().get("Tests"));
    }

    @Test
    void qualityGateFailsWhenTestFileRatioIsTooLow() {
        Directory root = new Directory("root")
                .add(new SourceCodeFile("A.java", 10, 4, false, false, true))
                .add(new SourceCodeFile("B.java", 10, 4, false, false, true))
                .add(new SourceCodeFile("C.java", 10, 4, false, false, true))
                .add(new TestFile("ATest.java", 5, 100, 100, "A.java"));
        QualityGateVisitor v = new QualityGateVisitor();
        root.accept(v);
        assertEquals(1.0/3, v.getTestSourceRatio(), 0.001);
        assertFalse(v.getChecks().get("Tests"));
    }

    @Test
    void qualityGateFailsForCriticalSecurityIssue() {
        Directory root = new Directory("root")
                .add(new SourceCodeFile("A.java", 10, 1, true, false, true))
                .add(new TestFile("ATest.java", 10, 4, 4, "A.java"));
        QualityGateVisitor v = new QualityGateVisitor();
        root.accept(v);
        assertFalse(v.getChecks().get("Security"));
        assertFalse(v.isReadyForRelease());
    }

    @Test
    void complianceDetectsUnpinnedAndProhibitedDependencies() {
        Directory root = new Directory("root").add(new DependencyFile("pom.xml", List.of(
                new Dependency("floating-lib", "LATEST", false, false, "MIT"),
                new Dependency("blocked-lib", "2.0", false, true, "Commercial")
        )));
        ComplianceVisitor v = new ComplianceVisitor();
        root.accept(v);
        assertEquals(2, v.getViolations().size());
        assertFalse(v.isCompliant());
    }

    @Test
    void complianceDetectsHardcodedSourceCredentials() {
        Directory root = new Directory("root")
                .add(new SourceCodeFile("src/Auth.java", 50, 3, true, false, false));
        ComplianceVisitor v = new ComplianceVisitor();
        root.accept(v);
        assertEquals(1, v.getViolations().size());
        assertTrue(v.report().contains("hardcoded credentials"));
    }

    @Test
    void complianceDetectsExposedConfigAndMissingProductionTests() {
        Directory root = new Directory("root")
                .add(new SourceCodeFile("Payment.java", 50, 4, false, false, true))
                .add(new ConfigurationFile("app.properties", true, true, true, 3));
        ComplianceVisitor v = new ComplianceVisitor();
        root.accept(v);
        assertEquals(2, v.getViolations().size());
        assertTrue(v.getViolations().stream().anyMatch(s -> s.contains("no mapped test")));
    }

    @Test
    void complianceRecognizesTestSourceLinkRegardlessOfOrder() {
        Directory root = new Directory("root")
                .add(new TestFile("tests/PaymentTest.java", 20, 2, 1, "src/Payment.java"))
                .add(new SourceCodeFile("src/Payment.java", 50, 4, false, false, true));
        ComplianceVisitor v = new ComplianceVisitor();
        root.accept(v);
        assertTrue(v.isCompliant());
    }

    @Test
    void complianceRejectsEmptyTestSuiteAsCoverage() {
        Directory root = new Directory("root")
                .add(new SourceCodeFile("src/Payment.java", 50, 4, false, false, true))
                .add(new TestFile("tests/PaymentTest.java", 0, 0, 0, "src/Payment.java"));
        ComplianceVisitor v = new ComplianceVisitor();
        root.accept(v);
        assertFalse(v.isCompliant());
    }

    @Test
    void nestedDirectoriesTraverseRecursively() {
        Directory root = new Directory("root").add(new Directory("middle")
                .add(new Directory("deep")
                .add(new SourceCodeFile("Deep.java", 7, 2, false, false, false))));
        ProjectStatisticsVisitor v = new ProjectStatisticsVisitor();
        root.accept(v);
        assertEquals(1, v.getSourceFiles());
        assertEquals(7, v.getLinesOfCode());
    }

    @Test
    void emptyDirectoryHasZeroMetricsWithoutDivisionByZero() {
        ProjectStatisticsVisitor v = new ProjectStatisticsVisitor();
        new Directory("empty").accept(v);
        assertEquals(0, v.getTotalFiles());
        assertEquals(0.0, v.getAverageComplexity(), 0.001);
        assertEquals(0.0, v.getTestSourceRatio(), 0.001);
    }

    @Test
    void emptyDirectoryIsNotReleaseReady() {
        QualityGateVisitor v = new QualityGateVisitor();
        new Directory("empty").accept(v);
        assertFalse(v.isReadyForRelease());
    }

    @Test
    void qualityGateRejectsEmptyTestFileEvenIfFileRatioPasses() {
        Directory root = new Directory("root")
                .add(new SourceCodeFile("A.java", 10, 3, false, false, true))
                .add(new TestFile("ATest.java", 0, 0, 0, "A.java"));
        QualityGateVisitor v = new QualityGateVisitor();
        root.accept(v);
        assertEquals(1.0, v.getTestSourceRatio(), 0.001);
        assertFalse(v.getChecks().get("Tests"));
    }

    @Test
    void technicalDebtIncreasesForComplexSource() {
        TechnicalDebtVisitor low = new TechnicalDebtVisitor();
        TechnicalDebtVisitor high = new TechnicalDebtVisitor();
        new SourceCodeFile("Simple.java", 100, 3, false, false, false).accept(low);
        new SourceCodeFile("Complex.java", 100, 18, false, true, false).accept(high);
        assertTrue(high.getDebtPoints() > low.getDebtPoints());
        assertEquals(1, high.getHighDebtFiles());
    }

    @Test
    void technicalDebtCountsFailedTestsAndVulnerableLibrary() {
        Directory root = new Directory("root")
                .add(new TestFile("T.java", 20, 5, 3, "A.java"))
                .add(new DependencyFile("pom.xml", List.of(new Dependency("old", "1.0", true, false, "MIT"))));
        TechnicalDebtVisitor v = new TechnicalDebtVisitor();
        root.accept(v);
        assertEquals(34, v.getDebtPoints());
        assertEquals(2, v.getInspectedFiles());
    }

    @Test
    void invalidElementMetricsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new SourceCodeFile("A.java", -1, 1, false, false, false));
        assertThrows(IllegalArgumentException.class,
                () -> new TestFile("ATest.java", 10, 2, 3, "A.java"));
        assertThrows(IllegalArgumentException.class,
                () -> new ConfigurationFile("config", false, true, false, -1));
    }

    @Test
    void dynamicDependencyVersionsAreRejected() {
        assertFalse(new Dependency("lib", "", false, false, "MIT").hasSpecifiedVersion());
        assertFalse(new Dependency("lib", "LATEST", false, false, "MIT").hasSpecifiedVersion());
        assertFalse(new Dependency("lib", "  latest  ", false, false, "MIT").hasSpecifiedVersion());
        assertFalse(new Dependency("lib", "1.+", false, false, "MIT").hasSpecifiedVersion());
        assertFalse(new Dependency("lib", "[1.0,2.0)", false, false, "MIT").hasSpecifiedVersion());
        assertTrue(new Dependency("lib", "2.4.0", false, false, "MIT").hasSpecifiedVersion());
    }

    @Test
    void selfContainingDirectoryIsRejected() {
        Directory root = new Directory("root");
        assertThrows(IllegalArgumentException.class, () -> root.add(root));
    }

    @Test
    void qualityGateRejectsNanThresholds() {
        assertThrows(IllegalArgumentException.class, () -> new QualityGateVisitor(Double.NaN, 0.4));
        assertThrows(IllegalArgumentException.class, () -> new QualityGateVisitor(10.0, Double.POSITIVE_INFINITY));
    }

    @Test
    void demoProjectPassesQualityButReportsMissingTestCoverage() {
        Directory root = DemoApplication.createFintechProject();
        QualityGateVisitor quality = new QualityGateVisitor();
        ComplianceVisitor compliance = new ComplianceVisitor();
        root.accept(quality);
        root.accept(compliance);
        assertTrue(quality.isReadyForRelease());
        assertFalse(compliance.isCompliant());
        assertEquals(1, compliance.getViolations().size());
        assertTrue(compliance.report().contains("UserService.java"));
    }
}
