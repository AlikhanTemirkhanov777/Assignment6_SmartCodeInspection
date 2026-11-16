package kz.edu.assignment6.model;
import java.util.Objects;
public record Dependency(String name, String version, boolean vulnerable,
                         boolean prohibited, String license) {
    public Dependency {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) throw new IllegalArgumentException("Dependency name cannot be blank");
        version = version == null ? "" : version;
        license = license == null ? "UNKNOWN" : license;
    }
    public boolean hasSpecifiedVersion() {
        String value = version.trim();
        return !value.isEmpty() && !value.equalsIgnoreCase("LATEST")
                && !value.equalsIgnoreCase("RELEASE")
                && !value.contains("+") && !value.contains("[") && !value.contains("(");
    }
}
