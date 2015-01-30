package com.completetrsst.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;

public class FeedPanelViewer extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    String greeting = "<html><body><p><strong>Hi, Stack Overflow!</strong></p></body></html>";
    ObservableList<String> names = FXCollections.observableArrayList("Matthew", "Hannah", "Stephan", "Denise");

    @Override
    public void start(Stage stage) {

        ListView<String> listView = new ListView<String>(names);
        stage.setScene(new Scene(listView));
        stage.show();

        listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> list) {
                return new HtmlFormatCell();
            }
        });

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Platform.runLater(() -> {
                System.out.println("Got here");
                names.add(greeting);
                names.add(greeting);
                names.add("andrew");
            });
        }).start();
    }

    public class HtmlFormatCell extends ListCell<String> {

        public HtmlFormatCell() {
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            System.out.println("Num items: " + names.size());
            System.out.println("Item: " + item);
            System.out.println("Is empty: " + empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (item != null) {
                if (item.contains("<p>")) {
                        WebView web = new WebView();
                        WebEngine engine = web.getEngine();
                        engine.loadContent(item);
                        web.setPrefHeight(50);
                        web.setPrefWidth(300);
                        web.autosize();
                        web.setBlendMode(BlendMode.MULTIPLY);
                        setText(null);
                        setGraphic(web);
                } else {
                    setText(item == null ? "" : "-" + item);
                    setTextFill(Color.BLUE);
                    if (isSelected()) {
                        setTextFill(Color.GREEN);
                    }
                    setGraphic(null);
                }

            }
        }
    }
}