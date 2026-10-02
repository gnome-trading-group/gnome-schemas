package group.gnometrading.schemas.migration;

import group.gnometrading.schemas.MessageHeaderDecoder;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * A planned conversion of one message template: a header rewrite plus a flat list of field steps that
 * together write every byte of the target block, so nothing is cleared first.
 */
final class CompiledConverter implements MessageConverter {

    private static final int HEADER_LENGTH = MessageHeaderDecoder.ENCODED_LENGTH;
    private static final int TEMPLATE_ID_OFFSET = 2;
    private static final int SCHEMA_ID_OFFSET = 4;
    private static final int VERSION_OFFSET = 6;

    private final int templateId;
    private final int schemaId;
    private final int targetVersion;
    private final int sourceBlockLength;
    private final int targetBlockLength;
    private final FieldOps.FieldOp[] ops;

    CompiledConverter(
            final int templateId,
            final int schemaId,
            final int targetVersion,
            final int sourceBlockLength,
            final int targetBlockLength,
            final FieldOps.FieldOp[] ops) {
        this.templateId = templateId;
        this.schemaId = schemaId;
        this.targetVersion = targetVersion;
        this.sourceBlockLength = sourceBlockLength;
        this.targetBlockLength = targetBlockLength;
        this.ops = ops;
    }

    @Override
    public int sourceBlockLength() {
        return this.sourceBlockLength;
    }

    @Override
    public int targetLength() {
        return HEADER_LENGTH + this.targetBlockLength;
    }

    @Override
    public int convert(
            final DirectBuffer src, final int srcOffset, final MutableDirectBuffer dst, final int dstOffset) {
        dst.putShort(dstOffset, (short) this.targetBlockLength, Scalars.ORDER);
        dst.putShort(dstOffset + TEMPLATE_ID_OFFSET, (short) this.templateId, Scalars.ORDER);
        dst.putShort(dstOffset + SCHEMA_ID_OFFSET, (short) this.schemaId, Scalars.ORDER);
        dst.putShort(dstOffset + VERSION_OFFSET, (short) this.targetVersion, Scalars.ORDER);
        final int srcBody = srcOffset + HEADER_LENGTH;
        final int dstBody = dstOffset + HEADER_LENGTH;
        for (final FieldOps.FieldOp op : this.ops) {
            op.apply(src, srcBody, dst, dstBody);
        }
        return targetLength();
    }

    int opCount() {
        return this.ops.length;
    }
}
