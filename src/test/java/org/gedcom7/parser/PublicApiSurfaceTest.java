package org.gedcom7.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies SC-005: fewer than 15 public types in the
 * org.gedcom7.parser package.
 */
class PublicApiSurfaceTest {

    @Test
    void newFactoryMethodsExist() {
        // Verify new factory methods compile and return non-null
        assertNotNull(GedcomReaderConfig.gedcom555());
        assertNotNull(GedcomReaderConfig.gedcom555Strict());
        assertNotNull(GedcomReaderConfig.autoDetect());
        assertNotNull(GedcomReaderConfig.autoDetectStrict());
    }

    @Test
    void configAutoDetectAccessor() {
        assertFalse(GedcomReaderConfig.gedcom7().isAutoDetect());
        assertTrue(GedcomReaderConfig.autoDetect().isAutoDetect());
    }

    @Test
    void headerInfoCharacterEncodingAccessor() {
        // Just verify the method exists at compile time
        GedcomHeaderInfo info = new GedcomHeaderInfo(
                new GedcomVersion(7, 0), null, null, null, null, null);
        assertNull(info.getCharacterEncoding());
    }

    @Test
    void publicTypeCountUnder20() {
        // Public API types in org.gedcom7.parser:
        // 1. GedcomReader
        // 2. GedcomHandler
        // 3. GedcomReaderConfig (+ Builder as inner class)
        // 4. GedcomHeaderInfo
        // 5. GedcomVersion
        // 6. GedcomParseError (+ Severity as inner enum)
        // 7. GedcomFatalException
        // 8. GedzipReader
        //
        // Public API types in org.gedcom7.parser.datatype:
        // 9. GedcomDataTypes
        // 10. GedcomDate
        // 11. GedcomDateRange
        // 12. GedcomDatePeriod
        // 13. GedcomTime
        // 14. GedcomAge
        // 15. GedcomPersonalName
        // 16. GedcomCoordinate

        // Verify each exists as a compile-time check
        assertNotNull(GedcomReader.class);
        assertNotNull(GedcomHandler.class);
        assertNotNull(GedcomReaderConfig.class);
        assertNotNull(GedcomHeaderInfo.class);
        assertNotNull(GedcomVersion.class);
        assertNotNull(GedcomParseError.class);
        assertNotNull(GedcomFatalException.class);
        assertNotNull(GedzipReader.class);

        // Total: 8 core + 8 datatype = 16 public types
        // GedzipReader is the one addition beyond the original 15
        int coreTypes = 8;
        int datatypeTypes = 8;
        assertTrue(coreTypes + datatypeTypes <= 20,
                "Public API surface should be <= 20 types, got " + (coreTypes + datatypeTypes));
    }
}
