package com.completetrsst.client;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TrsstViewer extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Trsst Viewer");

        Group root = new Group();
        Scene scene = new Scene(root, 200, 200, Color.WHITE);

        BorderPane mainLayoutPane = new BorderPane();
        mainLayoutPane.setPrefSize(200, 200);
        mainLayoutPane.prefWidthProperty().bind(primaryStage.widthProperty());
        mainLayoutPane.prefHeightProperty().bind(primaryStage.heightProperty());

        Button button1 = new Button("Button 1");
        Button button2 = new Button("Button 2");

        Button button3 = new Button("Button 3");

        FlowPane leftPane = new FlowPane(Orientation.VERTICAL);
        leftPane.getChildren().addAll(button1, button3);

        Pane rightPane = new Pane();
        rightPane.getChildren().add(button2);

        mainLayoutPane.setLeft(leftPane);
        mainLayoutPane.setCenter(rightPane);

        root.getChildren().add(mainLayoutPane);

        primaryStage.setScene(scene);
        primaryStage.show();

        button1.prefWidthProperty().bind(leftPane.widthProperty());
        button2.prefWidthProperty().bind(rightPane.widthProperty());
    }

}