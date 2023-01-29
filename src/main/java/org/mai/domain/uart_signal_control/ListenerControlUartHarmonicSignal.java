package org.mai.domain.uart_signal_control;

import org.mai.data.DataControlSinusPosition;

public interface ListenerControlUartHarmonicSignal {
    void transmitUartHarmonicSignalData(DataControlSinusPosition dataControlPosition);
    void stopExperimentProcess();
}
