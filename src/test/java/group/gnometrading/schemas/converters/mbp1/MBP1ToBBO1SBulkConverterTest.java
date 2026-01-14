package group.gnometrading.schemas.converters.mbp1;

import group.gnometrading.schemas.BBO1SSchema;
import group.gnometrading.schemas.MBP1Schema;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static group.gnometrading.schemas.converters.mbp1.MBP1TestUtils.generate;
import static org.junit.jupiter.api.Assertions.*;

class MBP1ToBBO1SBulkConverterTest {

    @Test
    void testEmptyList() {
        var converter = new MBP1ToBBO1SBulkConverter();
        List<MBP1Schema> input = new ArrayList<>();
        List<BBO1SSchema> result = converter.convert(input);
        // Empty input means no pending samples, so result should be empty
        assertTrue(result.isEmpty());
    }

    @Test
    void testSingleElementNoSample() {
        // A single element won't trigger a sample during iteration,
        // but the final sample() call should produce output
        var converter = new MBP1ToBBO1SBulkConverter();
        List<MBP1Schema> input = List.of(generate(TimeUnit.MILLISECONDS.toNanos(500)));
        List<BBO1SSchema> result = converter.convert(input);

        // The final sample() should produce one result
        assertEquals(1, result.size());
    }

    @Test
    void testMultipleElementsWithinSameInterval() {
        var converter = new MBP1ToBBO1SBulkConverter();
        List<MBP1Schema> input = List.of(
                generate(TimeUnit.MILLISECONDS.toNanos(100)),
                generate(TimeUnit.MILLISECONDS.toNanos(200)),
                generate(TimeUnit.MILLISECONDS.toNanos(500))
        );
        List<BBO1SSchema> result = converter.convert(input);

        // All within the same 1-second interval, so only final sample() produces output
        assertEquals(1, result.size());
    }

    @Test
    void testMultipleElementsAcrossIntervals() {
        var converter = new MBP1ToBBO1SBulkConverter();
        List<MBP1Schema> input = List.of(
                generate(TimeUnit.MILLISECONDS.toNanos(500)),   // interval 0
                generate(TimeUnit.MILLISECONDS.toNanos(1000)),  // triggers sample for interval 0
                generate(TimeUnit.MILLISECONDS.toNanos(1500)),  // still in interval 1
                generate(TimeUnit.MILLISECONDS.toNanos(2500))   // triggers sample for interval 1
        );
        List<BBO1SSchema> result = converter.convert(input);

        // 2 samples during iteration + 1 final sample (last element started interval 2)
        assertEquals(3, result.size());
    }

    @Test
    void testFinalSampleIncludesLastInterval() {
        var converter = new MBP1ToBBO1SBulkConverter();
        MBP1Schema lastInput = generate(TimeUnit.MILLISECONDS.toNanos(1500));
        List<MBP1Schema> input = List.of(
                generate(TimeUnit.MILLISECONDS.toNanos(500)),
                generate(TimeUnit.MILLISECONDS.toNanos(1000)),  // triggers sample for interval 0
                lastInput
        );
        List<BBO1SSchema> result = converter.convert(input);

        // 1 sample during iteration + 1 final sample = 2 total
        assertEquals(2, result.size());

        // Verify the last result contains data from the last input
        BBO1SSchema lastResult = result.get(result.size() - 1);
        assertEquals(lastInput.decoder.exchangeId(), lastResult.decoder.exchangeId());
        assertEquals(lastInput.decoder.securityId(), lastResult.decoder.securityId());
    }

    @Test
    void testOutputSchemasAreIndependent() {
        var converter = new MBP1ToBBO1SBulkConverter();
        List<MBP1Schema> input = List.of(
                generate(TimeUnit.MILLISECONDS.toNanos(500)),
                generate(TimeUnit.MILLISECONDS.toNanos(1500)),
                generate(TimeUnit.MILLISECONDS.toNanos(2500))
        );
        List<BBO1SSchema> result = converter.convert(input);

        // Verify all results are different instances
        for (int i = 0; i < result.size(); i++) {
            for (int j = i + 1; j < result.size(); j++) {
                assertNotSame(result.get(i), result.get(j));
            }
        }
    }
}

