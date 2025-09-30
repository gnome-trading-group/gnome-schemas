package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.Schema;
import org.agrona.concurrent.EpochClock;


public abstract class SamplingSchemaConverter<I extends Schema, O extends Schema> implements SchemaConverter<I, O> {

    private final EpochClock clock;
    private final long sampleIntervalMillis;

    private long lastSampleTimeMillis, nextSampleTimeMillis;
    private boolean ignoreFirstInterval;

    public SamplingSchemaConverter(EpochClock clock, long sampleIntervalMillis) {
        this.clock = clock;
        this.sampleIntervalMillis = sampleIntervalMillis;
        this.lastSampleTimeMillis = this.nextSampleTimeMillis = -1;
        this.ignoreFirstInterval = true;
    }

    private long getNextInterval() {
        long now = this.clock.time();
        return now + (this.sampleIntervalMillis - (now % this.sampleIntervalMillis));
    }

    /**
     * @return the start (inclusive) of the last interval sampled
     */
    public long getLastSampleTimeMillis() {
        return this.lastSampleTimeMillis;
    }

    @Override
    public O convert(I source) {
        long now = this.clock.time();
        if (this.nextSampleTimeMillis == -1) {
            this.nextSampleTimeMillis = this.getNextInterval();
            updateState(source);
            return null;
        }

        if (now < this.nextSampleTimeMillis) {
            updateState(source);
            return null;
        }

        this.lastSampleTimeMillis = this.nextSampleTimeMillis - this.sampleIntervalMillis;
        this.nextSampleTimeMillis = this.getNextInterval();
        final O result = sample();
        updateState(source);

        if (this.ignoreFirstInterval) {
            this.ignoreFirstInterval = false;
            return null;
        }
        return result;
    }

    protected abstract O sample();

    protected abstract void updateState(I source);
}
