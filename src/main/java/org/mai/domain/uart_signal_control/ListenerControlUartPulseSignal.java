package org.mai.domain.uart_signal_control;

import org.mai.data.DataControlImitator;
import org.mai.data.DataControlImitatorAdvance;
import org.mai.data.DataControlPosition;

public interface ListenerControlUartPulseSignal {
    void transmitUartPulseSignalData(DataControlPosition dataControlPosition, DataControlImitator dataControlImitator, DataControlImitatorAdvance dataControlImitatorAdvance, int loadSelected);
    void stopExperimentProcess();
}
