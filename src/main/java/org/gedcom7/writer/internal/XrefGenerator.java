package org.gedcom7.writer.internal;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates unique cross-reference identifiers with per-prefix counters.
 *
 * <p>Each prefix (e.g., "I" for individuals, "F" for families) has its
 * own independent counter, producing IDs like "I1", "I2", "F1", "F2".
 */
public final class XrefGenerator {

    private final Map<String, Integer> counters = new HashMap<>();

    /**
     * Returns the next xref ID for the given prefix.
     *
     * @param prefix the prefix (e.g., "I", "F", "S")
     * @return the next unique ID (e.g., "I1", "I2")
     */
    public String next(String prefix) {
        int count = counters.merge(prefix, 1, Integer::sum);
        return prefix + count;
    }
}
