package group.gnometrading.schemas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import group.gnometrading.schemas.book.Bbo1mDecoder;
import group.gnometrading.schemas.book.Bbo1sDecoder;
import group.gnometrading.schemas.book.MboDecoder;
import group.gnometrading.schemas.book.Mbp10Decoder;
import group.gnometrading.schemas.book.Mbp1Decoder;
import group.gnometrading.schemas.book.Ohlcv1hDecoder;
import group.gnometrading.schemas.book.Ohlcv1mDecoder;
import group.gnometrading.schemas.book.Ohlcv1sDecoder;
import group.gnometrading.schemas.book.TradesDecoder;
import org.junit.jupiter.api.Test;

class MessageSchemaTest {

    private static final int[] CORE_TEMPLATES = {
        OrderExecutionReportDecoder.TEMPLATE_ID,
        OrderDecoder.TEMPLATE_ID,
        ModifyOrderDecoder.TEMPLATE_ID,
        CancelOrderDecoder.TEMPLATE_ID,
        IntentDecoder.TEMPLATE_ID,
        MetricRowDecoder.TEMPLATE_ID,
        MetricStreamDecoder.TEMPLATE_ID,
        MetricColumnDecoder.TEMPLATE_ID,
        ShardStatusDecoder.TEMPLATE_ID,
    };

    private static final int[] BOOK_TEMPLATES = {
        MboDecoder.TEMPLATE_ID,
        Mbp10Decoder.TEMPLATE_ID,
        Mbp1Decoder.TEMPLATE_ID,
        Bbo1sDecoder.TEMPLATE_ID,
        Bbo1mDecoder.TEMPLATE_ID,
        TradesDecoder.TEMPLATE_ID,
        Ohlcv1sDecoder.TEMPLATE_ID,
        Ohlcv1mDecoder.TEMPLATE_ID,
        Ohlcv1hDecoder.TEMPLATE_ID,
    };

    @Test
    void everyCoreMessageSitsInCoresBlock() {
        for (final int templateId : CORE_TEMPLATES) {
            assertEquals(MessageSchema.CORE, MessageSchema.forTemplateId(templateId), "template " + templateId);
        }
        assertEquals(MessageSchema.CORE.schemaId(), MessageHeaderDecoder.SCHEMA_ID);
    }

    @Test
    void everyBookRecordSitsInBooksBlock() {
        for (final int templateId : BOOK_TEMPLATES) {
            assertEquals(MessageSchema.BOOK, MessageSchema.forTemplateId(templateId), "template " + templateId);
        }
        assertEquals(MessageSchema.BOOK.schemaId(), group.gnometrading.schemas.book.MessageHeaderDecoder.SCHEMA_ID);
    }

    @Test
    void blocksAreContiguousInOrderAndSchemaIdsDistinct() {
        final MessageSchema[] schemas = MessageSchema.values();
        for (int i = 1; i < schemas.length; i++) {
            final MessageSchema previous = schemas[i - 1];
            assertEquals(
                    previous.firstTemplateId() + previous.templateCount(),
                    schemas[i].firstTemplateId(),
                    schemas[i].name());
            for (int j = 0; j < i; j++) {
                assertTrue(schemas[i].schemaId() != schemas[j].schemaId(), schemas[i] + " and " + schemas[j]);
            }
        }
    }

    @Test
    void theGatewayAndEsportsBlocksAreReserved() {
        assertEquals(MessageSchema.GATEWAY, MessageSchema.forTemplateId(200));
        assertEquals(MessageSchema.GATEWAY, MessageSchema.forTemplateId(299));
        assertEquals(3, MessageSchema.GATEWAY.schemaId());
        assertEquals(MessageSchema.ESPORTS, MessageSchema.forTemplateId(300));
        assertEquals(MessageSchema.ESPORTS, MessageSchema.forTemplateId(599));
        assertEquals(4, MessageSchema.ESPORTS.schemaId());
    }

    @Test
    void templateIdsOutsideEveryBlockHaveNoSchema() {
        assertNull(MessageSchema.forTemplateId(-1));
        assertNull(MessageSchema.forTemplateId(600));
    }

    @Test
    void theBookFamilyAgreesWithItsSchema() {
        assertEquals(MessageSchema.BOOK.schemaId(), Family.BOOK.schemaId());
        for (final int templateId : BOOK_TEMPLATES) {
            assertTrue(Family.BOOK.owns(templateId));
        }
    }
}
