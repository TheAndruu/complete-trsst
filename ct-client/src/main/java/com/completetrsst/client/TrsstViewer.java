package com.completetrsst.client;

import java.io.File;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import com.completetrsst.client.controls.SignInControl;
import com.completetrsst.crypto.keys.KeyManager;

/** This will be the main application for our Trsst Client */
public class TrsstViewer extends Application {

    private static final double WINDOW_WIDTH = 700;
    private static final double WINDOW_HEIGHT = 500;

    private static final double LEFT_PANE_WIDTH = 200;
    private static final double RIGHT_PANE_WIDTH = WINDOW_WIDTH - LEFT_PANE_WIDTH;

    private Stage primaryStage;

    private static final KeyManager keyManager = new FileSystemKeyManager();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        // Left pane
        BorderPane leftPane = createLeftPane();
        leftPane.setTop(createSignInBar());

        // Right pane
        Pane rightPane = createRightPane();

        // Split pane to contain both
        SplitPane splitPane = createSplitPane();
        splitPane.getItems().addAll(leftPane, rightPane);
        SplitPane.setResizableWithParent(leftPane, false);

        // Main scene
        Group rootGroup = new Group();
        rootGroup.getChildren().add(splitPane);
        Scene scene = new Scene(rootGroup, WINDOW_WIDTH, WINDOW_HEIGHT, Color.WHITE);
        scene.getStylesheets().add("styles/custom-fx8.css");
        stage.setScene(scene);

        // Property size bindings
        rightPane.prefWidthProperty().bind(stage.widthProperty());// .subtract(leftPane.widthProperty()));
        rightPane.prefHeightProperty().bind(stage.heightProperty());

        configureAndShowStage(stage);
    }

    private void configureAndShowStage(Stage stage) {
        stage.setTitle("Trsst Viewer");
        stage.sizeToScene();
        stage.show();
    }

    private SplitPane createSplitPane() {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        Platform.runLater(() -> splitPane.setDividerPosition(0, LEFT_PANE_WIDTH / WINDOW_WIDTH));
        return splitPane;
    }

    private BorderPane createLeftPane() {
        BorderPane leftPane = new BorderPane();
        leftPane.setPrefWidth(LEFT_PANE_WIDTH);
        return leftPane;
    }

    private Pane createSignInBar() {
        SignInControl signIn = new SignInControl();
        signIn.setKeyManager(keyManager);
        return signIn;
    }

    private void showSignInModal() {
        // final Stage dialog = new Stage();
        // dialog.initModality(Modality.WINDOW_MODAL);
        // dialog.initOwner(primaryStage);

        KeyManager keyManager = new FileSystemKeyManager();
        FileChooser fileChooser = new FileChooser();

        // fileChooser.setInitialDirectory(keyManager.getKeyStoreHome().toFile());
        fileChooser.setTitle("Load private keys");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Key Files", "*.pkcs", "*.jks", "*.p12"),
                new ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        // TODO: Load file as key

    }

    private Pane createRightPane() {
        Pane rightPane = new Pane();
        rightPane.setPrefWidth(RIGHT_PANE_WIDTH);
        rightPane.getChildren().add(new Label("Feed goes here eventually"));
        return rightPane;
    }
}