package com.completetrsst.client.controls;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PasswordConfirmPopup {

    private final Stage stage = new Stage();

    private EventHandler<ActionEvent> passwordEnteredHandler;
    private EventHandler<ActionEvent> passwordCanceledHandler;

    private final PasswordField passwordOne = new PasswordField();
    private final PasswordField passwordTwo = new PasswordField();

    private final Text statusBox = new Text();

    private static final String TEXTBOX_CLASS = "login-password-box";
    private static final String LABEL_CLASS = "login-password-label";

    private final double WIDTH = 320;
    private final double HEIGHT = 312;

    private String confirmedPassword = null;

    public PasswordConfirmPopup() {
        Group root = new Group();
        Scene scene = new Scene(root, WIDTH, HEIGHT, Color.rgb(0, 0, 0, 0));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        Rectangle background = createBackground();

        VBox vBox = createForm();

        root.getChildren().addAll(background, vBox);

        stage.setScene(scene);
    }

    public void setRelativeTo(Parent parent) {
        Scene parentScene = parent.getScene();
        Point2D point = parent.localToScreen(parentScene.getX(), parentScene.getY());
        stage.setX(point.getX() + WIDTH / 2d);
        stage.setY(point.getY() + HEIGHT / 2d);
    }

    public void show() {
        stage.show();
    }

    public void hide() {
        setConfirmedPassword(null);
        passwordOne.clear();
        passwordTwo.clear();
        stage.hide();
    }

    public void setPasswordEnteredHandler(EventHandler<ActionEvent> handler) {
        this.passwordEnteredHandler = handler;
    }

    public void setPasswordCanceledHandler(EventHandler<ActionEvent> handler) {
        this.passwordCanceledHandler = handler;
    }

    private void setConfirmedPassword(String password) {
        this.confirmedPassword = password;
    }

    public String getConfirmedPassword() {
        return confirmedPassword;
    }

    public void setStatusText(String status) {
        statusBox.setText(status);
    }

    private VBox createForm() {
        initPasswordInputs();

        SVGPath padLock = createLockIcon();

        Label headerLabel = new Label("Enter a password for the account:");
        headerLabel.getStyleClass().add(LABEL_CLASS);

        HBox inputRow = new HBox();
        inputRow.getChildren().addAll(passwordOne, padLock);
        VBox boxBlock = new VBox();
        boxBlock.getChildren().addAll(headerLabel, inputRow, passwordTwo);

        Button enterButton = new Button("Sign in");
        enterButton.setOnAction((event) -> firePasswordConfirmed(event));
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction((event) -> passwordCanceledHandler.handle(event));

        HBox buttonRow = new HBox();
        buttonRow.getChildren().addAll(cancelButton, enterButton);

        HBox statusRow = new HBox();
        statusRow.getChildren().add(statusBox);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(boxBlock, buttonRow, statusRow);
        return vBox;
    }

    private void firePasswordConfirmed(ActionEvent event) {
        String validationResult = validatePasswords();
        if (validationResult.length() == 0) {
            setConfirmedPassword(passwordOne.getText());
            passwordEnteredHandler.handle(event);
        } else {
            statusBox.setText(validationResult);
        }
    }

    private String validatePasswords() {
        String p1 = passwordOne.getText();
        String p2 = passwordTwo.getText();
        if (p1 == null || p1.length() < 1) {
            return "Must enter the password in the first box";
        }
        if (p2 == null || p2.length() < 1) {
            return "Must enter the password in the second box";
        }

        if (!p1.equals(p2)) {
            return "The passwords must match";
        }
        if (p2.length() < 12) {
            return "Passwords must be at least 12 characters long";
        }

        return "";
    }

    private SVGPath createLockIcon() {
        SVGPath padLock = new SVGPath();
        padLock.setFill(Color.rgb(255, 255, 255, .9));
        padLock.setContent("M24.875,15.334v-4.876c0-4.894-3.981-8.875-8.875-8.875s-8.875,3.981-8.875,8.875v4.876H5.042v15.083h21.916V15.334H24.875zM10.625,10.458c0-2.964,2.411-5.375,5.375-5.375s5.375,2.411,5.375,5.375v4.876h-10.75V10.458zM18.272,26.956h-4.545l1.222-3.667c-0.782-0.389-1.324-1.188-1.324-2.119c0-1.312,1.063-2.375,2.375-2.375s2.375,1.062,2.375,2.375c0,0.932-0.542,1.73-1.324,2.119L18.272,26.956z");
        return padLock;
    }

    private void initPasswordInputs() {

        passwordOne.setFont(Font.font("SanSerif", 20));
        passwordOne.setPromptText("Enter Password");
        passwordOne.getStyleClass().add(TEXTBOX_CLASS);
        passwordOne.prefWidthProperty().bind(stage.widthProperty().subtract(55));
        // user hits the enter key
        passwordOne.setOnAction(actionEvent -> firePasswordConfirmed(actionEvent));

        passwordTwo.setFont(Font.font("SanSerif", 20));
        passwordTwo.setPromptText("Confirm Password");
        passwordTwo.getStyleClass().add(TEXTBOX_CLASS);
        passwordTwo.prefWidthProperty().bind(stage.widthProperty().subtract(55));
        // user hits the enter key
        passwordTwo.setOnAction(actionEvent -> firePasswordConfirmed(actionEvent));
    }

    private Rectangle createBackground() {
        Rectangle background = new Rectangle(320, 112);
        background.setX(0);
        background.setY(0);
        background.setArcHeight(14);
        background.setArcWidth(14);
        background.setFill(Color.rgb(0, 0, 0, .55));
        background.setStrokeWidth(1.5);
        background.setStroke(Color.rgb(255, 255, 255, .9));
        return background;
    }
}
