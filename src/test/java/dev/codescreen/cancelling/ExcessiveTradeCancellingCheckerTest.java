package dev.codescreen.cancelling;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link ExcessiveTradeCancellingChecker}
 */
public class ExcessiveTradeCancellingCheckerTest {

    @Test
    public void testCompaniesInvolvedInExcessiveCancellations() {
        assertEquals(Arrays.asList("Ape accountants", "Cauldron cooking"), ExcessiveTradeCancellingChecker.companiesInvolvedInExcessiveCancellations());
    }

}
