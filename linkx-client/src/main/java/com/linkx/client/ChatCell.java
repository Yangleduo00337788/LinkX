package com.linkx.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ChatCell extends ListCell<String> {

    private final VBox container = new VBox();

    public ChatCell() {
        container.setPadding(new Insets(2, 10, 2, 10));
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            container.getChildren().clear();

            if (item.startsWith("我: ")) {
                String msg = item.substring(3);
                TextFlow textFlow = new TextFlow(new Text(msg));
                textFlow.setStyle("-fx-padding: 8 12; -fx-background-color: #95ec69; -fx-background-radius: 10; -fx-wrap-text: true;");
                textFlow.setMaxWidth(280);

                VBox bubble = new VBox(textFlow);
                bubble.setAlignment(Pos.CENTER_RIGHT);

                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_RIGHT);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                row.getChildren().addAll(spacer, bubble);

                container.getChildren().add(row);
                container.setAlignment(Pos.CENTER_RIGHT);
            } else {
                int firstColon = item.indexOf(": ");
                int secondColon = item.indexOf(": ", firstColon + 2);
                String name = item.substring(0, firstColon);
                String time = item.substring(firstColon + 2, secondColon);
                String msg = item.substring(secondColon + 2);

                Text nameText = new Text(name);
                nameText.setStyle("-fx-font-size: 11px; -fx-fill: #999;");

                TextFlow textFlow = new TextFlow(new Text(msg));
                textFlow.setStyle("-fx-padding: 8 12; -fx-background-color: white; -fx-background-radius: 10; -fx-wrap-text: true; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1);");
                textFlow.setMaxWidth(280);

                Text timeText = new Text(time);
                timeText.setStyle("-fx-font-size: 10px; -fx-fill: #ccc;");

                VBox bubble = new VBox(nameText, textFlow, timeText);
                bubble.setSpacing(2);
                bubble.setAlignment(Pos.CENTER_LEFT);

                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                row.getChildren().addAll(bubble, spacer);

                container.getChildren().add(row);
                container.setAlignment(Pos.CENTER_LEFT);
            }

            setGraphic(container);
        }
    }
}
