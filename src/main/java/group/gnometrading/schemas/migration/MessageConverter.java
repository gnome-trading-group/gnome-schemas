package group.gnometrading.schemas.migration;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

/** Rewrites one SBE message, header included, from one schema version to the next. */
interface MessageConverter {

    /** Block length a message of this template has in the source version. */
    int sourceBlockLength();

    /** Bytes {@link #convert} writes, header included; 0 if the message is dropped. */
    int targetLength();

    /**
     * @return the number of bytes written to {@code dst}, header included; 0 if the message is dropped
     */
    int convert(DirectBuffer src, int srcOffset, MutableDirectBuffer dst, int dstOffset);
}
