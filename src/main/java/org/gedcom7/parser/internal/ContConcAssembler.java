package org.gedcom7.parser.internal;

/**
 * GEDCOM 5.5.5 payload assembler. Recognises both CONT and CONC
 * as continuation pseudo-structures. CONT joins with a newline;
 * CONC joins with no separator (direct concatenation).
 */
public final class ContConcAssembler implements PayloadAssembler {

    @Override
    public boolean isPseudoStructure(String tag) {
        return "CONT".equals(tag) || "CONC".equals(tag);
    }

    @Override
    public void appendPayload(StringBuilder payload, String continuationValue, String tag) {
        if ("CONT".equals(tag)) {
            payload.append('\n');
        }
        // CONC: no separator
        if (continuationValue != null) {
            payload.append(continuationValue);
        }
    }
}
