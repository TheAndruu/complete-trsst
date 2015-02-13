package com.completetrsst.client;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.completetrsst.client.controls.MainLayoutController;
import com.completetrsst.client.controls.SignInController;
import com.completetrsst.client.controls.events.AuthenticationHandler;

/** This will be the main application for our Trsst Client */
public class TrsstViewer extends Application {

    private static final Logger log = LoggerFactory.getLogger(TrsstViewer.class);

    private SignInController signInController;

    private MainLayoutController mainLayoutController;

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
        FXMLLoader mainLayoutLoader = loadView("/com/completetrsst/client/controls/MainLayoutView.fxml");
        BorderPane mainLayout = mainLayoutLoader.load();
        mainLayoutController = mainLayoutLoader.getController();

        FXMLLoader signInLoader = loadView("/com/completetrsst/client/controls/SignInView.fxml");
        Pane signInPane = signInLoader.load();
        signInController = signInLoader.getController();

        mainLayout.setTop(signInPane);
        addAuthHandlers();
        return mainLayout;
    }

    private FXMLLoader loadView(String viewLocation) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(viewLocation));
        return loader;
    }

    public AuthenticationHandler loggedInHandler() {
        return (event) -> {
            showPublishingPane();
            mainLayoutController.showFeed("6cVDNuHqpviE47ReY3gfidyLPoJ3hFBGK");
        };
    }

    private void showPublishingPane() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = loadView("/com/completetrsst/client/controls/PublishingView.fxml");
                BorderPane publishingView = loader.load();
                mainLayoutController.setFeedBottom(publishingView);
            } catch (IOException e) {
                log.error("Couldn't load Publishing View", e);
                return;
            }
        });
    }

    public AuthenticationHandler loggedOutHandler() {
        return (event) -> {
            mainLayoutController.clearFeedBottom();
            mainLayoutController.showFeed("");
        };
    }

    private void addAuthHandlers() {
        signInController.addLoggedInHandler(loggedInHandler());
        signInController.addLoggedOutHandler(loggedOutHandler());
    }

}