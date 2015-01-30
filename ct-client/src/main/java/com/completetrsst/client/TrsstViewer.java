package com.completetrsst.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;


public class TrsstViewer extends Application {

    class Content {
        private String value;
        private String contentType;

        public String getValue() {
            return value;
        }

        public Content(String type, String value) {
            this.contentType = type;
            this.value = value;
        }

        public String getContentType() {
            return this.contentType;
        }
    }

    class PlainContent extends Content {
        public PlainContent(String value) {
            super("text/plain", value);
        }
    }

    class HtmlContent extends Content {
        public HtmlContent(String value) {
            super("text/html", value);
        }
    }

    private ObservableList<Content> names = FXCollections.observableArrayList(new PlainContent("this is some plain content here"), new HtmlContent(
            "<p> this is some html content in a <p> tag</p>"), new HtmlContent("<strong>this is some text in a strong tag</strong>"),
            new PlainContent("more plain content, this with a <p> tag in it </p>"));

    public static void main(String[] args) {
        launch(args);
    }

    private ListView<Content>listView; 
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Trsst Viewer");

        Group root = new Group();
        Scene scene = new Scene(root, 600, 500, Color.WHITE);
        scene.getStylesheets().add("styles.css");

        SplitPane pane = new SplitPane();
        pane.setPrefSize(200, 200);
        pane.prefWidthProperty().bind(primaryStage.widthProperty());
        pane.prefHeightProperty().bind(primaryStage.heightProperty());
        pane.setOrientation(Orientation.HORIZONTAL);
        pane.setDividerPosition(0, 0.1);

        Button button1 = new Button("Button 1");
        Button button3 = new Button("Button 3");

        listView = new ListView<Content>(names);
        listView.setCellFactory(new Callback<ListView<Content>, ListCell<Content>>() {
            @Override
            public ListCell<Content> call(ListView<Content> list) {
                return new HtmlFormatCell();
            }
        });

        // same so far
        FlowPane leftFlowPane = new FlowPane(Orientation.VERTICAL);
        leftFlowPane.getChildren().addAll(button1, button3);

        Pane leftPane = new Pane();
        leftPane.getChildren().add(leftFlowPane);

        Pane rightPane = new Pane();
        rightPane.getChildren().add(listView);

        Platform.runLater(() -> SplitPane.setResizableWithParent(leftPane, false));

        pane.getItems().addAll(leftPane, rightPane);

        root.getChildren().add(pane);

        primaryStage.setScene(scene);
        primaryStage.show();

        button1.prefWidthProperty().bind(leftPane.widthProperty());
        listView.prefWidthProperty().bind(rightPane.widthProperty());
        listView.prefHeightProperty().bind(rightPane.heightProperty());

        addOnNewThread();
    }

    private void addOnNewThread() {
        new Thread(
                () -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Platform.runLater(() -> {
                        System.out.println("Got here");
                        names.add(new HtmlContent(
                                "<html><body><p><strong>Hi, Charles Town!</strong></p><p><strong>Hi, Charles Town!</strong></p><p><strong>Hi, Charles Town! Hi, Charles Town! Hi, Charles Town! Hi, Charles Town! Hi, Charles Town! Hi, Charles Town! Hi, Charles Town! Hi, Charles Town!</strong></p><p><strong>Hi, Charles Town!</strong></p><p><strong>Hi, Charles Town!</strong></p></body></html>"));
                        names.add(new HtmlContent("<html><body><p><strong>Hi, WV!</strong></p></body></html>"));
                        names.add(new PlainContent("andrew is the beast"));
                    });
                }).start();
    }

    public class HtmlFormatCell extends ListCell<Content> {

        public HtmlFormatCell() {
        }

        @Override
        protected void updateItem(Content item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            WebView web = new WebView();
            WebEngine engine = web.getEngine();
            
//            engine.getLoadWorker().stateProperty().addListener((state) -> {
//                String heightText = engine.executeScript("window.getComputedStyle(document.body, null).getPropertyValue('height')").toString();
//                double height = Double.valueOf(heightText.replace("px", ""));
//                double min = Math.min(height + 35, 100);
//                web.setPrefHeight(min);
//            });
            engine.loadContent(item.getValue(), item.getContentType());
            
//            web.addEventHandler(ScrollEvent.SCROLL, (ev) -> {
//                listView.setTranslateX(ev.getDeltaX());
//                listView.setTranslateY(ev.getDeltaY());
////                listView.fireEvent(ev);
//            });
            
//            web.setOnScroll((scrollEvent) -> {
//              
//                listView.setTranslateY(listView.getTranslateY() + scrollEvent.getDeltaY());
//                listView.translateYProperty().bind(web.translateYProperty());
//                web.translateYProperty().bind(listView.translateYProperty());
//                listView.setTranslateY(listView.getTranslateY() + amount);
//                System.out.println("Text y: " + scrollEvent.getTextDeltaY());
////              
//            });
            
            web.setPrefHeight(100);
            web.setPrefWidth(300);
            web.autosize();
            web.setBlendMode(BlendMode.MULTIPLY);
            setText(null);
            setGraphic(web);

            // engine.loadContent(item);
            

            // Original labels
            // note these do scroll as desired
            // setText(item == null ? "" : "-" + item);
            // setTextFill(Color.BLUE);

            // Text area configuring:
            // setText(null);
            // TextField field = new TextField(item);
            // field.setEditable(false);
            // field.setBlendMode(BlendMode.MULTIPLY);
            // field.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-background-insets: 0, 0, 0, 0; -fx-border-color: black; -fx-border-width: 0; "
            // + "-fx-border-radius: 16; -fx-focus-color: transparent");
            // setGraphic(field);
            // }

        }
    }
}



/*
listView.fireEvent(new ScrollEvent(ScrollEvent.SCROLL,
ev.getX(), ev.getY(), ev.getScreenX(), ev.getScreenY(), 
ev.isShiftDown(), ev.isControlDown(), ev.isAltDown(), ev.isMetaDown(), ev.isDirect(),
ev.isInertia(),
ev.getDeltaX(), ev.getDeltaY(), ev.getTotalDeltaX(), ev.getTotalDeltaY(),
ev.getMultiplierX(), ev.getMultiplierY(), ev.getTextDeltaXUnits(), ev.getTextDeltaX(), ev.getTextDeltaYUnits(), ev.getTextDeltaY(),
ev.getTouchCount(), new PickResult(listView, ev.getSceneX(), ev.getSceneY()))); */