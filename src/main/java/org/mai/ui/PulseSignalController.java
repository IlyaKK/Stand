package org.mai.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.mai.App;
import org.mai.data.DataControlImitator;
import org.mai.data.DataControlImitatorAdvance;
import org.mai.data.DataControlPosition;
import org.mai.domain.Uart;
import org.mai.domain.application_control.ListenerApplication;
import org.mai.domain.page_control.ListenerPulseSignal;
import org.mai.domain.uart_signal_control.ListenerControlUartPulseSignal;

import java.io.IOException;

public class PulseSignalController implements ListenerPulseSignal {
    public LineChart graphLinePulseSignal;
    public LineChart graphLineMoment;

    public TextField kpTextField;
    public TextField kiTextField;
    public TextField kdTextField;
    public TextField angleTextField;
    public TextField j_nTextField;
    public TextField k_vtTextField;
    public TextField k_shTextField;
    public TextField j_dv_imTextField;
    public TextField q_imTextField;
    public TextField ce_imTextField;
    public TextField kpImitatorTextField;
    public TextField kiImitatorTextField;
    public TextField kdImitatorTextField;

    public Button submitButton;
    public Button stopButton;
    public Button saveResultButton;

    public NumberAxis xAxis;
    public NumberAxis yAxis;
    public NumberAxis tMomentSignalAxis;
    public NumberAxis momentMomentSignalAxis;

    public AnchorPane loadAnchorPane;

    public XYChart.Series<Double, Double> measureAngleSeries = new XYChart.Series();
    public XYChart.Series<Double, Double> featuredAngleSeries = new XYChart.Series();

    public XYChart.Series<Double, Double> measureMomentSeries = new XYChart.Series();
    public XYChart.Series<Double, Double> referenceMomentSeries = new XYChart.Series();

    public CheckBox loadCheckBox;
    public ScrollPane loadScrollPane;

    private double angle = 0.0;
    private double kp = 0.0;
    private double ki = 0.0;
    private double kd = 0.0;
    private double kpIm = 0.0;
    private double kiIm = 0.0;
    private double kdIm = 0.0;
    private double j_n = 0.0;
    private double k_vt = 0.0;
    private double k_sh = 0.0;
    private double j_dv_im = 0.0;
    private double q_im = 0.0;
    private double ce_im = 0.0;
    private int loadSelected = 0;
    private final DataControlPosition dataControlPosition = new DataControlPosition();
    private final DataControlImitator dataControlImitator = new DataControlImitator();
    private final DataControlImitatorAdvance dataControlImitatorAdvance = new DataControlImitatorAdvance();
    private static ListenerControlUartPulseSignal listenerControlUartPulseSignal;
    private static ListenerApplication listenerApplication;

    static void initialiseListener(PrimaryController primaryController) {
        listenerControlUartPulseSignal = primaryController;
    }

    public static void initialiseApplicationListener(App app) {
        listenerApplication = app;
    }

    @FXML
    public void initialize() {
        Uart.setPulseListener(this);
        initialiseListenerLoadCheckBox();
        initializeGraphPulseSignal();
        initializeGraphMomentSignal();
        initializeListenerSubmitButton();
        initializeListenerStopButton();
        initializeListenerSaveResultButton();
    }

    private void initialiseListenerLoadCheckBox() {
        loadCheckBox.setOnAction(l -> {
            if (loadCheckBox.isSelected()) {
                loadSelected = 1;
            } else {
                loadSelected = 0;
            }
            loadScrollPane.setVisible(loadCheckBox.isSelected());
        });
    }

    private void initializeGraphPulseSignal() {
        graphLinePulseSignal.getStylesheets().add("org/mai/style_graph_angle.css");
        graphLinePulseSignal.getXAxis().setLabel("Время, t[c]");
        graphLinePulseSignal.getYAxis().setLabel("Угол, [градусы]");
        graphLinePulseSignal.setTitle("Реагирование системы на скачёк");
        graphLinePulseSignal.setCreateSymbols(true);
        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);
        measureAngleSeries.setName("Фактические данные");
        featuredAngleSeries.setName("Заданный угол");
        graphLinePulseSignal.getData().addAll(measureAngleSeries, featuredAngleSeries);
        XYChart.Data data = new XYChart.Data(0, 0);
        measureAngleSeries.getData().addAll(data);
        Node node = data.getNode();
        Tooltip tooltip = new Tooltip("Время: " + data.getXValue().toString() + ';' + "\n" + "Измеренный угол: " + data.getYValue().toString() + ';');
        Tooltip.install(node, tooltip);
    }

    private void initializeGraphMomentSignal() {
        graphLineMoment.getStylesheets().add("org/mai/style_graph_moment.css");
        graphLineMoment.getXAxis().setLabel("Время, t[c]");
        graphLineMoment.getYAxis().setLabel("Момент, [Нм]");
        graphLineMoment.setTitle("Изменение момента");
        graphLineMoment.setCreateSymbols(true);
        tMomentSignalAxis.setAutoRanging(true);
        momentMomentSignalAxis.setAutoRanging(true);
        measureMomentSeries.setName("Измеренный момент");
        referenceMomentSeries.setName("Расчитанный момент");
        graphLineMoment.getData().addAll(measureMomentSeries, referenceMomentSeries);
    }

    private void initializeListenerSubmitButton() {
        submitButton.setOnAction(l -> {
            if (!kpTextField.getText().isEmpty()) {
                kp = Double.parseDouble(kpTextField.getText());
            }

            if (!kiTextField.getText().isEmpty()) {
                ki = Double.parseDouble(kiTextField.getText());
            }

            if (!kdTextField.getText().isEmpty()) {
                kd = Double.parseDouble(kdTextField.getText());
            }

            if (!angleTextField.getText().isEmpty()) {
                angle = Double.parseDouble(angleTextField.getText());
            }

            if (loadCheckBox.isSelected()) {

                if (!j_nTextField.getText().isEmpty()) {
                    j_n = Double.parseDouble(j_nTextField.getText());
                }

                if (!k_vtTextField.getText().isEmpty()) {
                    k_vt = Double.parseDouble(k_vtTextField.getText());
                }

                if (!k_shTextField.getText().isEmpty()) {
                    k_sh = Double.parseDouble(k_shTextField.getText());
                }

                if (!j_dv_imTextField.getText().isEmpty()) {
                    j_dv_im = Double.parseDouble(j_dv_imTextField.getText());
                }

                if (!q_imTextField.getText().isEmpty()) {
                    q_im = Double.parseDouble(q_imTextField.getText());
                }

                if (!ce_imTextField.getText().isEmpty()) {
                    ce_im = Double.parseDouble(ce_imTextField.getText());
                }

                if (!kpImitatorTextField.getText().isEmpty()) {
                    kpIm = Double.parseDouble(kpImitatorTextField.getText());
                }

                if (!kiImitatorTextField.getText().isEmpty()) {
                    kiIm = Double.parseDouble(kiImitatorTextField.getText());
                }

                if (!kdImitatorTextField.getText().isEmpty()) {
                    kdIm = Double.parseDouble(kdImitatorTextField.getText());
                }
                dataControlImitator.setKp(kpIm);
                dataControlImitator.setKi(kiIm);
                dataControlImitator.setKd(kdIm);
                dataControlImitator.setJN(j_n);
                dataControlImitator.setKv(k_vt);
                dataControlImitatorAdvance.setKh(k_sh);
                dataControlImitatorAdvance.setJDv(j_dv_im);
                dataControlImitatorAdvance.setQ(q_im);
                dataControlImitatorAdvance.setCE(ce_im);
            }
            dataControlPosition.setLoadSelected(loadSelected);
            dataControlPosition.setKP(kp);
            dataControlPosition.setKI(ki);
            dataControlPosition.setKD(kd);
            dataControlPosition.setAngle(angle);
            listenerControlUartPulseSignal.transmitUartPulseSignalData(dataControlPosition, dataControlImitator, dataControlImitatorAdvance, loadSelected);

        });
    }

    private void initializeListenerStopButton() {
        stopButton.setOnAction(l -> {
            listenerControlUartPulseSignal.stopExperimentProcess();
        });
    }

    private void initializeListenerSaveResultButton() {
        saveResultButton.setOnAction(l -> {
            try {
                listenerApplication.doSnapShot();
            } catch (IOException e) {
                showAlert(e.toString());
            }
        });
    }

    @Override
    public void setNewPointOnGraph(Double t, Double angle, Double measureMoment, Double referenceMoment) {
        Platform.runLater(() -> {
            featuredAngleSeries.getData().addAll(new XYChart.Data(t, dataControlPosition.getAngle()));
            measureAngleSeries.getData().addAll(new XYChart.Data(t, angle));
            measureMomentSeries.getData().addAll(new XYChart.Data(t, measureMoment));
            referenceMomentSeries.getData().addAll(new XYChart.Data(t, referenceMoment));
        });
    }

    @Override
    public void showAlert(String error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Не удалось прочитать значение из COM PORT");
        alert.setContentText(error);
        alert.showAndWait();
    }
}
