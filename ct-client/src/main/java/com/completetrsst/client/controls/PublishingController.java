package com.completetrsst.client.controls;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.crypto.dsig.XMLSignatureException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.completetrsst.atom.AtomSigner;
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
        log.info("Is public post? : " + !(isPrivate));
        
        AtomSigner signer = new AtomSigner();
        String rawXml = "";
        try {
            rawXml = signer.createEntry(postInput.getText(), "", keyManager.getSignKey(), keyManager.getEncryptKey().getPublic());
        } catch (IOException | XMLSignatureException e1) {
            log.error(e1.getMessage(), e1);
            throw new RuntimeException(e1);
        }
        
        try {
            postToUrl("http://localhost:8080/publish/" + keyManager.getId(),rawXml);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

    }

    private void postToUrl(String urlPath, String data) throws IOException {
        URL url = new URL(urlPath);

        String result = "";
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", " application/xml");
            connection.setRequestProperty("charset", "UTF-8");
            connection.setRequestProperty("Content-Length", "" + data.getBytes().length);
            connection.setUseCaches(false);
            byte[] bytes = data.getBytes(Charset.forName("UTF-8"));
            DataOutputStream outStream = null;
            try {
                outStream = new DataOutputStream(connection.getOutputStream());
                outStream.write(bytes);
            } finally {
                if (outStream != null) {
                    outStream.flush();
                    outStream.close();
                }
            }

            InputStream inStream = null;
            try {
                inStream = connection.getInputStream();
                result = IOUtils.toString(inStream, "UTF-8");
            } finally {
                if (inStream != null) {
                    inStream.close();
                }
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        log.info("Message returned: " + result);
    }

    public void setKeyManager(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

}
