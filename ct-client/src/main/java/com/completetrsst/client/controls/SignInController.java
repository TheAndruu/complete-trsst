package com.completetrsst.client.controls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import com.completetrsst.client.controls.events.AuthenticationEvent;
import com.completetrsst.client.controls.events.AuthenticationHandler;
import com.completetrsst.client.controls.popup.PasswordConfirmPopup;
import com.completetrsst.client.controls.popup.PasswordInputPopup;
import com.completetrsst.client.managers.FileSystemKeyManager;
import com.completetrsst.crypto.keys.KeyManager;

public class SignInController {

    private static final String MENU_CLASS = "login-menu-item";

    private static final KeyManager keyManager = new FileSystemKeyManager();

    @FXML
    private FlowPane container;

    @FXML
    private Button createButton;
    @FXML
    private Button signInButton;
    @FXML
    private Button logOutButton;

    @FXML
    private HBox signInControls;

    @FXML
    private HBox loggedInControls;

    private List<AuthenticationHandler> loggedInHandlers = new ArrayList<AuthenticationHandler>();
    private List<AuthenticationHandler> loggedOutHandlers = new ArrayList<AuthenticationHandler>();

    /** Invoked by FXML when loading fxml content */
    public void initialize() throws Exception {
        // Show the logIn controls
        logOut();
    }

    @FXML
    private void logOut() {
        final String id = keyManager.getId();
        // clear the keys
        keyManager.clearKeys();

        // update the status bar
        logOutButton.setText("");
        container.getChildren().clear();
        container.getChildren().add(signInControls);

        loggedOutHandlers.forEach((handler) -> handler.handleEvent(new AuthenticationEvent().setAccountId(id).setAuthenticated(false)));
    }

    @FXML
    private void showCreatePopUp() {
        PasswordConfirmPopup passwordPopup = new PasswordConfirmPopup();
        passwordPopup.setPasswordCanceledHandler((event) -> passwordPopup.hide());
        passwordPopup.setPasswordEnteredHandler((event) -> createAccount(passwordPopup));
        passwordPopup.setRelativeTo(container.getParent());
        passwordPopup.show();
    }

    private void createAccount(PasswordConfirmPopup popup) {
        String confirmedPassword = popup.getConfirmedPassword();
        keyManager.createKeys(confirmedPassword);
        keyManager.saveKeys(confirmedPassword);
        popup.hide();
        showLogOut();
    }

    private void showLogOut() {
        logOutButton.setText("Log out from " + keyManager.getId());
        container.getChildren().clear();
        container.getChildren().add(loggedInControls);
    }

    @FXML
    private void showSignInPopUp(ActionEvent e) {
        final Popup popUp = new Popup();
        popUp.setAutoFix(true);
        popUp.setAutoHide(true);
        Collection<Control> signInOptions = createSignInMenuItems(popUp);
        VBox vBox = new VBox();
        vBox.getChildren().addAll(signInOptions);
        popUp.getContent().addAll(vBox);

        Point2D buttonPosition = signInButton.localToScreen(signInButton.getLayoutX(), signInButton.getLayoutY());
        double screenX = buttonPosition.getX() - signInButton.getWidth();
        double screenY = buttonPosition.getY() + signInButton.getHeight() / 2d;
        popUp.show(signInButton, screenX, screenY);
    }

    private Collection<Control> createSignInMenuItems(Popup popUp) {
        List<String> availableIds = keyManager.getAvailableFeedIds();
        // No id's found locally
        if (availableIds.size() == 0) {
            Control emptyLabel = createSignInMenuItem("No accounts created yet on this machine");
            emptyLabel.setOnMouseClicked((mouseEvent) -> popUp.hide());
            return Collections.singletonList(emptyLabel);
        }

        // For ids that were found
        List<Control> signInMenu = new ArrayList<Control>(availableIds.size() + 1);
        signInMenu.add(createSignInMenuItem("Choose an account to log in:"));
        availableIds.forEach((id) -> {
            Control item = createSignInMenuItem(id);
            item.setOnMouseClicked((mouseEvent) -> {
                popUp.hide();
                showPasswordInputForLogin(id);
            });
            signInMenu.add(item);
        });

        return signInMenu;
    }

    private void showPasswordInputForLogin(String id) {
        final PasswordInputPopup passwordPopup = new PasswordInputPopup();
        passwordPopup.setPasswordCanceledHandler((event) -> passwordPopup.hide());
        passwordPopup.setPasswordEnteredHandler((event) -> login(passwordPopup));
        passwordPopup.setRelativeTo(container.getParent());
        passwordPopup.setAccountId(id);
        passwordPopup.show();
    }

    private void login(PasswordInputPopup passwordPopup) {
        final String password = passwordPopup.getPasswordEntered();
        final String id = passwordPopup.getAccountId();
        keyManager.loadKeys(id, password);
        if (keyManager.getSignKey() == null) {
            passwordPopup.setStatusText("Password couldn't unlock account " + id);
        } else {
            passwordPopup.hide();
            showLogOut();
            loggedInHandlers.forEach((handler) -> handler.handleEvent(new AuthenticationEvent().setAccountId(id).setAuthenticated(true)));
        }
    }

    private Control createSignInMenuItem(String text) {
        Label item = new Label(text);
        item.getStyleClass().add(MENU_CLASS);
        return item;
    }

    public void addLoggedInHandler(AuthenticationHandler handler) {
        loggedInHandlers.add(handler);
    }

    public void addLoggedOutHandler(AuthenticationHandler handler) {
        loggedOutHandlers.add(handler);
    }

    public KeyManager getKeyManager() {
        return keyManager;
    }
}
