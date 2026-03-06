package org.gedcom7.converter;

/**
 * Callback for conversion warnings encountered during GEDCOM version conversion.
 *
 * <p>Implementations receive each warning as it occurs during conversion,
 * enabling real-time logging or monitoring of conversion quality.
 */
@FunctionalInterface
public interface ConversionWarningHandler {

    /**
     * Called when a conversion warning is encountered.
     *
     * @param warning the conversion warning details
     */
    void handle(ConversionWarning warning);
}
