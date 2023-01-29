package org.mai.domain.page_control;

public interface ListenerHarmonicSignal {
    void setNewPointsOnGraph(Double t, Double angle, Double sinusAngle);
    void showAlert(String error);
}
