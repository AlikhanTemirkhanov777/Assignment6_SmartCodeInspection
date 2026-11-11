package kz.edu.assignment6.model;

import java.util.Objects;

public record Dependency(String name, String version, boolean vulnerable,
                         boolean prohibited, String license) {
    public Dependency {
        Objects.requireNonNull(name, "name");
        license = license == null ? "UNKNOWN" : license;
        if (name.isBlank()) throw new IllegalArgumentException("Dependency name cannot be blank");
        version = version == null ? "" : version;
    }
    public boolean hasSpecifiedVersion() { return !version.isBlank() && !version.equalsIgnoreCase("LATEST"); }
}
