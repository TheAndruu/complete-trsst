package com.completetrsst.client;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.completetrsst.client.controls.SignInController;

/** This will be the main application for our Trsst Client */
public class TrsstViewer extends Application {

    private static final Logger log = LoggerFactory.getLogger(TrsstViewer.class);

    private SignInController signInController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        BorderPane mainLayout;
        try {
            mainLayout = constructLayout();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        Scene scene = new Scene(mainLayout);
        scene.getStylesheets().add("styles/custom-fx8.css");
        stage.setTitle("FXML Welcome");
        stage.setScene(scene);
        stage.show();
    }

    private BorderPane constructLayout() throws IOException {
        BorderPane mainLayout = FXMLLoader.load(getClass().getResource("/com/completetrsst/client/controls/MainLayoutView.fxml"));

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/completetrsst/client/controls/SignInView.fxml"));
        Pane signInPane = loader.load();

        mainLayout.setTop(signInPane);

        signInController = loader.getController();

        return mainLayout;
    }

}