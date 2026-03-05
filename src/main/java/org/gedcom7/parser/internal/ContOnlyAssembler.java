package org.gedcom7.parser.internal;

/**
 * GEDCOM 7 payload assembler. Recognises {@code CONT} as
 * a continuation pseudo-structure and joins payloads with
 * a newline. {@code CONC} is NOT recognised (GEDCOM 7 dropped it).
 */
public final class ContOnlyAssembler implements PayloadAssembler {

    @Override
    public boolean isPseudoStructure(String tag) {
        return "CONT".equals(tag);
    }

    @Override
    public void appendPayload(StringBuilder payload, String continuationValue, String tag) {
        payload.append('\n');
        if (continuationValue != null) {
            payload.append(continuationValue);
        }
    }
}
