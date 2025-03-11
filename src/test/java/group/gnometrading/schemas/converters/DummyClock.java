package group.gnometrading.schemas.converters;

import org.agrona.concurrent.EpochClock;

public class DummyClock implements EpochClock {

    public long time = 0;

    @Override
    public long time() {
        return time;
    }
}
