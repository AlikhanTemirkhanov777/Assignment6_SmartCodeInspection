package kz.edu.assignment6.visitor;
import kz.edu.assignment6.model.*;
public interface ProjectVisitor {
    void visit(SourceCodeFile file);
    void visit(TestFile file);
    void visit(ConfigurationFile file);
    void visit(DependencyFile file);
    void visit(Directory directory);
}
