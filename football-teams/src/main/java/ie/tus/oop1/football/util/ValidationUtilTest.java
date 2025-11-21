package ie.tus.oop1.football.util;

import junit.framework.TestCase;

/**
 * Unit tests for ValidationUtil using JUnit 3 framework.
 * Tests validate correct parsing of age and year values,
 * including boundary and invalid inputs.
 */
public class ValidationUtilTest extends TestCase {

    // ---------------------------------------------------------
    // parseAge Tests
    // ---------------------------------------------------------

    /**
     * Test #001: Valid Age
     * Input: "20"
     * Expected Output: 20 (No exception)
     * Purpose: Ensure age in valid range (15-75) is parsed correctly.
     */
    public void testParseAge001() {
        try {
            int age = ValidationUtil.parseAge("20");
            assertEquals(20, age);
        } catch (ExceptionHandler e) {
            fail("Exception not expected");
        }
    }

    /**
     * Test #002: Too Young Age
     * Input: "14"
     * Expected: ExceptionHandler with message "Age must be between 15 and 75"
     * Purpose: Ensure lower boundary validation is enforced.
     */
    public void testParseAge002() {
        try {
            ValidationUtil.parseAge("14");
            fail("Exception expected");
        } catch (ExceptionHandler e) {
            assertEquals("Age must be between 15 and 75", e.getMessage());
        }
    }

    /**
     * Test #003: Too Old Age
     * Input: "90"
     * Expected: ExceptionHandler with message "Age must be between 15 and 75"
     * Purpose: Ensure upper boundary validation is enforced.
     */
    public void testParseAge003() {
        try {
            ValidationUtil.parseAge("90");
            fail("Exception expected");
        } catch (ExceptionHandler e) {
            assertEquals("Age must be between 15 and 75", e.getMessage());
        }
    }


    // ---------------------------------------------------------
    // parseYear Tests
    // ---------------------------------------------------------

    /**
     * Test #004: Valid Year
     * Input: "2000"
     * Expected: 2000 (No exception)
     * Purpose: Ensure valid year within allowed range is accepted.
     */
    public void testParseYear001() {
        try {
            int year = ValidationUtil.parseYear("2000");
            assertEquals(2000, year);
        } catch (ExceptionHandler e) {
            fail("Exception not expected");
        }
    }

    /**
     * Test #005: Too Old Year
     * Input: "1800"
     * Expected: ExceptionHandler with message "Invalid year"
     * Purpose: Ensure lower year limit validation works.
     */
    public void testParseYear002() {
        try {
            ValidationUtil.parseYear("1800");
            fail("Exception expected");
        } catch (ExceptionHandler e) {
            assertEquals("Invalid year", e.getMessage());
        }
    }

    /**
     * Test #006: Future Year
     * Input: "3000"
     * Expected: ExceptionHandler with message "Invalid year"
     * Purpose: Ensure year greater than current year is rejected.
     */
    public void testParseYear003() {
        try {
            ValidationUtil.parseYear("3000");
            fail("Exception expected");
        } catch (ExceptionHandler e) {
            assertEquals("Invalid year", e.getMessage());
        }
    }
}
