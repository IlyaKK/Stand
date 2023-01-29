package org.mai;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.mai.domain.Uart;
import org.mai.domain.application_control.ListenerApplication;
import org.mai.ui.PulseSignalController;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application implements ListenerApplication {
    public Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(App.class.getResource("primary.fxml"));
        Parent root = loader.load();
        stage.setTitle("Стенд");
        scene = new Scene(root, 1080, 720);
        stage.setScene(scene);
        stage.show();
        PulseSignalController.initialiseApplicationListener(this);
    }

    /*
    Переопределяем метод закрытия программы "по крестику".
    При закрытии программы закрываем порт, чтобы основной поток завершился корректно
     */
    @Override
    public void stop(){
        Uart.closePort();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void doSnapShot() throws IOException {
        WritableImage image = scene.snapshot(null);
        File file = new File("tempExperiment.png");
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", file);
    }
}