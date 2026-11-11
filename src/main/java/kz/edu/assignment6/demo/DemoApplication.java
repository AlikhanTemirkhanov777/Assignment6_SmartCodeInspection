package kz.edu.assignment6.demo;

import kz.edu.assignment6.model.*;
import kz.edu.assignment6.visitor.*;
import java.util.List;

public final class DemoApplication {
    private DemoApplication() { }

    public static Directory createFintechProject() {
        Directory root = new Directory("fintech-project");
        Directory src = new Directory("src");
        src.add(new SourceCodeFile("src/PaymentService.java", 180, 8, false, false, true));
        src.add(new SourceCodeFile("src/UserService.java", 130, 9, false, false, true));

        Directory tests = new Directory("tests");
        tests.add(new TestFile("tests/PaymentServiceTest.java", 95, 12, 12, "src/PaymentService.java"));

        Directory config = new Directory("config");
        config.add(new ConfigurationFile("config/application.properties", true, true, false, 18));

        root.add(src).add(tests).add(config);
        root.add(new DependencyFile("pom.xml", List.of(
                new Dependency("spring-boot-starter", "3.2.5", false, false, "Apache-2.0"),
                new Dependency("junit-jupiter", "5.10.2", false, false, "EPL-2.0")
        )));
        return root;
    }

    public static void main(String[] args) {
        Directory project = createFintechProject();
        System.out.println("=== SMART CODE INSPECTION & COMPLIANCE ENGINE ===\n");

        SecurityAuditVisitor security = new SecurityAuditVisitor();
        project.accept(security);
        System.out.println(security.report() + "\n");

        ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
        project.accept(statistics);
        System.out.println(statistics.report() + "\n");

        QualityGateVisitor qualityGate = new QualityGateVisitor();
        project.accept(qualityGate);
        System.out.println(qualityGate.report() + "\n");

        ComplianceVisitor compliance = new ComplianceVisitor();
        project.accept(compliance);
        System.out.println(compliance.report() + "\n");

    }
}
