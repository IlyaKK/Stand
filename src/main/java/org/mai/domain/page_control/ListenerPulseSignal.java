package org.mai.domain.page_control;

public interface ListenerPulseSignal {
    void setNewPointOnGraph(Double t, Double angle, Double measureMoment, Double referenceMoment);
    void showAlert(String error);
}
