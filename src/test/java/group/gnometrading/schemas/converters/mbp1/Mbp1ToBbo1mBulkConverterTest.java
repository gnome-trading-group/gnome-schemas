package group.gnometrading.schemas.converters.mbp1;

import static group.gnometrading.schemas.converters.mbp1.Mbp1TestUtils.generate;
import static org.junit.jupiter.api.Assertions.*;

import group.gnometrading.schemas.Bbo1mSchema;
import group.gnometrading.schemas.Mbp1Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class Mbp1ToBbo1mBulkConverterTest {

    @Test
    void testEmptyList() {
        var converter = new Mbp1ToBbo1mBulkConverter();
        List<Mbp1Schema> input = new ArrayList<>();
        List<Bbo1mSchema> result = converter.convert(input);
        // Empty input means no pending samples, so result should be empty
        assertTrue(result.isEmpty());
    }

    @Test
    void testSingleElementNoSample() {
        // A single element won't trigger a sample during iteration,
        // but the final sample() call should produce output
        var converter = new Mbp1ToBbo1mBulkConverter();
        List<Mbp1Schema> input = List.of(generate(TimeUnit.SECONDS.toNanos(30)));
        List<Bbo1mSchema> result = converter.convert(input);

        // The final sample() should produce one result
        assertEquals(1, result.size());
    }

    @Test
    void testMultipleElementsWithinSameInterval() {
        var converter = new Mbp1ToBbo1mBulkConverter();
        List<Mbp1Schema> input = List.of(
                generate(TimeUnit.SECONDS.toNanos(10)),
                generate(TimeUnit.SECONDS.toNanos(20)),
                generate(TimeUnit.SECONDS.toNanos(30)));
        List<Bbo1mSchema> result = converter.convert(input);

        // All within the same 1-minute interval, so only final sample() produces output
        assertEquals(1, result.size());
    }

    @Test
    void testMultipleElementsAcrossIntervals() {
        var converter = new Mbp1ToBbo1mBulkConverter();
        List<Mbp1Schema> input = List.of(
                generate(TimeUnit.SECONDS.toNanos(30)), // interval 0
                generate(TimeUnit.MINUTES.toNanos(1)), // triggers sample for interval 0
                generate(TimeUnit.SECONDS.toNanos(90)), // still in interval 1
                generate(TimeUnit.MINUTES.toNanos(2) + TimeUnit.SECONDS.toNanos(30)) // triggers sample for interval 1
                );
        List<Bbo1mSchema> result = converter.convert(input);

        // 2 samples during iteration + 1 final sample (last element started interval 2)
        assertEquals(3, result.size());
    }

    @Test
    void testFinalSampleIncludesLastInterval() {
        var converter = new Mbp1ToBbo1mBulkConverter();
        Mbp1Schema lastInput = generate(TimeUnit.SECONDS.toNanos(90));
        List<Mbp1Schema> input = List.of(
                generate(TimeUnit.SECONDS.toNanos(30)),
                generate(TimeUnit.MINUTES.toNanos(1)), // triggers sample for interval 0
                lastInput);
        List<Bbo1mSchema> result = converter.convert(input);

        // 1 sample during iteration + 1 final sample = 2 total
        assertEquals(2, result.size());

        // Verify the last result contains data from the last input
        Bbo1mSchema lastResult = result.get(result.size() - 1);
        assertEquals(lastInput.decoder.exchangeId(), lastResult.decoder.exchangeId());
        assertEquals(lastInput.decoder.securityId(), lastResult.decoder.securityId());
    }

    @Test
    void testOutputSchemasAreIndependent() {
        var converter = new Mbp1ToBbo1mBulkConverter();
        List<Mbp1Schema> input = List.of(
                generate(TimeUnit.SECONDS.toNanos(30)),
                generate(TimeUnit.MINUTES.toNanos(1) + TimeUnit.SECONDS.toNanos(30)),
                generate(TimeUnit.MINUTES.toNanos(2) + TimeUnit.SECONDS.toNanos(30)));
        List<Bbo1mSchema> result = converter.convert(input);

        // Verify all results are different instances
        for (int i = 0; i < result.size(); i++) {
            for (int j = i + 1; j < result.size(); j++) {
                assertNotSame(result.get(i), result.get(j));
            }
        }
    }
}
