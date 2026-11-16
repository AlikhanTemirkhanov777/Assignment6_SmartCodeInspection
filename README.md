# Assignment 6 — Smart Code Inspection & Compliance Engine

**Pattern:** Visitor | **Language:** Java 17+ | **Build:** Maven | **Tests:** JUnit 5

## 1. Project overview

This application analyzes a simulated fintech project. Project files are represented by independent elements; security, statistics, release readiness, compliance, and technical debt are implemented as separate visitors. All visitors process **the same project tree**.

The demo constructs this logical structure in Java:

```text
fintech-project/
├── src/
│   ├── PaymentService.java
│   └── UserService.java
├── tests/
│   └── PaymentServiceTest.java
├── config/
│   └── application.properties
└── pom.xml
```

The tree is an **in-memory model**, not a directory scanner: file paths and metrics are demo data. In the demo, the quality gate passes, but compliance detects the missing test mapping for `UserService.java`. This deliberately demonstrates a non-compliant project and is not a runtime error.

## 2. Architecture

### Elements (`kz.edu.assignment6.model`)

| Class | Purpose |
|---|---|
| `ProjectElement` | Declares `accept(ProjectVisitor)` |
| `SourceCodeFile` | Source LOC, complexity, credential/risk flags, production flag |
| `TestFile` | Test LOC, number of test cases, number passed, covered source path |
| `ConfigurationFile` | Production/secure/exposed-credential flags, setting count |
| `DependencyFile` and `Dependency` | Dependency versions and vulnerability/prohibition metadata |
| `Directory` | Contains `ProjectElement` children, including nested directories |

### Visitors (`kz.edu.assignment6.visitor`)

| Visitor | Result |
|---|---|
| `SecurityAuditVisitor` | Findings with severity, security score out of 100 |
| `ProjectStatisticsVisitor` | Counts files, test cases, LOC, dependencies, complexity, ratios |
| `QualityGateVisitor` | Checks critical security issues, complexity <= 10, test-file/source-file ratio >= 0.40 |
| `ComplianceVisitor` | Flags hardcoded/exposed credentials, unpinned versions, prohibited dependencies, and untested production source files |
| `TechnicalDebtVisitor` | Adds heuristic debt points for risky code, failed tests, vulnerable dependencies, and insecure configuration |

Each element implements `accept(ProjectVisitor visitor)` by calling `visitor.visit(this)`. Each visitor implements all five overloads of `visit`. A directory recursively visits its children: `child.accept(this)`. There is no `instanceof` or switch on element types.

### Definitions used by the metrics

- **Source files** = the number of `SourceCodeFile` elements.
- **Test files** = the number of `TestFile` elements.
- **Number of tests** = sum of `TestFile.testsCount`.
- **Average complexity** = sum of source complexity / number of source files (zero if none).
- **Test/source ratio** = number of **test files / source files**; this is used by the Quality Gate. For the demo: `1 / 2 = 0.50`.
- **Test cases/source ratio** = number of individual test cases / source files (additional statistic). For the demo: `12 / 2 = 6.00`.
- An empty project is **not** ready for release, even if custom thresholds are set to zero. A test file without any test cases is also insufficient for a PASS.
- A test mapping counts toward compliance coverage only when the `TestFile` has at least one test case.

## 3. Double dispatch

1. The client holds a `ProjectElement` and calls `element.accept(visitor)`.
2. Java selects the concrete element's `accept` implementation at runtime.
3. Inside `SourceCodeFile.accept`, `this` has the concrete type `SourceCodeFile`, so the matching `visit(SourceCodeFile)` overload is selected.
4. The concrete visitor determines the operation to perform.

This is **double dispatch**: the operation depends on the concrete element and the concrete visitor.

## 4. Assignment questions

**1. What problem does Visitor solve?** It separates many different analysis operations from the project file classes. A new operation can be added without editing each element class.

**2. Where are Element and Visitor?** `ProjectElement` and concrete file/directory classes are in `model`; `ProjectVisitor` and five concrete visitor implementations are in `visitor`.

**3. What is double dispatch?** `accept(visitor)` invokes the element-specific method, then `visitor.visit(this)` calls the appropriate visitor overload for that concrete element type.

**4. Why not put all operations in Elements?** That would mix data and unrelated reporting logic, violate SRP, and force edits to each element class whenever a new analysis is required.

**5. Which is easier to add: a Visitor or an Element? Why?** A visitor: create one class implementing `ProjectVisitor`. A new element requires a new class, a new overload in `ProjectVisitor`, and an implementation in every visitor.

**6. When is Visitor a bad choice?** When new element types are added frequently, there are very few different operations, or access to private state would require exposing excessive implementation details.

**7. Visitor versus Strategy?** Strategy swaps algorithms for one behavior through a common interface. Visitor performs operations over multiple element types through element-specific `visit` overloads and double dispatch.

**8. SOLID principles?** SRP: data and analysis are separated. OCP: new visitors extend the system without changes to the element classes. DIP: concrete elements refer to the `ProjectVisitor` interface. LSP: any `ProjectElement` can be processed through the common `accept` contract. ISP is supported by a focused visitor interface for this fixed hierarchy, but adding unrelated element types can make the interface too broad.

## 5. Visitor limitation challenge — DatabaseMigrationFile

To add `DatabaseMigrationFile`:

1. Create `DatabaseMigrationFile implements ProjectElement` with its data and `accept` method.
2. Add `void visit(DatabaseMigrationFile file);` to `ProjectVisitor`.
3. Implement this method in **all five visitors**; otherwise they will not compile.
4. Add tests, demo data, and update UML.

Visitor favors adding operations over frequently changing the list of element types.

## 6. Building and running

Requirements: JDK 17+ and Maven 3.8+.

```bash
mvn clean test
mvn -q exec:java -Dexec.mainClass=kz.edu.assignment6.demo.DemoApplication
```

In IntelliJ IDEA: open the project folder (the one containing `pom.xml`), wait for Maven import, select JDK 17 or newer, run `VisitorPatternTest`, then run `DemoApplication.main()`.

On Windows, double-click `run-demo.bat` to compile and run the demo using the JDK (no Maven required). Run the JUnit tests separately using `mvn clean test` or the IntelliJ test runner. On Linux/macOS, `bash run-demo.sh` compiles and runs the demo using just the JDK.

## 7. Testing

`src/test/java/kz/edu/assignment6/VisitorPatternTest.java` contains **28 behavioral JUnit 5 tests** covering:

- All five concrete element types and dispatch;
- Hardcoded credentials, vulnerable dependencies, insecure production config, and score clamping;
- Nested directories, statistics, individual test counts, and file-based coverage ratio;
- Release PASS/FAIL, boundary threshold and zero-source cases;
- Missing production tests, unpinned/prohibited dependencies, config and source credentials;
- Extra `TechnicalDebtVisitor` and failed test cases;
- Invalid metrics, `NaN` thresholds, direct directory self-reference, dynamic versions;
- Demo requirements and intentionally non-compliant coverage.

The test suite uses real assertions and can be executed with Maven (`mvn clean test`).

## 8. UML

- `docs/class-diagram.puml` — class diagram source.
- `docs/sequence-diagram.puml` — sequence diagram source.
- `docs/class-diagram.png` and `docs/sequence-diagram.png` — rendered diagrams for convenient review.

The class diagram contains the element and visitor interfaces, five concrete elements, five visitors, `Directory` children relationship, and `accept`/`visit` coupling. The sequence diagram demonstrates a client visiting `SourceCodeFile` with `SecurityAuditVisitor` and requesting its report.

## 9. Git requirements

The original public repository contains **14 incremental commits**, including the independent addition of `TechnicalDebtVisitor` in commit `66441b0` (without changes to any concrete element class). The fixed ZIP is a clean working copy and does not include `.git` history. To synchronize the corrections with the public GitHub repository, replace the corresponding files in your checked-out repository and make a **new corrective commit**, keeping the existing history intact.

Public repository: https://github.com/AlikhanTemirkhanov777/Assignment6_SmartCodeInspection

## 10. Expected demo output (highlights)

```text
SECURITY AUDIT: score 100/100, 0 findings
PROJECT STATISTICS: 5 files, 2 source, 1 test file, 12 test cases, 405 LOC
Average complexity: 8.50
Test/source ratio (files): 0.50
QUALITY GATE: Security PASS / Complexity PASS / Tests PASS / FINAL RESULT PASSED
COMPLIANCE CHECK: NON-COMPLIANT (UserService.java has no mapped test)
TECHNICAL DEBT: 0 points, LOW
```

## 11. Defense live changes

To add another operation such as `LicenseAuditVisitor`: implement `ProjectVisitor`, supply all five `visit(...)` methods, traverse `Directory` via `child.accept(this)`, and call `project.accept(newVisitor)`. **No concrete Element class must be edited.**

To add a new type `DatabaseMigrationFile`, the visitor interface and every concrete visitor **must** be updated. That is the central Visitor trade-off.
