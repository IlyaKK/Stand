package org.mai.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import org.mai.data.DataControlImitator;
import org.mai.data.DataControlImitatorAdvance;
import org.mai.data.DataControlPosition;
import org.mai.data.DataControlSinusPosition;
import org.mai.domain.*;
import org.mai.domain.uart_signal_control.ListenerControlUartHarmonicSignal;
import org.mai.domain.uart_signal_control.ListenerControlUartPulseSignal;

import java.util.Arrays;

public class PrimaryController implements ListenerControlUartPulseSignal, ListenerControlUartHarmonicSignal {
    public ComboBox<String> listCOMPortsComboBox;

    public Button searchComPortsButton;
    public Tab pulseSignalTab;
    public Tab harmonicSignalTab;

    private Uart uart;

    @FXML
    public void initialize() {
        initializeCOMPorts();
        initializeListenerSelectCOMPortsComboBox();
        initializeListenerSearchCOMPortsButton();
        PulseSignalController.initialiseListener(this);
        HarmonicSignalController.initialiseListener(this);
    }

    private void initializeCOMPorts() {
        uart = new Uart();
        setListComPorts();
    }

    private void initializeListenerSelectCOMPortsComboBox() {
        listCOMPortsComboBox.setOnAction(l -> {
            try {
                if(listCOMPortsComboBox.getValue() != null){
                    uart.setComPort((listCOMPortsComboBox.getValue()));
                }
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Не удалось подключиться к COM порту");
                alert.setContentText(ex.toString());
                alert.showAndWait();
            }
        });
    }

    private void initializeListenerSearchCOMPortsButton() {
        searchComPortsButton.setOnAction(l -> {
            setListComPorts();
        });
    }

    private void setListComPorts(){
        ObservableList<String> comPorts = getSystemCOMPorts();
        listCOMPortsComboBox.setItems(comPorts);
    }

    private ObservableList<String> getSystemCOMPorts() {
        ObservableList<String> comPorts = FXCollections.observableArrayList();
        String[] ports = uart.getSystemComPorts();
        comPorts.addAll(Arrays.asList(ports));
        return comPorts;
    }

    @Override
    public void transmitUartPulseSignalData(DataControlPosition dataControlPosition, DataControlImitator dataControlImitator, DataControlImitatorAdvance dataControlImitatorAdvance, int loadSelected) {
        uart.transmitJsonPID(dataControlPosition, dataControlImitator, dataControlImitatorAdvance, loadSelected);
    }

    @Override
    public void transmitUartHarmonicSignalData(DataControlSinusPosition dataControlSinusPosition) {
        uart.transmitJsonSinusPID(dataControlSinusPosition);
    }

    @Override
    public void stopExperimentProcess() {
        uart.stopExperimentProcess();
    }
}
