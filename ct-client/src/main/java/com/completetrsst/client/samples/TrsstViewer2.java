package com.completetrsst.client.samples;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.SplitPane;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import org.apache.commons.lang3.StringEscapeUtils;



public class TrsstViewer2 extends Application {

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
        public String getValue() {
            String oldValue = super.getValue();
            String newValue = "<div style='color: red;'>";
            newValue += oldValue;
            newValue += "</div>";
            return newValue;
        }
    }

    private ObservableList<Content> names = FXCollections.observableArrayList(new PlainContent("this is some plain content here"), new HtmlContent(
            "<p> this is some html content in a <p> tag</p>"), new HtmlContent("<strong>this is some text in a strong tag</strong>"),
            new PlainContent("more plain content, this with a <p> tag in it </p>"));

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Trsst Viewer");

        Group root = new Group();
        Scene scene = new Scene(root, 600, 500, Color.WHITE);

        SplitPane pane = new SplitPane();
        pane.setPrefSize(200, 200);
        pane.prefWidthProperty().bind(primaryStage.widthProperty());
        pane.prefHeightProperty().bind(primaryStage.heightProperty());
        pane.setOrientation(Orientation.HORIZONTAL);
        pane.setDividerPosition(0, 0.2);

        Button button1 = new Button("Button 1");
        Button button3 = new Button("Button 3");

        // same so far
        FlowPane leftFlowPane = new FlowPane(Orientation.VERTICAL);
        leftFlowPane.getChildren().addAll(button1, button3);

        Pane leftPane = new Pane();
        leftPane.getChildren().add(leftFlowPane);

        
        WebView web = new WebView();
        WebEngine engine = web.getEngine();
        web.autosize();
        web.setBlendMode(BlendMode.MULTIPLY);
        
        addNamesListener(engine);
        addOnNewThread();
        
        
        Pane rightPane = new Pane();
        rightPane.getChildren().add(web);

        Platform.runLater(() -> SplitPane.setResizableWithParent(leftPane, false));

        pane.getItems().addAll(leftPane, rightPane);

        root.getChildren().add(pane);

        primaryStage.setScene(scene);
        primaryStage.show();

        button1.prefWidthProperty().bind(leftPane.widthProperty());
        web.prefWidthProperty().bind(rightPane.widthProperty());
//        web.prefHeightProperty().bind(rightPane.heightProperty().subtract(30));
        web.prefHeightProperty().bind(rightPane.heightProperty());

        addOnNewThread();
    }

    private void addNamesListener(WebEngine engine) {
        names.addListener(new ListChangeListener<Content>() {
            @Override
            public void onChanged(Change<? extends Content> content) {
                StringBuilder builder = new StringBuilder();
                content.getList().forEach((listItem) -> {
                    if (listItem instanceof HtmlContent) {
                        builder.insert(0, listItem.getValue());
                    } else {
                        // if plain text, escape for display
                        String escaped = StringEscapeUtils.escapeHtml4(listItem.getValue());
                        builder.insert(0, escaped);
                    }
                });
                engine.loadContent(builder.toString());
            }
            
        });
        
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

    private class HtmlFormatCell extends ListCell<Content> {

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
            
            engine.loadContent(item.getValue(), item.getContentType());
            
            
            web.setPrefHeight(100);
            web.setPrefWidth(300);
            web.autosize();
            web.setBlendMode(BlendMode.MULTIPLY);
            setText(null);
            setGraphic(web);

        }
    }
}
