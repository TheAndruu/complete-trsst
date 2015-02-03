package com.completetrsst.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TrsstViewer extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        // Left pane
        BorderPane leftPane = createLeftPane();
        Button signInButton = createSignInButton();
        leftPane.setTop(signInButton);

        // Right pane
        Pane rightPane = createRightPane();

        // Split pane to contain both
        SplitPane splitPane = createSplitPane();
        splitPane.getItems().addAll(leftPane, rightPane);
        SplitPane.setResizableWithParent(leftPane, false);

        // Main scene
        Group rootGroup = new Group();
        rootGroup.getChildren().add(splitPane);
        Scene scene = new Scene(rootGroup, 700, 500, Color.WHITE);

        // Property size bindings
        rightPane.prefWidthProperty().bind(stage.widthProperty());// .subtract(leftPane.widthProperty()));
        rightPane.prefHeightProperty().bind(stage.heightProperty());

        configureAndShowStage(stage, scene);
    }

    private void configureAndShowStage(Stage stage, Scene scene) {
        stage.setTitle("Trsst Viewer");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

    private SplitPane createSplitPane() {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        Platform.runLater(() -> splitPane.setDividerPosition(0, 200 / 700d));
        return splitPane;
    }

    private BorderPane createLeftPane() {
        BorderPane leftPane = new BorderPane();
        leftPane.setPrefWidth(200);
        return leftPane;
    }

    private Button createSignInButton() {
        Button signInButton = new Button("Sign in");
        return signInButton;
    }

    private Pane createRightPane() {
        Pane rightPane = new Pane();
        rightPane.setPrefWidth(300);
        rightPane.getChildren().add(new Label("Feed goes here eventually"));
        return rightPane;
    }

    private void addOnNewThread() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Platform.runLater(() -> {
                System.out.println("Got here");
            });
        }).start();
    }
}