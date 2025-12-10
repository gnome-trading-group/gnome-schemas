package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.Schema;

import java.time.Duration;


public abstract class SamplingSchemaConverter<I extends Schema, O extends Schema> implements SchemaConverter<I, O> {

    private final long sampleIntervalNanos;

    private long lastSampleTimeNanos, nextSampleTimeNanos;

    public SamplingSchemaConverter(Duration sampleInterval) {
        this.sampleIntervalNanos = sampleInterval.toNanos();
        this.lastSampleTimeNanos = this.nextSampleTimeNanos = -1;
    }

    private long getNextInterval(long eventTimestampNanos) {
        return eventTimestampNanos + (this.sampleIntervalNanos - (eventTimestampNanos % this.sampleIntervalNanos));
    }

    /**
     * @return the start (inclusive) of the last interval sampled, in nanoseconds
     */
    public long getLastSampleTimeNanos() {
        return this.lastSampleTimeNanos;
    }

    @Override
    public O convert(I source) {
        long eventTimestampNanos = source.getEventTimestamp();
        if (this.nextSampleTimeNanos == -1) {
            this.nextSampleTimeNanos = this.getNextInterval(eventTimestampNanos);
            updateState(source);
            return null;
        }

        if (eventTimestampNanos < this.nextSampleTimeNanos) {
            updateState(source);
            return null;
        }

        this.lastSampleTimeNanos = this.nextSampleTimeNanos - this.sampleIntervalNanos;
        this.nextSampleTimeNanos = this.getNextInterval(eventTimestampNanos);
        final O result = sample();
        updateState(source);
        return result;
    }

    protected abstract O sample();

    protected abstract void updateState(I source);
}
