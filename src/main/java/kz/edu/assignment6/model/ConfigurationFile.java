package kz.edu.assignment6.model;

import kz.edu.assignment6.visitor.ProjectVisitor;
import java.util.Objects;

public final class ConfigurationFile implements ProjectElement {
    private final String path;
    private final boolean production;
    private final boolean secure;
    private final boolean exposedCredentials;
    private final int settingsCount;

    public ConfigurationFile(String path, boolean production, boolean secure,
                             boolean exposedCredentials, int settingsCount) {
        this.path = Objects.requireNonNull(path, "path");
        if (path.isBlank() || settingsCount < 0) throw new IllegalArgumentException("Invalid configuration data");
        this.production = production;
        this.secure = secure;
        this.exposedCredentials = exposedCredentials;
        this.settingsCount = settingsCount;
    }

    public String getPath() { return path; }
    public boolean isProduction() { return production; }
    public boolean isSecure() { return secure; }
    public boolean hasExposedCredentials() { return exposedCredentials; }
    public int getSettingsCount() { return settingsCount; }

    @Override public void accept(ProjectVisitor visitor) { visitor.visit(this); }
}
