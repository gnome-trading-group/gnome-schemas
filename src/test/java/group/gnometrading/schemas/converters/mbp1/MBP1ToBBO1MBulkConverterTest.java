package group.gnometrading.schemas.converters.mbp1;

import group.gnometrading.schemas.BBO1MSchema;
import group.gnometrading.schemas.MBP1Schema;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static group.gnometrading.schemas.converters.mbp1.MBP1TestUtils.generate;
import static org.junit.jupiter.api.Assertions.*;

class MBP1ToBBO1MBulkConverterTest {

    @Test
    void testEmptyList() {
        var converter = new MBP1ToBBO1MBulkConverter();
        List<MBP1Schema> input = new ArrayList<>();
        List<BBO1MSchema> result = converter.convert(input);
        // SamplingSchemaBulkConverter always calls sample() at the end,
        // which returns a result even for empty input (with uninitialized data)
        assertEquals(1, result.size());
    }

    @Test
    void testSingleElementNoSample() {
        // A single element won't trigger a sample during iteration,
        // but the final sample() call should produce output
        var converter = new MBP1ToBBO1MBulkConverter();
        List<MBP1Schema> input = List.of(generate(TimeUnit.SECONDS.toNanos(30)));
        List<BBO1MSchema> result = converter.convert(input);

        // The final sample() should produce one result
        assertEquals(1, result.size());
    }

    @Test
    void testMultipleElementsWithinSameInterval() {
        var converter = new MBP1ToBBO1MBulkConverter();
        List<MBP1Schema> input = List.of(
                generate(TimeUnit.SECONDS.toNanos(10)),
                generate(TimeUnit.SECONDS.toNanos(20)),
                generate(TimeUnit.SECONDS.toNanos(30))
        );
        List<BBO1MSchema> result = converter.convert(input);

        // All within the same 1-minute interval, so only final sample() produces output
        assertEquals(1, result.size());
    }

    @Test
    void testMultipleElementsAcrossIntervals() {
        var converter = new MBP1ToBBO1MBulkConverter();
        List<MBP1Schema> input = List.of(
                generate(TimeUnit.SECONDS.toNanos(30)),   // interval 0
                generate(TimeUnit.MINUTES.toNanos(1)),    // triggers sample for interval 0
                generate(TimeUnit.SECONDS.toNanos(90)),   // still in interval 1
                generate(TimeUnit.MINUTES.toNanos(2) + TimeUnit.SECONDS.toNanos(30))  // triggers sample for interval 1
        );
        List<BBO1MSchema> result = converter.convert(input);

        // 2 samples during iteration + 1 final sample = 3 total
        assertEquals(3, result.size());
    }

    @Test
    void testFinalSampleIncludesLastInterval() {
        var converter = new MBP1ToBBO1MBulkConverter();
        MBP1Schema lastInput = generate(TimeUnit.SECONDS.toNanos(90));
        List<MBP1Schema> input = List.of(
                generate(TimeUnit.SECONDS.toNanos(30)),
                generate(TimeUnit.MINUTES.toNanos(1)),  // triggers sample for interval 0
                lastInput
        );
        List<BBO1MSchema> result = converter.convert(input);

        // 1 sample during iteration + 1 final sample = 2 total
        assertEquals(2, result.size());

        // Verify the last result contains data from the last input
        BBO1MSchema lastResult = result.get(result.size() - 1);
        assertEquals(lastInput.decoder.exchangeId(), lastResult.decoder.exchangeId());
        assertEquals(lastInput.decoder.securityId(), lastResult.decoder.securityId());
    }

    @Test
    void testOutputSchemasAreIndependent() {
        var converter = new MBP1ToBBO1MBulkConverter();
        List<MBP1Schema> input = List.of(
                generate(TimeUnit.SECONDS.toNanos(30)),
                generate(TimeUnit.MINUTES.toNanos(1) + TimeUnit.SECONDS.toNanos(30)),
                generate(TimeUnit.MINUTES.toNanos(2) + TimeUnit.SECONDS.toNanos(30))
        );
        List<BBO1MSchema> result = converter.convert(input);

        // Verify all results are different instances
        for (int i = 0; i < result.size(); i++) {
            for (int j = i + 1; j < result.size(); j++) {
                assertNotSame(result.get(i), result.get(j));
            }
        }
    }
}

