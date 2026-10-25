package group.gnometrading.schemas;

import static org.junit.jupiter.api.Assertions.*;

import group.gnometrading.schemas.book.Action;
import group.gnometrading.schemas.book.Side;
import org.junit.jupiter.api.Test;

class StreamRecordTest {

    @Test
    void readsTheStreamHeaderWithoutKnowingTheRecordType() {
        final Mbp10Schema record = new Mbp10Schema();
        record.encoder.streamId(1_962_670L);
        record.encoder.timestampEvent(111L);
        record.encoder.timestampRecv(222L);
        record.encoder.sequence(333L);
        record.encoder.flags().clear().badData(true).marketByPrice(true);
        record.encoder.action(Action.Trade).side(Side.Bid);

        assertEquals(1_962_670, record.streamId());
        assertEquals(111L, record.getEventTimestamp());
        assertEquals(222L, record.getRecvTimestamp());
        assertEquals(333L, record.getSequenceNumber());
        assertTrue(record.isBadData());
        assertFalse(record.isSnapshot());
        assertEquals(1_962_670, StreamHeader.streamId(record.buffer, 0));
    }

    @Test
    void everyRecordTypeKnowsItsTemplateAndFamily() {
        for (final RecordType type : RecordType.values()) {
            final StreamRecord record = type.newInstance();
            assertEquals(type, record.recordType);
            assertEquals(type.templateId(), record.templateId());
            assertEquals(type.family().schemaId(), record.schemaId());
            assertTrue(type.family().owns(type.templateId()), type + " is outside its family's template ids");
            assertSame(type, RecordType.forTemplateId(type.templateId()));
            assertSame(type, RecordType.forIdentifier(type.identifier()));
        }
    }

    @Test
    void recordsByTemplateIdHoldOneNewRecordOfEachTypeAtItsTemplateId() {
        final StreamRecord[] records = RecordType.newRecordsByTemplateId();
        int present = 0;
        for (int templateId = 0; templateId < records.length; templateId++) {
            final RecordType type = RecordType.forTemplateId(templateId);
            if (type == null) {
                assertNull(records[templateId]);
            } else {
                assertEquals(type, records[templateId].recordType);
                present++;
            }
        }
        assertEquals(RecordType.values().length, present);
        assertNotSame(
                records[RecordType.MBP_10.templateId()],
                RecordType.newRecordsByTemplateId()[RecordType.MBP_10.templateId()]);
    }

    @Test
    void coreTemplatesAreNotStreamRecords() {
        assertNull(RecordType.forTemplateId(new Intent().templateId()));
        assertNull(RecordType.forTemplateId(-1));
        assertNull(RecordType.forTemplateId(10_000));
    }

    @Test
    void copiesTheWholeRecord() {
        final Ohlcv1sSchema record = new Ohlcv1sSchema();
        record.encoder.streamId(7);
        record.encoder.open(100);
        record.encoder.high(20);
        record.encoder.low(40);
        record.encoder.close(60);
        record.encoder.volume(50);

        final Ohlcv1sSchema other = new Ohlcv1sSchema();
        other.copyFrom(record);

        assertEquals(7, other.streamId());
        assertEquals(100, other.decoder.open());
        assertEquals(20, other.decoder.high());
        assertEquals(40, other.decoder.low());
        assertEquals(60, other.decoder.close());
        assertEquals(50, other.decoder.volume());
    }
}
