package kz.edu.assignment6.visitor;

import java.util.Objects;

public record Finding(Severity severity, String location, String message) {
    public Finding {
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(message, "message");
    }
    @Override public String toString() { return "[" + severity + "] " + location + " - " + message; }
}
