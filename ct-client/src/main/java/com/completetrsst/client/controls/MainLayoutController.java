package com.completetrsst.client.controls;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainLayoutController {

    private static final Logger log = LoggerFactory.getLogger(MainLayoutController.class);

    @FXML
    private BorderPane rootLayout;

    @FXML
    private BorderPane feedPane;

    public MainLayoutController() {
    }

    public void showFeed(String accountId) {
        if (accountId == null || accountId.length() < 1) {
            Pane pane = new Pane();
            pane.getChildren().add(new Label("Feed goes here"));
            feedPane.setCenter(pane);
            return;
        }

        WebView view = new WebView();
        WebEngine engine = view.getEngine();
//        engine.load("http://localhost:8080/feed/" + accountId);
        engine.load("http://www.google.com");
        feedPane.setCenter(view);
    }

    public void setFeedBottom(BorderPane pane) {
        feedPane.setBottom(pane);
    }

    public void clearFeedBottom() {
        setFeedBottom(null);
    }

}
