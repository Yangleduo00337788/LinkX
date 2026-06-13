package com.linkx.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

            String[] parts = item.split("\\|", 5);
            if (parts.length < 5) {
                setGraphic(null);
                return;
            }

            String type = parts[0];
            String nickname = parts[1];
            String avatarUrl = parts[2];
            String time = parts[3];
            String content = parts[4];

            ImageView avatar = new ImageView();
            avatar.setFitWidth(40);
            avatar.setFitHeight(40);
            avatar.setPreserveRatio(true);
            avatar.setStyle("-fx-background-color: #ddd; -fx-background-radius: 20;");

            if (!avatarUrl.isEmpty()) {
                try {
                    avatar.setImage(new Image(avatarUrl, 40, 40, true, true));
                } catch (Exception e) {
                    avatar.setImage(null);
                }
            }

            TextFlow textFlow = new TextFlow(new Text(content));
            Text timeText = new Text(time);
            timeText.setStyle("-fx-font-size: 10px; -fx-fill: #999;");

            if ("ME".equals(type)) {
                textFlow.setStyle("-fx-padding: 8 12; -fx-background-color: #95ec69; -fx-background-radius: 10; -fx-wrap-text: true;");
                textFlow.setMaxWidth(250);

                VBox bubble = new VBox(textFlow, timeText);
                bubble.setSpacing(2);
                bubble.setAlignment(Pos.CENTER_RIGHT);

                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_RIGHT);
                row.setSpacing(8);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                row.getChildren().addAll(spacer, bubble, avatar);

                container.getChildren().add(row);
                container.setAlignment(Pos.CENTER_RIGHT);
            } else {
                textFlow.setStyle("-fx-padding: 8 12; -fx-background-color: white; -fx-background-radius: 10; -fx-wrap-text: true; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1);");
                textFlow.setMaxWidth(250);

                Text nameText = new Text(nickname);
                nameText.setStyle("-fx-font-size: 11px; -fx-fill: #999;");

                VBox bubble = new VBox(nameText, textFlow, timeText);
                bubble.setSpacing(2);
                bubble.setAlignment(Pos.CENTER_LEFT);

                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);
                row.setSpacing(8);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                row.getChildren().addAll(avatar, bubble, spacer);

                container.getChildren().add(row);
                container.setAlignment(Pos.CENTER_LEFT);
            }

            setGraphic(container);
        }
    }
}
