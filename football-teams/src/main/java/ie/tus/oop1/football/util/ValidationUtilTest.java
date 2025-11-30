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
    public void testParseYear004() {
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
    public void testParseYear005() {
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
    public void testParseYear006() {
        try {
            ValidationUtil.parseYear("3000");
            fail("Exception expected");
        } catch (ExceptionHandler e) {
            assertEquals("Invalid year", e.getMessage());
        }
    }
    // ---------------------------------------------------------
    // parseAge Additional Tests
    // ---------------------------------------------------------



    /**
     * Test #007: Valid Boundary Age at Lower Limit
     * Input: "15"
     * Expected: 15 (No exception)
     * Purpose: Ensure minimum age boundary is accepted.
     */
    public void testParseAge007() {
        try {
            int age = ValidationUtil.parseAge("15");
            assertEquals(15, age);
        } catch (ExceptionHandler e) {
            fail("Exception not expected");
        }
    }

    /**
     * Test #008: Valid Boundary Age at Upper Limit
     * Input: "75"
     * Expected: 75 (No exception)
     * Purpose: Ensure maximum age boundary is accepted.
     */
    public void testParseAge008() {
        try {
            int age = ValidationUtil.parseAge("75");
            assertEquals(75, age);
        } catch (ExceptionHandler e) {
            fail("Exception not expected");
        }
    }


    // ---------------------------------------------------------
    // parseYear Additional Tests
    // ---------------------------------------------------------



    /**
     * Test #009: Valid Year at Lower Boundary
     * Input: "1900" (Assuming 1900 is allowed lower limit)
     * Expected: 1900 (No exception)
     * Purpose: Boundary test for lowest valid year.
     */
    public void testParseYear009() {
        try {
            int year = ValidationUtil.parseYear("1900");
            assertEquals(1900, year);
        } catch (ExceptionHandler e) {
            fail("Exception not expected");
        }
    }

}
