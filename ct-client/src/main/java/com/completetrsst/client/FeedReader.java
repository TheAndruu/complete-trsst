package com.completetrsst.client;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.completetrsst.atom.AtomParser;
import com.completetrsst.constants.Namespaces;
import com.completetrsst.constants.Nodes;
import com.completetrsst.crypto.keys.TrsstKeyFunctions;
import com.completetrsst.xml.XmlUtil;

/**
 * A login form to demonstrate lambdas, properties, and bindings.
 * 
 */
public class FeedReader extends Application {

    private static final Color foregroundColor = Color.rgb(255, 255, 255, .9);
    private static final Color backgroundColor = Color.rgb(0, 0, 0, .55);

    @Override
    public void start(Stage primaryStage) {
        // create a transparent stage
        primaryStage.initStyle(StageStyle.DECORATED);
        Group root = new Group();
        Scene scene = new Scene(root, 350, 650, Color.rgb(0, 0, 0, 0));
        primaryStage.setScene(scene);

        Rectangle background = new Rectangle(350, 650);
        background.setX(0);
        background.setY(0);
        background.setArcHeight(14);
        background.setArcWidth(14);
        background.setFill(backgroundColor);
        background.setStrokeWidth(1.5);
        background.setStroke(foregroundColor);

        VBox formLayout = createRow3Dom(primaryStage);

        formLayout.prefWidthProperty().bind(primaryStage.widthProperty().subtract(45));

        formLayout.setLayoutX(12);
        formLayout.setLayoutY(12);
        root.getChildren().addAll(background, formLayout);
        primaryStage.show();
    }

    private VBox createRow3Dom(Stage parentStage) {

        VBox row = new VBox(4);

        // WebView browser = new WebView();
        WebEngine webEngine = new WebEngine();

        AtomParser parser = new AtomParser();

        final List<Text> textBoxes = new ArrayList<Text>();
        final ChangeListener<State> listener = new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
                if (newValue == Worker.State.SUCCEEDED) {
                    // finished loading...
                    org.w3c.dom.Document xmlDom = webEngine.getDocument();
                    Element root = xmlDom.getDocumentElement();
                    try {
                        // TODO: Maybe do all parsing with JDOM since different implementaions make such a difference?
                        root = XmlUtil.toDom(XmlUtil.toJdom(root));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    List<Node> entryNodes = parser.removeEntryNodes(root);

                    // root contains 'feed' now, and 'entryNodes' are self-explanatory

                    Text textBox = createTextBox("Feed: " + TrsstKeyFunctions.removeFeedUrnPrefix(parser.getId(root)));
                    textBoxes.add(textBox);

                    entryNodes.forEach(node -> {
                        Element entryElement = (Element) node;
                        // TODO: check if 'encrypted' or content exists, and use those appropriately
                            Node titleNode = parser.getFirstNode(entryElement, Namespaces.ATOM_XMLNS, Nodes.ATOM_TITLE);
                            String title = titleNode.getTextContent();
                            Text text = createTextBox(title);
                            textBoxes.add(text);

                        });
                    row.getChildren().addAll(textBoxes);
                 // Note: Removing listener after done
                    webEngine.getLoadWorker().stateProperty().removeListener(this);
                } else if (newValue == Worker.State.FAILED) {
                    webEngine.getLoadWorker().stateProperty().removeListener(this);
                }
            }

        };
        webEngine.getLoadWorker().stateProperty().addListener(listener); // addListener()
        // begin loading...
        webEngine.load("http://localhost:8080/feed/8TxKTU9YELxsq5tzADU6Y5zqm44fkNJhy");

        // sample feed
        // http://localhost:8080/feed/8TxKTU9YELxsq5tzADU6Y5zqm44fkNJhy

        // row.prefWidthProperty().bind(parentStage.widthProperty().subtract(45));
        // row.prefHeightProperty().bind(parentStage.heightProperty().subtract(45));
        row.setPrefWidth(300);
        // row.setPrefHeight(900);

        return row;
    }

    private Text createTextBox(String text) {
        Text textBox = new Text();
        textBox.setFont(Font.font("SanSerif", FontWeight.BOLD, 10));
        textBox.setFill(Color.CYAN);
        // textBox.setStroke(foregroundColor);
        // textBox.setStrokeWidth(1.5);
        textBox.setSmooth(true);
        textBox.setText(text);
        return textBox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}