package org.mai.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.mai.App;
import org.mai.data.DataControlSinusPosition;
import org.mai.domain.Uart;
import org.mai.domain.application_control.ListenerApplication;
import org.mai.domain.page_control.ListenerHarmonicSignal;
import org.mai.domain.uart_signal_control.ListenerControlUartHarmonicSignal;

public class HarmonicSignalController implements ListenerHarmonicSignal {

    public TextField frequencyTextField;
    public TextField amplitudeTextField;
    public TextField sinusKpTextField;
    public TextField sinusKiTextField;
    public TextField sinusKdTextField;
    public Button sinusSubmitButton;
    public Button sinusStopButton;
    public NumberAxis xAxis1;
    public NumberAxis yAxis1;
    public LineChart sinusGraphLine;

    public XYChart.Series<Double, Double> measureAngleSeries = new XYChart.Series();
    public XYChart.Series<Double, Double> sinusAngleSeries = new XYChart.Series();

    private static ListenerControlUartHarmonicSignal listenerControlUartHarmonicSignal;
    private static ListenerApplication listenerApplication;

    private final DataControlSinusPosition dataControlSinusPosition = new DataControlSinusPosition();

    private double amplitude = 0.0, frequency = 0.0, kp = 0.0, ki = 0.0, kd = 0.0;

    public static void initialiseListener(PrimaryController primaryController) {
        listenerControlUartHarmonicSignal = (ListenerControlUartHarmonicSignal) primaryController;
    }

    @FXML
    public void initialize() {
        initializeGraph();
        Uart.setHarmonicListener(this);
    }

    public static void initialiseApplicationListener(App app){
        listenerApplication = (ListenerApplication) app;
    }

    private void initializeGraph() {
        sinusGraphLine.getStylesheets().add("org/mai/style_graph_angle.css");
        sinusGraphLine.getXAxis().setLabel("Время, t[c]");
        sinusGraphLine.getYAxis().setLabel("Угол, [градусы]");
        sinusGraphLine.setTitle("Реагирование системы на синусоиду");
        sinusGraphLine.setCreateSymbols(false);
        xAxis1.setAutoRanging(true);
        yAxis1.setAutoRanging(true);

        measureAngleSeries.setName("Фактические данные");
        measureAngleSeries.getData().add(new XYChart.Data(0, 0));

        sinusAngleSeries.setName("Гармонический сигнал");
        sinusGraphLine.getData().addAll(measureAngleSeries, sinusAngleSeries);
    }

    public void transmitSinusData(ActionEvent actionEvent) {
        if (!sinusKpTextField.getText().isEmpty()) {
            kp = Double.parseDouble(sinusKpTextField.getText());
        }

        if (!sinusKiTextField.getText().isEmpty()) {
            ki = Double.parseDouble(sinusKiTextField.getText());
        }

        if (!sinusKdTextField.getText().isEmpty()) {
            kd = Double.parseDouble(sinusKdTextField.getText());
        }

        if (!amplitudeTextField.getText().isEmpty()) {
            amplitude = Double.parseDouble(amplitudeTextField.getText());
        }

        if (!frequencyTextField.getText().isEmpty()) {
            frequency = Double.parseDouble(frequencyTextField.getText());
        }

        dataControlSinusPosition.setKP(kp);
        dataControlSinusPosition.setKI(ki);
        dataControlSinusPosition.setKD(kd);
        dataControlSinusPosition.setAmplitudeSinus(amplitude);
        dataControlSinusPosition.setFrequencySinus(frequency);
        listenerControlUartHarmonicSignal.transmitUartHarmonicSignalData(dataControlSinusPosition);
    }

    @Override
    public void setNewPointsOnGraph(Double t, Double angle, Double sinusAngle) {
        Platform.runLater(() -> {
            sinusAngleSeries.getData().addAll(new XYChart.Data(t, sinusAngle));
            measureAngleSeries.getData().addAll(new XYChart.Data(t, angle));
        });
    }

    @Override
    public void showAlert(String error) {

    }

    public void stopExperimentProcess(ActionEvent actionEvent) {
        listenerControlUartHarmonicSignal.stopExperimentProcess();
    }

    public void saveHarmonicExperiment(ActionEvent actionEvent) {
    }
}
