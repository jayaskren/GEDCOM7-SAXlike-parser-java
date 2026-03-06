package org.gedcom7.writer;

import java.util.Objects;

/**
 * Immutable cross-reference handle returned from record-creating methods.
 *
 * <p>The {@code id} is the bare identifier without {@code @} delimiters.
 * The writer wraps it in {@code @..@} automatically when emitting output.
 */
public final class Xref {

    private final String id;

    private Xref(String id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    /**
     * Creates an Xref from a bare identifier string.
     *
     * @param id the identifier (without @ delimiters)
     * @return an Xref handle
     */
    public static Xref of(String id) {
        return new Xref(id);
    }

    /**
     * Returns the bare identifier (without @ delimiters).
     */
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Xref)) return false;
        Xref xref = (Xref) o;
        return id.equals(xref.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "@" + id + "@";
    }
}
