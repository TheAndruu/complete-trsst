package com.completetrsst.client.controls;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.xml.crypto.dsig.XMLSignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.completetrsst.atom.AtomParser;
import com.completetrsst.crypto.xml.SignatureUtil;
import com.completetrsst.rome.modules.EntryModule;
import com.completetrsst.rome.modules.FeedModule;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;

public class MainLayoutController {

    private static final Logger log = LoggerFactory.getLogger(MainLayoutController.class);

    @FXML
    private BorderPane rootLayout;

    @FXML
    private BorderPane feedPane;

    @FXML
    private ListView<String> entryListView;

    private ObservableList<String> entries = FXCollections.observableArrayList();

    public MainLayoutController() {
    }

    /** Invoked by FXML when loading fxml content */
    public void initialize() {
        log.info("Call to initialize in MainLayoutController");

        entryListView.setItems(entries);
        entryListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> list) {
                return new HtmlFormatCell();
            }
        });
    }

    public void showFeed(String accountId) {

        Platform.runLater(() -> {
            entries.clear();

            // Set to empty if no account logged in
            if (accountId == null || accountId.length() < 1) {
                entries.add("Feed goes here");
                return;
            }

            loadFeed("http://localhost:8080/feed/" + accountId);

        });

    }

    private void loadFeed(final String url) {
        // Otherwise, update the entries from the feed
        // TODO: Move this to a separate class / method
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(url);
        Invocation.Builder builder = target.request();
        String response = builder.get(String.class);
        client.close();
        
        // Convert to DOM so we can handle this stuff
        Element domFeed;
        try {
            domFeed = XmlUtil.toDom(response);
        } catch (IOException e) {
            log.warn("Couldn't convert XML feed to DOM");
            return;
        }
        AtomParser parser = new AtomParser();
        List<Node> entryNodes = parser.removeEntryNodes(domFeed);
        for (Node node : entryNodes) {
            Element entry = (Element)node;
            String asString = XmlUtil.serializeDom(entry);
            try {
                entry = XmlUtil.toDom(asString);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                continue;
            }
            System.err.println(asString);
            String validMessage = " No signature found";
            try {
                boolean isValid = SignatureUtil.verifySignature(entry);
                validMessage = isValid ? " Signature valid!" : " INVALID signature!";
            } catch (XMLSignatureException e) {
                log.warn("Couldn't verify signature on entry");
            }
            String content = parser.getTitle(entry) + validMessage;
            entries.add(content);
        }
    }
    
    public void loadFeedForUrl(final String url) {
        new Thread(() -> {
            SyndFeed feed = null;
            InputStream is = null;
            try {
                URLConnection openConnection = new URL(url).openConnection();
                is = new URL(url).openConnection().getInputStream();
                if ("gzip".equals(openConnection.getContentEncoding())) {
                    is = new GZIPInputStream(is);
                }
                InputSource source = new InputSource(is);
                SyndFeedInput input = new SyndFeedInput();
                feed = input.build(source);

                feed.getModules().forEach(module -> {
                    if (module instanceof FeedModule) {
                        FeedModule trsst = (FeedModule) module;
                        // TODO: Publishing of feed needs sign and encrypt key set on the Feed element
                        // even if only publishing Signed feed with AtomSigner
                        log.info("Sign: " + trsst.getSignKey());
                        log.info("Encrypt: " + trsst.getEncryptKey());
                    }
                });
                feed.getEntries().forEach(entry -> {
                    StringBuilder entryContent = new StringBuilder();
                    entryContent.append(entry.getTitle());
                    log.info("Entry found: " + entryContent);
                    
                    List<Module> modules = entry.getModules();
                    modules.forEach(module -> {
                        if (module instanceof EntryModule) {
                            EntryModule trsst = (EntryModule) module;
                            log.info("...Predecessor: " + trsst.getPredecessorValue());
                            log.info("...Signed entry: " + trsst.isSigned());
                            log.info("...Encryped entry: " + trsst.isEncrypted());
                            // TODO: Check for encrypted here 
                            // TODO: Check for signature valid
                            if (trsst.isSigned()) {
                                entryContent.append("Signature valid: " + trsst.isSignatureValid());
                            }
                        }
                    });
                    
                    Platform.runLater(() -> {
                        entries.add(entryContent.toString());
                    });
                });

            } catch (Exception e) {
                log.warn("Feed could not be parsed");
                log.warn(e.getMessage());
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();

    }

    public void setBottomPane(BorderPane pane) {
        feedPane.setBottom(pane);
    }

    public void clearBottomPane() {
        setBottomPane(null);
    }

    public class HtmlFormatCell extends ListCell<String> {

        public HtmlFormatCell() {
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            System.out.println("Item: " + item);
            System.out.println("Is empty: " + empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (item != null) {
                if (item.contains("<p>")) {
                    WebView web = new WebView();
                    WebEngine engine = web.getEngine();
                    engine.loadContent(item);
                    web.setPrefHeight(50);
                    web.setPrefWidth(300);
                    web.autosize();
                    web.setBlendMode(BlendMode.MULTIPLY);
                    setText(null);
                    setGraphic(web);
                } else {
                    setText(item == null ? "" : "-" + item);
                    setTextFill(Color.BLUE);
                    if (isSelected()) {
                        setTextFill(Color.GREEN);
                    }
                    setGraphic(null);
                }

            }
        }
    }
}
