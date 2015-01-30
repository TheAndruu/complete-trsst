package com.completetrsst.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class FxTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SplitPane Test");

        Group root = new Group();
        Scene scene = new Scene(root, 200, 200, Color.WHITE);

        SplitPane pane = new SplitPane();
        pane.setPrefSize(200, 200);
        pane.prefWidthProperty().bind(primaryStage.widthProperty());
        pane.prefHeightProperty().bind(primaryStage.heightProperty());
        pane.setOrientation(Orientation.HORIZONTAL);
        pane.setDividerPosition(0, 0.3);

        Button button1 = new Button("Button 1");
        Button button2 = new Button("Button 2");

        Button button3 = new Button("Button 3");
// same so far
        FlowPane leftFlowPane = new FlowPane(Orientation.VERTICAL);
        leftFlowPane.getChildren().addAll(button1, button3);

        Pane leftPane = new Pane();
        leftPane.getChildren().add(leftFlowPane);

        Pane rightPane = new Pane();
        rightPane.getChildren().add(button2);

        Platform.runLater(() -> SplitPane.setResizableWithParent(leftPane, false));

        pane.getItems().add(leftPane);
        pane.getItems().add(rightPane);
        button1.setPrefWidth(70);
        leftPane.setPrefWidth(100);
        pane.setDividerPosition(0, 0.3);

        root.getChildren().add(pane);

        primaryStage.setScene(scene);
        primaryStage.show();

        button1.prefWidthProperty().bind(leftPane.widthProperty());
        button2.prefWidthProperty().bind(rightPane.widthProperty());
    }

}