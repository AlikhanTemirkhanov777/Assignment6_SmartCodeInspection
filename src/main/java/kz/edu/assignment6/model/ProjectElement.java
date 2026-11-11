package kz.edu.assignment6.model;

import kz.edu.assignment6.visitor.ProjectVisitor;

/** An element of a project tree that can be processed by different visitors. */
public interface ProjectElement {
    void accept(ProjectVisitor visitor);
}
