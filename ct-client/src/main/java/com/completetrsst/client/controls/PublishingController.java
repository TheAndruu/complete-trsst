package com.completetrsst.client.controls;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishingController {

    private static final Logger log = LoggerFactory.getLogger(PublishingController.class);

    @FXML
    private TextArea postInput;

    @FXML
    private RadioButton optionPublic;

    // TODO: When private and 'post' clicked, show popup prepopulated w/ recipients to choose from
    @FXML
    private RadioButton optionPrivate;

    @FXML
    private Button postButton;

    public PublishingController() {
    }

    @FXML
    public void handlePostButton(ActionEvent event) {

    }
}
