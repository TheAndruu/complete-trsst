package com.completetrsst.client.controls;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainLayoutController {

    private static final Logger log = LoggerFactory.getLogger(MainLayoutController.class);

    @FXML
    private Pane feedPane;

    public MainLayoutController() {
    }

    public void showFeed(String accountId) {
        feedPane.getChildren().clear();
        if (accountId == null || accountId.length() < 1) {
            feedPane.getChildren().add(new Label("Feed goes here"));
            return;
        }

        WebView view = new WebView();
        WebEngine engine = view.getEngine();
        engine.load("http://localhost:8080/feed/" + accountId);
        feedPane.getChildren().add(view);
    }
}
