package com.completetrsst.client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class FxTest extends Application {

    public static void main(String[] args) {
        Application.launch(FxTest.class, args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SplitPane Test");

        Group root = new Group();
        Scene scene = new Scene(root, 200, 200, Color.WHITE);

        // CREATE THE SPLITPANE
        BorderPane pane = new BorderPane();
        pane.setPrefSize(200, 200);
        pane.prefWidthProperty().bind(primaryStage.widthProperty());
        pane.prefHeightProperty().bind(primaryStage.heightProperty());
        // splitPane.prefWidthProperty().bind(primaryStage.widthProperty().subtract(55));
        // passwordField.prefWidthProperty().bind(primaryStage.widthProperty().subtract(55));
        // splitPane.setOrientation(Orientation.HORIZONTAL);
        // splitPane.setDividerPosition(0, 0.3);

        // ADD LAYOUTS AND ASSIGN CONTAINED CONTROLS
        Button button1 = new Button("Button 1");
        Button button2 = new Button("Button 2");

        Button button3 = new Button("Button 3");
        
        
        FlowPane leftFlowPane = new FlowPane(Orientation.VERTICAL);
        leftFlowPane.getChildren().addAll(button1, button3);
        
        Pane leftPane = new Pane();
        // button1.setMaxWidth(50d);
        // leftPane.setLeft(button1);
        leftPane.getChildren().add(leftFlowPane);

//        Pane label = new Pane();
//        label.getChildren().add(new Label("||"));
//        leftPane.getChildren().add(label);
//        label.setLayoutX(71);
        Pane rightPane = new Pane();
        // rightPane.setRight(button2);
        rightPane.getChildren().add(button2);

        
        // addListeners(button1);
        // addListeners(button2);

        pane.setLeft(leftPane);
        pane.setCenter(rightPane);

        // ADD SPLITPANE TO ROOT
        root.getChildren().add(pane);

        primaryStage.setScene(scene);
        primaryStage.show();
        
//        leftFlowPane.prefWidthProperty().bind(label.layoutXProperty());
        button1.prefWidthProperty().bind(leftPane.widthProperty());
        button2.prefWidthProperty().bind(rightPane.widthProperty());
        
    }

    private void addListeners(Node label, final double minLeft) {
        final Delta dragDelta = new Delta();
        label.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                // record a delta distance for the drag and drop operation.
                dragDelta.x = label.getLayoutX() - mouseEvent.getSceneX();
                // dragDelta.y = label.getLayoutY() - mouseEvent.getSceneY();
                label.setCursor(Cursor.MOVE);
            }
        });
        label.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                label.setCursor(Cursor.HAND);
            }
        });
        label.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                double sceneX = mouseEvent.getSceneX();
                // if we're not too into the negative X values
                if ((sceneX + dragDelta.x) > minLeft) {
                        label.setLayoutX(sceneX + dragDelta.x);
                }
            }
        });
        label.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                label.setCursor(Cursor.HAND);
            }
        });
    }

    // records relative x and y co-ordinates.
    class Delta {
        double x, y;
    }
}