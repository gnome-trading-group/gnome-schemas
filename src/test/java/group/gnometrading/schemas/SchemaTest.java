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

        assertEquals(schema.decoder.open(), other.decoder.open());
        assertEquals(schema.decoder.high(), other.decoder.high());
        assertEquals(schema.decoder.low(), other.decoder.low());
        assertEquals(schema.decoder.close(), other.decoder.close());
        assertEquals(schema.decoder.volume(), other.decoder.volume());
    }

}
