package org.nalecz.jfirmware.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.nalecz.jfirmware.core.Connector;
import org.nalecz.jfirmware.core.DeviceInfo;
import org.nalecz.jfirmware.core.SimpleDataflash;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    private static final String WINDOW_TITLE = "JFirmwareEditor - Updater";

    public static void main(String[] args) {
        Connector.getDevice();
        launch(args);
    }

    private void bindSceneHandlers(Scene scene) {
        Connector.getDeviceSubject().subscribe(device -> Platform.runLater(() -> {
            SimpleDataflash dataflash = device.readDataflash();

            TextField hwVersion = (TextField) scene.lookup("#hwVersion");
            hwVersion.setText(String.valueOf(dataflash.getHardwareVersion() / 100f));

            TextField fwVersion = (TextField) scene.lookup("#fwVersion");
            fwVersion.setText(String.valueOf(dataflash.getFirmwareVersion() / 100f));

            TextField bootMode = (TextField) scene.lookup("#bootMode");
            bootMode.setText(dataflash.isLoadFromLdrom() ? "LDROM" : "APROM");

            DeviceInfo deviceInfo = DeviceInfo.get(dataflash);
            TextField deviceName = (TextField) scene.lookup("#deviceName");
            deviceName.setText(deviceInfo.getName());
        }));

        Connector.getStateSubject().subscribe(state -> Platform.runLater(() -> {
            String stateString = "Unknown";
            if (Objects.equals(state, Connector.STATE_CONNECTED)) {
                stateString = "Connected";
            } else if (Objects.equals(state, Connector.STATE_DISCONNECTED)) {
                stateString = "Disconnected";
            }

            Label connectionState = (Label) scene.lookup("#connectionState");
            if (connectionState != null) {
                connectionState.setText(stateString);
            }
        }));
    }

    @Override
    public synchronized void start(Stage primaryStage) {
        primaryStage.setTitle(WINDOW_TITLE);

        try {
            VBox rootLayout = FXMLLoader.load(getClass().getResource("/view/rootLayout.fxml"));

            Scene scene = new Scene(rootLayout);

            bindSceneHandlers(scene);
            scene.getStylesheets().add(Main.class.getResource("/bootstrap3.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.show();

            Button cancelButton = (Button) scene.lookup("#cancelButton");
            cancelButton.setOnAction(event -> Platform.exit());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
