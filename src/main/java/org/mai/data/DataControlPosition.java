package org.mai.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataControlPosition {
    double kP;
    double kI;
    double kD;
    double angle;
    int loadSelected;
}
