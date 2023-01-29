package org.mai.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataControlSinusPosition {
    double kP;
    double kI;
    double kD;
    double frequencySinus;
    double amplitudeSinus;
}
