package group.gnometrading.schemas;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaTest {

    @Test
    void testBasicCopy() {
        OHLCV1SSchema schema = new OHLCV1SSchema();

        schema.encoder.open(100);
        schema.encoder.high(20);
        schema.encoder.low(40);
        schema.encoder.close(60);
        schema.encoder.volume(50);

        OHLCV1SSchema other = new OHLCV1SSchema();
        other.copyFrom(schema);

        assertEquals(100, other.decoder.open());
        assertEquals(20, other.decoder.high());
        assertEquals(40, other.decoder.low());
        assertEquals(60, other.decoder.close());
        assertEquals(50, other.decoder.volume());
    }

}
