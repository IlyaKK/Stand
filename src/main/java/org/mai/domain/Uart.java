package org.mai.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import jssc.*;
import org.json.JSONObject;
import org.mai.data.DataControlImitator;
import org.mai.data.DataControlImitatorAdvance;
import org.mai.data.DataControlPosition;
import org.mai.data.DataControlSinusPosition;
import org.mai.domain.page_control.ListenerHarmonicSignal;
import org.mai.domain.page_control.ListenerPulseSignal;
import org.mai.ui.HarmonicSignalController;
import org.mai.ui.PulseSignalController;

import java.io.*;
import java.util.concurrent.atomic.AtomicReference;

public class Uart {
    private static SerialPort serialPort;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private StringWriter stringWriterDataControlPosition = new StringWriter();
    private StringWriter stringWriterDataControlImitator = new StringWriter();
    private StringWriter stringWriterDataControlImitatorAdvance = new StringWriter();
    private StringWriter stringWriterDataControlSinusPosition = new StringWriter();
    private UartTransmittedState uartTransmittedState = UartTransmittedState.TRANSMIT_COMMAND;
    private UartReceivedState uartReceivedState = UartReceivedState.RECEIVE_COMMAND_SEND_SIZE_PID_DATA;
    private static ListenerPulseSignal listenerPulseSignal;
    private static ListenerHarmonicSignal listenerHarmonicSignal;
    private String command[] = {"0", "0"};
    int sizePointAngle = 0;
    int sizePointAngleSinus = 0;
    public static FileWriter fileWriter;
    public static BufferedWriter bufferedWriter;
    private boolean enableLog = true;
    private boolean enableFileLog = true;


    public Uart() {
        try {
            fileWriter = new FileWriter("1.txt", true);
            bufferedWriter = new BufferedWriter(fileWriter);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setPulseListener(PulseSignalController pulseSignalController) {
        listenerPulseSignal = pulseSignalController;
    }

    public static void setHarmonicListener(HarmonicSignalController harmonicSignalController) {
        listenerHarmonicSignal = harmonicSignalController;
    }

    public void setComPort(String nameComPort) throws SerialPortException {
        serialPort = new SerialPort(nameComPort);
        serialPort.openPort();
        serialPort.setParams(SerialPort.BAUDRATE_115200,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE,
                false,
                false);
        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
    }

    private void sendToConsole(int line, String action, String value){
        System.out.println(line);
        System.out.println(action + ": "  + value);
    }
    private void writeToFile(int line, String action, String value){
        try{
            bufferedWriter.newLine();
            bufferedWriter.write(line + " " + action + ": "  + value);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
        Т.к. основной поток программы не завершается из-за листнера,
        который слушает входящий порт, то создаём функцию, которая
        закроет этот порт для корректного завершения
     */
    public static void closePort(){
        try {
            bufferedWriter.newLine();
            bufferedWriter.write("----------------------------------------------------");
            bufferedWriter.close();
            serialPort.closePort();
        } catch (SerialPortException | IOException e) {
            e.printStackTrace();
        }
    }

    public void transmitJsonPID(DataControlPosition dataControlPosition, DataControlImitator dataControlImitator, DataControlImitatorAdvance dataControlImitatorAdvance, int loadSelected) {
        AtomicReference<String> jsonControlPositionData = new AtomicReference<>();
        AtomicReference<String> jsonImitator = new AtomicReference<>();
        AtomicReference<String> jsonImitatorAdvance = new AtomicReference<>();

        Thread uartThread = new Thread(() -> {
            try {
                objectMapper.writeValue(stringWriterDataControlPosition, dataControlPosition);
                objectMapper.writeValue(stringWriterDataControlImitator, dataControlImitator);
                objectMapper.writeValue(stringWriterDataControlImitatorAdvance, dataControlImitatorAdvance);
                jsonControlPositionData.set(stringWriterDataControlPosition.toString());
                if (loadSelected == 1) {
                    jsonImitator.set(stringWriterDataControlImitator.toString());
                    jsonImitatorAdvance.set(stringWriterDataControlImitatorAdvance.toString());
                }
                int flagTransmitted = 0;
                command[0] = "1";
                command[1] = "0";
                uartTransmittedState = UartTransmittedState.TRANSMIT_COMMAND;
                while (uartTransmittedState != UartTransmittedState.END_TRANSMIT_IMPULSE_DATA) {
                    switch (uartTransmittedState) {
                        case TRANSMIT_COMMAND:
                            if (flagTransmitted == 0) {
                                uartReceivedState = UartReceivedState.RECEIVE_COMMAND_SEND_SIZE_PID_DATA;
                                serialPort.writeString(command[0] + command[1]);
                                if (enableLog){
                                    sendToConsole(108, "write", command[0] + command[1]);
                                }
                                if (enableFileLog){
                                    writeToFile(108, "write", command[0] + command[1]);
                                }
                                flagTransmitted = 1;
                            }
                            break;

                        case TRANSMIT_SIZE_PID_DATA:
                            String sizeStringPid = String.valueOf(jsonControlPositionData.get().length());
                            if (flagTransmitted == 1) {
                                uartReceivedState = UartReceivedState.RECEIVE_COMMAND_SEND_PID_DATA;
                                serialPort.writeString(sizeStringPid);
                                if (enableLog){
                                    sendToConsole(134, "write", sizeStringPid);
                                }
                                if (enableFileLog){
                                    writeToFile(134, "write", sizeStringPid);
                                }
                                flagTransmitted = 2;
                            }
                            break;

                        case TRANSMIT_PID_DATA:
                            if (flagTransmitted == 2) {
                                if (loadSelected == 1) {
                                    uartReceivedState = UartReceivedState.RECEIVED_COMMAND_SEND_SIZE_IMITATOR_DATA;
                                } else {
                                    uartReceivedState = UartReceivedState.RECEIVED_COMMAND_END_TRANSMIT;
                                }
                                serialPort.writeString(jsonControlPositionData.get());
                                if (enableLog){
                                    sendToConsole(152, "write", jsonControlPositionData.get());
                                }
                                if (enableFileLog){
                                    writeToFile(152, "write", jsonControlPositionData.get());
                                }
                                flagTransmitted = 3;
                            }
                            break;

                        case TRANSMIT_SIZE_IMITATOR_DATA:
                            String sizeStringImitator = String.valueOf(jsonImitator.get().length());
                            if (flagTransmitted == 3) {
                                uartReceivedState = UartReceivedState.RECEIVE_COMMAND_SEND_IMITATOR_DATA;
                                serialPort.writeString(sizeStringImitator);
                                if (enableLog){
                                    sendToConsole(166, "write", sizeStringImitator);
                                }
                                if (enableFileLog){
                                    writeToFile(166, "write", sizeStringImitator);
                                }
                                flagTransmitted = 4;
                            }
                            break;

                        case TRANSMIT_IMITATOR_DATA:
                            if (flagTransmitted == 4) {
                                uartReceivedState = UartReceivedState.RECEIVED_COMMAND_SEND_SIZE_IMITATOR_DATA_ADVANCE;
                                serialPort.writeString(jsonImitator.get());
                                if (enableLog){
                                    sendToConsole(181, "write", jsonImitator.get());
                                }
                                if (enableFileLog){
                                    writeToFile(181, "write", jsonImitator.get());
                                }
                                flagTransmitted = 5;
                            }
                            break;

                        case TRANSMIT_SIZE_IMITATOR_DATA_ADVANCE:
                            String sizeStringImitatorAdvance = String.valueOf(jsonImitatorAdvance.get().length());
                            if (flagTransmitted == 5) {
                                uartReceivedState = UartReceivedState.RECEIVE_COMMAND_SEND_IMITATOR_DATA_ADVANCE;
                                serialPort.writeString(sizeStringImitatorAdvance);
                                if (enableLog){
                                    sendToConsole(196, "write", sizeStringImitatorAdvance);
                                }
                                if (enableFileLog){
                                    writeToFile(196, "write", sizeStringImitatorAdvance);
                                }
                                flagTransmitted = 6;
                            }
                            break;

                        case TRANSMIT_IMITATOR_DATA_ADVANCE:
                            if (flagTransmitted == 6) {
                                uartReceivedState = UartReceivedState.RECEIVED_COMMAND_END_TRANSMIT;
                                serialPort.writeString(jsonImitatorAdvance.get());
                                if (enableLog){
                                    sendToConsole(210, "write", jsonImitatorAdvance.get());
                                }
                                if (enableFileLog){
                                    writeToFile(210, "write", jsonImitatorAdvance.get());
                                }
                                flagTransmitted = 7;
                            }
                            break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        uartThread.setDaemon(true);
        uartThread.start();
    }

    public void transmitJsonSinusPID(DataControlSinusPosition dataControlSinusPosition) {
        AtomicReference<String> jsonControlSinusPosition = new AtomicReference<>();
        Thread uartThread = new Thread(() -> {
            try {
                objectMapper.writeValue(stringWriterDataControlSinusPosition, dataControlSinusPosition);
                jsonControlSinusPosition.set(stringWriterDataControlSinusPosition.toString());
                int flagTransmitted = 0;
                uartTransmittedState = UartTransmittedState.TRANSMIT_COMMAND;
                while (uartTransmittedState != UartTransmittedState.END_TRANSMIT_SINUS) {
                    switch (uartTransmittedState) {
                        case TRANSMIT_COMMAND:
                            command[0] = "4";
                            if (flagTransmitted == 0) {
                                uartReceivedState = UartReceivedState.RECEIVE_COMMAND_SEND_SIZE_SINUS_PID_DATA;
                                serialPort.writeString(command[0] + command[1]);
                                if (enableLog){
                                    sendToConsole(248, "write", command[0] + command[1]);
                                }
                                if (enableFileLog){
                                    writeToFile(248, "write", command[0] + command[1]);
                                }
                                flagTransmitted = 1;
                            }
                            break;

                        case TRANSMIT_SIZE_SINUS_PID_DATA:
                            String sizeStringPid = String.valueOf(jsonControlSinusPosition.get().length());
                            if (flagTransmitted == 1) {
                                uartReceivedState = UartReceivedState.RECEIVE_COMMAND_SEND_SINUS_PID_DATA;
                                serialPort.writeString(sizeStringPid);
                                if (enableLog){
                                    sendToConsole(263, "write", sizeStringPid);
                                }
                                if (enableFileLog){
                                    writeToFile(263, "write", sizeStringPid);
                                }
                                flagTransmitted = 2;
                            }
                            break;

                        case TRANSMIT_SINUS_PID_DATA:
                            if (flagTransmitted == 2) {
                                uartReceivedState = UartReceivedState.RECEIVED_COMMAND_END_TRANSMIT_SINUS;
                                serialPort.writeString(jsonControlSinusPosition.get());
                                if (enableLog){
                                    sendToConsole(277, "write", jsonControlSinusPosition.get());
                                }
                                if (enableFileLog){
                                    writeToFile(277, "write", jsonControlSinusPosition.get());
                                }
                                flagTransmitted = 3;
                            }
                            break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        uartThread.setDaemon(true);
        uartThread.start();
    }

    public void stopExperimentProcess() {
        command[1] = "7";
    }

    private class PortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            try {
                transactionReceiveImpulseData();
                transactionReceiveHarmonicData();
                transactionReceiveImitatorData();
                transactionReceiveImpulsePoints();
                transactionReceiveHarmonicPoints();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private void transactionReceiveImpulseData() throws SerialPortException {
            String data;
            switch (uartReceivedState) {
                case RECEIVE_COMMAND_SEND_SIZE_PID_DATA:
                    data = serialPort.readString(13);
                    if (enableLog){
                        sendToConsole(320, "read", data);
                    }
                    if (enableFileLog){
                        writeToFile(320, "read", data);
                    }
                    if (data.equals("Send size pid")) {
                        uartTransmittedState = UartTransmittedState.TRANSMIT_SIZE_PID_DATA;
                    }
                    break;

                case RECEIVE_COMMAND_SEND_PID_DATA:
                    data = serialPort.readString(8);
                    if (enableLog){
                        sendToConsole(333, "read", data);
                    }
                    if (enableFileLog){
                        writeToFile(333, "read", data);
                    }
                    if (data.equals("Send pid")) {
                        uartTransmittedState = UartTransmittedState.TRANSMIT_PID_DATA;
                    }
                    break;

                case RECEIVED_COMMAND_END_TRANSMIT:
                    data = serialPort.readString(12);
                    if (enableLog){
                        sendToConsole(346, "read", data);
                    }
                    if (enableFileLog){
                        writeToFile(346, "read", data);
                    }
                    if (data.equals("End Transmit")) {
                        uartTransmittedState = UartTransmittedState.END_TRANSMIT_IMPULSE_DATA;
                        command[0] = "2";
                        uartReceivedState = UartReceivedState.RECEIVE_SIZE_ANGLE_POINT;
                        serialPort.writeString(command[0] + command[1]);
                        if (enableLog){
                            sendToConsole(357, "write", command[0] + command[1]);
                        }
                        if (enableFileLog){
                            writeToFile(357, "write", command[0] + command[1]);
                        }
                    }
                    break;

            }
        }

        private void transactionReceiveHarmonicData() throws SerialPortException {
            String data;
            switch (uartReceivedState) {
                case RECEIVE_COMMAND_SEND_SIZE_SINUS_PID_DATA:
                    data = serialPort.readString(19);
                    if (enableLog){
                        sendToConsole(374, "read", data);
                    }
                    if (enableFileLog){
                        writeToFile(374, "read", data);
                    }
                    if (data.equals("Send size sinus pid")) {
                        uartTransmittedState = UartTransmittedState.TRANSMIT_SIZE_SINUS_PID_DATA;
                    }
                    break;

                case RECEIVE_COMMAND_SEND_SINUS_PID_DATA:
                    data = serialPort.readString(14);
                    if (enableLog){
                        sendToConsole(387, "read", data);
                    }
                    if (enableFileLog){
                        writeToFile(387, "read", data);
                    }
                    if (data.equals("Send sinus pid")) {
                        uartTransmittedState = UartTransmittedState.TRANSMIT_SINUS_PID_DATA;
                    }
                    break;

                case RECEIVED_COMMAND_END_TRANSMIT_SINUS:
                    data = serialPort.readString(18);
                    if (enableLog){
                        sendToConsole(400, "read", data);
                    }
                    if (enableFileLog){
                        writeToFile(400, "read", data);
                    }
                    if (data.equals("End Transmit sinus")) {
                        uartTransmittedState = UartTransmittedState.END_TRANSMIT_SINUS;
                        command[0] = "5";
                        uartReceivedState = UartReceivedState.RECEIVE_SIZE_ANGLE_SINUS_POINTS;
                        serialPort.writeString(command[0] + command[1]);
                        if (enableLog){
                            sendToConsole(411, "write", command[0] + command[1]);
                        }
                        if (enableFileLog){
                            writeToFile(411, "write", command[0] + command[1]);
                        }
                    }
                    break;
            }
        }

        private void transactionReceiveImitatorData() throws SerialPortException {
            String data;
            switch (uartReceivedState) {
                case RECEIVED_COMMAND_SEND_SIZE_IMITATOR_DATA:
                    data = serialPort.readString(18);
                    if (enableLog){
                        sendToConsole(427, "read", data);
                    }
                    if (enableFileLog){
                        writeToFile(427, "read", data);
                    }
                    if (data.equals("Send size imitator")) {
                        uartTransmittedState = UartTransmittedState.TRANSMIT_SIZE_IMITATOR_DATA;
                    }
                    break;

                case RECEIVE_COMMAND_SEND_IMITATOR_DATA:
                    data = serialPort.readString(13);
                    if (enableLog){
                        sendToConsole(440, "read", data);
                    }
                    if (enableFileLog){
                        writeToFile(440, "read", data);
                    }
                    if (data.equals("Send imitator")) {
                        uartTransmittedState = UartTransmittedState.TRANSMIT_IMITATOR_DATA;
                    }
                    break;

                case RECEIVED_COMMAND_SEND_SIZE_IMITATOR_DATA_ADVANCE:
                    data = serialPort.readString(26);
                    if (enableLog){
                        sendToConsole(453, "read", data);
                    }
                    if (enableFileLog){
                        writeToFile(453, "read", data);
                    }
                    if (data.equals("Send size imitator advance")) {
                        uartTransmittedState = UartTransmittedState.TRANSMIT_SIZE_IMITATOR_DATA_ADVANCE;
                    }
                    break;

                case RECEIVE_COMMAND_SEND_IMITATOR_DATA_ADVANCE:
                    data = serialPort.readString(21);
                    if (enableLog){
                        sendToConsole(466, "read", data);
                    }
                    if (enableFileLog){
                        writeToFile(466, "read", data);
                    }
                    if (data.equals("Send imitator advance")) {
                        uartTransmittedState = UartTransmittedState.TRANSMIT_IMITATOR_DATA_ADVANCE;
                    }
                    break;
            }
        }

        private void transactionReceiveImpulsePoints() throws SerialPortException {
            String data;
            switch (uartReceivedState) {
                case RECEIVE_SIZE_ANGLE_POINT:
                    data = serialPort.readString(2);
                    if (enableLog){
                        sendToConsole(484, "read", data);
                    }
                    if (enableFileLog){
                        writeToFile(484, "read", data);
                    }
                    sizePointAngle = Integer.parseInt(data);
                    uartReceivedState = UartReceivedState.RECEIVE_ANGLE_POINT;
                    command[0] = "3";
                    serialPort.writeString(command[0] + command[1]);
                    if (enableLog){
                        sendToConsole(494, "write", command[0] + command[1]);
                    }
                    if (enableFileLog){
                        writeToFile(494, "write", command[0] + command[1]);
                    }
                    break;

                case RECEIVE_ANGLE_POINT:
                    data = serialPort.readString(sizePointAngle);
                    if (enableLog){
                        sendToConsole(504, "read", data);
                    }
                    if (enableFileLog){
                        writeToFile(504, "read", data);
                    }
                    JSONObject jsonRequest = new JSONObject(data);
                    Double t = Double.parseDouble(String.valueOf(jsonRequest.get("time")));
                    Double angle = Double.parseDouble(String.valueOf(jsonRequest.get("angle")));
                    Double measureMoment = Double.parseDouble(String.valueOf(jsonRequest.get("measure_moment")));
                    Double referenceMoment = Double.parseDouble(String.valueOf(jsonRequest.get("reference_moment")));
                    listenerPulseSignal.setNewPointOnGraph(t, angle, measureMoment, referenceMoment);
                    uartReceivedState = UartReceivedState.RECEIVE_SIZE_ANGLE_POINT;
                    command[0] = "2";
                    serialPort.writeString(command[0] + command[1]);
                    if (enableLog){
                        sendToConsole(519, "write", command[0] + command[1]);
                    }
                    if (enableFileLog){
                        writeToFile(519, "write", command[0] + command[1]);
                    }
                    break;
            }

        }

        private void transactionReceiveHarmonicPoints() throws SerialPortException {
            String data;
            switch (uartReceivedState) {
                case RECEIVE_SIZE_ANGLE_SINUS_POINTS:
                    data = serialPort.readString(2);
                    if (enableLog){
                        sendToConsole(535, "read", data);
                    }
                    if (enableFileLog){
                        writeToFile(535, "read", data);
                    }
                    sizePointAngleSinus = Integer.parseInt(data);
                    uartReceivedState = UartReceivedState.RECEIVE_ANGLE_SINUS_POINT;
                    command[0] = "6";
                    serialPort.writeString(command[0] + command[1]);
                    if (enableLog){
                        sendToConsole(545, "write", command[0] + command[1]);
                    }
                    if (enableFileLog){
                        writeToFile(545, "write", command[0] + command[1]);
                    }
                    break;

                case RECEIVE_ANGLE_SINUS_POINT:
                    data = serialPort.readString(sizePointAngleSinus);
                    if (enableLog){
                        sendToConsole(555, "read", data);
                    }
                    if (enableFileLog){
                        writeToFile(555, "read", data);
                    }
                    JSONObject jsonRequestSinus = new JSONObject(data);
                    Double timeSinus = Double.parseDouble(String.valueOf(jsonRequestSinus.get("time")));
                    Double measureAngle = Double.parseDouble(String.valueOf(jsonRequestSinus.get("angle")));
                    Double sinusAngle = Double.parseDouble(String.valueOf(jsonRequestSinus.get("sinusAngle")));
                    listenerHarmonicSignal.setNewPointsOnGraph(timeSinus, measureAngle, sinusAngle);
                    uartReceivedState = UartReceivedState.RECEIVE_SIZE_ANGLE_SINUS_POINTS;
                    command[0] = "5";
                    serialPort.writeString(command[0] + command[1]);
                    if (enableLog){
                        sendToConsole(569, "write", command[0] + command[1]);
                    }
                    if (enableFileLog){
                        writeToFile(569, "write", command[0] + command[1]);
                    }
                    break;
            }
        }
    }

    public String[] getSystemComPorts() {
        return SerialPortList.getPortNames();
    }
}
