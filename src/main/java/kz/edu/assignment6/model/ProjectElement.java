package kz.edu.assignment6.model;
import kz.edu.assignment6.visitor.ProjectVisitor;
/** Element of a project tree. Analysis is delegated to a visitor. */
public interface ProjectElement { void accept(ProjectVisitor visitor); }
