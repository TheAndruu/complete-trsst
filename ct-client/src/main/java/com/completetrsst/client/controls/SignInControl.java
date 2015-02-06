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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import com.completetrsst.crypto.keys.KeyManager;

public class SignInControl extends Pane {

    private static final String MENU_CLASS = "login-menu-item";

    private KeyManager keyManager;

    private Button signInButton = new Button("Sign in");
    private Button createButton = new Button("Create account");
    private Button logOutButton = new Button("Logout");

    private HBox signInControls = new HBox();
    private HBox loggedInControls = new HBox();
    
    public SignInControl() {
        constructControl();
    }

    private void constructControl() {
        createButton.getStyleClass().add(MENU_CLASS);
        createButton.setOnAction((event) -> {
            showCreatePopUp();
        });
        
        signInButton.getStyleClass().add(MENU_CLASS);
        signInButton.setOnAction((event) -> {
            showSignInPopUp();
        });
        
        signInControls.getChildren().addAll(createButton, signInButton);
        HBox.setHgrow(signInButton, Priority.ALWAYS);
        
        // Make signIn the visible options
        getChildren().addAll(signInControls);
        
        // create the header for when one is logged in, leaving it not visible
        
        logOutButton.getStyleClass().add(MENU_CLASS);
        logOutButton.setOnAction((event) -> logOut());
        
        HBox.setHgrow(logOutButton, Priority.ALWAYS);
        
        loggedInControls.getChildren().addAll(logOutButton);
    }

    private void logOut() {
        // clear the keys
        keyManager.clearKeys();
        
        // update the status bar
        logOutButton.setText("");
        getChildren().clear();
        getChildren().add(signInControls);
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
        logOutButton.setText("Log out from " + keyManager.getId());
        getChildren().clear();
        getChildren().add(loggedInControls);
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
        item.getStyleClass().add(MENU_CLASS);
        return item;
    }

    public SignInControl setKeyManager(KeyManager manager) {
        this.keyManager = manager;
        return this;
    }
}
