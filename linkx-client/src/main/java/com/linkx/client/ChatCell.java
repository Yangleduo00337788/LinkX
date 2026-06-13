package com.linkx.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ChatCell extends ListCell<String> {

    private final Text text = new Text();
    private final TextFlow textFlow = new TextFlow(text);
    private final VBox bubble = new VBox(textFlow);
    private final HBox container = new HBox(bubble);

    public ChatCell() {
        textFlow.setStyle("-fx-padding: 8 12; -fx-background-radius: 10; -fx-wrap-text: true;");
        bubble.setMaxWidth(300);
        container.setPadding(new Insets(4, 10, 4, 10));
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            text.setText(item);

            if (item.startsWith("我: ")) {
                String msg = item.substring(3);
                text.setText(msg);
                textFlow.setStyle("-fx-padding: 8 12; -fx-background-color: #95ec69; -fx-background-radius: 10; -fx-wrap-text: true;");
                bubble.setAlignment(Pos.CENTER_RIGHT);
                container.setAlignment(Pos.CENTER_RIGHT);
                container.getChildren().clear();
                container.getChildren().add(bubble);
            } else {
                int colonIdx = item.indexOf(": ");
                String name = item.substring(0, colonIdx);
                String msg = item.substring(colonIdx + 2);
                text.setText(msg);
                textFlow.setStyle("-fx-padding: 8 12; -fx-background-color: white; -fx-background-radius: 10; -fx-wrap-text: true;");
                bubble.setAlignment(Pos.CENTER_LEFT);
                container.setAlignment(Pos.CENTER_LEFT);
                container.getChildren().clear();

                VBox leftBox = new VBox();
                Text nameText = new Text(name);
                nameText.setStyle("-fx-font-size: 11px; -fx-fill: #999; -fx-padding: 0 0 2 10;");
                leftBox.getChildren().addAll(nameText, bubble);
                container.getChildren().add(leftBox);
            }

            setGraphic(container);
        }
    }
}
