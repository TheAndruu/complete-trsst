package com.completetrsst.client.controls;

import java.util.concurrent.atomic.AtomicBoolean;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;

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

    private boolean isPrivate = false;
    
    @FXML
    private Button postButton;

    public PublishingController() {
    }

    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        optionPublic.setToggleGroup(group);
        optionPublic.setSelected(true);
        optionPrivate.setToggleGroup(group);
        
        group.selectedToggleProperty().addListener((event, ov, nv) -> {
            RadioButton chk = (RadioButton)nv.getToggleGroup().getSelectedToggle();
            isPrivate = chk.equals(optionPrivate);
        });
    }

    @FXML
    public void handlePostButton(ActionEvent event) {
        log.info("Is public post? : " + !(isPrivate));
    }
}
