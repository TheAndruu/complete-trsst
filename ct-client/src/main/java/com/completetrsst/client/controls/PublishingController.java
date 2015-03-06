package com.completetrsst.client.controls;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.xml.crypto.dsig.XMLSignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.completetrsst.atom.AtomEncrypter;
import com.completetrsst.atom.AtomSigner;
import com.completetrsst.client.controls.events.PublishEvent;
import com.completetrsst.client.controls.events.PublishHandler;
import com.completetrsst.crypto.keys.KeyManager;

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

    // TODO: Better way to pass around or share this key manager
    private KeyManager keyManager;

    @FXML
    private Button postButton;

    private List<PublishHandler> publishHandlers = new ArrayList<PublishHandler>();

    public PublishingController() {
    }

    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        optionPublic.setToggleGroup(group);
        optionPublic.setSelected(true);
        optionPrivate.setToggleGroup(group);

        group.selectedToggleProperty().addListener((event, ov, nv) -> {
            RadioButton chk = (RadioButton) nv.getToggleGroup().getSelectedToggle();
            isPrivate = chk.equals(optionPrivate);
        });
    }

    @FXML
    public void handlePostButton(ActionEvent event) {
        log.info("Is private post? : " + isPrivate);

        String rawXml = isPrivate ? createEncryptedPost() : createPublicPost();

        System.err.println("Raw xml to be posted: " + rawXml);
        
        postWithJaxRs("http://localhost:8080/publish", rawXml);

        publishHandlers.forEach((handler) -> handler.handleEvent(new PublishEvent().setAccountId(keyManager.getId())));

    }

    private String createEncryptedPost() {
        AtomEncrypter signer = new AtomEncrypter();
        String textToEncrypt = postInput.getText();
        // TODO: Need previous entry's post value -- pull from API
        String prevEntrySigValue = "";

        // TODO: Need public keys of recipients!
        List<PublicKey> recipientKeys = Collections.EMPTY_LIST;

        // TODO: Just use our own keys for now
        try {
            return signer.createEncryptedEntry(textToEncrypt, prevEntrySigValue, keyManager.getSignKey(), keyManager.getEncryptKey(), recipientKeys);
        } catch (IOException | GeneralSecurityException | XMLSignatureException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String createPublicPost() {
        AtomSigner signer = new AtomSigner();
        try {
            return signer.createEntry(postInput.getText(), "", keyManager.getSignKey(), keyManager.getEncryptKey().getPublic());
        } catch (IOException | XMLSignatureException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void postWithJaxRs(String urlPath, String data) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(urlPath);
        Invocation.Builder builder = target.request();
        String response = builder.post(Entity.entity(data, MediaType.APPLICATION_XML_TYPE), String.class);
        client.close();

        log.info("Got response: " + response);
    }

    public void setKeyManager(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    public void addPublishHandler(PublishHandler handler) {
        publishHandlers.add(handler);
    }
}
