package kz.edu.assignment6.model;

import kz.edu.assignment6.visitor.ProjectVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Directory implements ProjectElement {
    private final String name;
    private final List<ProjectElement> children = new ArrayList<>();

    public Directory(String name) {
        this.name = Objects.requireNonNull(name, "name");
        if (name.isBlank()) throw new IllegalArgumentException("Directory name cannot be blank");
    }

    public String getName() { return name; }
    public Directory add(ProjectElement element) {
        children.add(Objects.requireNonNull(element, "element"));
        return this;
    }
    public List<ProjectElement> getChildren() { return List.copyOf(children); }
    public boolean isEmpty() { return children.isEmpty(); }

    @Override public void accept(ProjectVisitor visitor) { visitor.visit(this); }
}
