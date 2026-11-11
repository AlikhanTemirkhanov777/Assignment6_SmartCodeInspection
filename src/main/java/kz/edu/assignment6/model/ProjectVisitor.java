package kz.edu.assignment6.model;

/** Operations are kept separate from the project element hierarchy. */
public interface ProjectVisitor {
    void visit(SourceCodeFile file);
    void visit(TestFile file);
    void visit(ConfigurationFile file);
    void visit(DependencyFile file);
    void visit(Directory directory);
}
