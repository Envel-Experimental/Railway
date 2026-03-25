package com.railwayteam.railways.mixin_interfaces;

import com.railwayteam.railways.compat.journeymap.TrainMarkerData;
import org.jetbrains.annotations.Nullable;

public interface IMarkerTrackableTrain {
    @Nullable
    TrainMarkerData railways$getLastSentMarkerData();
    void railways$setLastSentMarkerData(@Nullable TrainMarkerData data);
}
