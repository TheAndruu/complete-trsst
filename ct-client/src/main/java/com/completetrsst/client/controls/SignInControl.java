package com.completetrsst.client.controls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import com.completetrsst.crypto.keys.KeyManager;

public class SignInControl extends HBox {

    private static final String LOGIN_BUTTON_CLASS = "login-menu-button";

    private KeyManager keyManager;

    private Control signInButton = new Button("Sign in");
    private Control createButton = new Button("Create account");

    // private PasswordInputPopup passwordPopup = new PasswordInputPopup();

    public SignInControl() {
        constructControl();
    }

    private void constructControl() {
        createButton.getStyleClass().add(LOGIN_BUTTON_CLASS);
        createButton.setOnMouseClicked((event) -> {
            showCreatePopUp();

        });
        
        signInButton.getStyleClass().add(LOGIN_BUTTON_CLASS);
        signInButton.setOnMouseClicked((event) -> {
            showSignInPopUp();
        });


        getChildren().addAll(createButton, signInButton);
    }

    private void showCreatePopUp() {
         PasswordConfirmPopup passwordPopup = new PasswordConfirmPopup();
         passwordPopup.setPasswordCanceledHandler((event) -> passwordPopup.hide());
         passwordPopup.setPasswordEnteredHandler((event) -> createAccount(passwordPopup));
         passwordPopup.setRelativeTo(getParent());
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
        
        // TODO: Add menu showing account logged in as and logout button with action handlers
        
        // TODO: Also add 'clear keys' function to KeyManager
        signInButton.setVisible(false);
        createButton.setVisible(false);
        getChildren().add(new Label("Log out"));
    }

    private void showSignInPopUp() {
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
        List<Control> signInMenu = new ArrayList<Control>(availableIds.size());
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
        passwordPopup.setRelativeTo(SignInControl.this.getParent());
        passwordPopup.setAccountId(id);
        passwordPopup.show();
    }

    private void login(PasswordInputPopup passwordPopup) {
        String password = passwordPopup.getPasswordEntered();
        String id = passwordPopup.getAccountId();
        keyManager.loadKeys(id, password);
        if (keyManager.getSignKey() == null) {
            passwordPopup.setStatusText("Password couldn't unlock account " + id);
        } else {
            passwordPopup.hide();
            showLogOut();
        }
    }

    private Control createSignInMenuItem(String text) {
        Label item = new Label(text);
        item.getStyleClass().add(LOGIN_BUTTON_CLASS);
        return item;
    }

    public SignInControl setKeyManager(KeyManager manager) {
        this.keyManager = manager;
        return this;
    }
}
