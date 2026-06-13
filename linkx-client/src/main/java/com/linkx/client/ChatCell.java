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

    private static final int AVATAR_SIZE = 36;

    public ChatCell() {
        setPadding(new Insets(0));
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        String[] parts = item.split("\\|", 6);
        if (parts.length < 6) {
            setGraphic(null);
            return;
        }

        String type = parts[0];
        String nickname = parts[1];
        String avatarUrl = parts[2];
        String time = parts[3];
        String content = parts[4];
        String readStatus = parts[5];

        ImageView avatar = createAvatar(avatarUrl);
        TextFlow textFlow = new TextFlow(new Text(content));
        textFlow.setMaxWidth(260);

        Text timeText = new Text("  " + time);
        timeText.setStyle("-fx-font-size: 10px; -fx-fill: #aaa;");

        HBox timeRow = new HBox(4);
        timeRow.setAlignment(Pos.CENTER_RIGHT);

        if ("ME".equals(type)) {
            Text readText = new Text(readStatus);
            readText.setStyle("-fx-font-size: 10px; -fx-fill: #1890ff;");
            timeRow.getChildren().addAll(timeText, readText);

            textFlow.setStyle("-fx-padding: 8 14; -fx-background-color: #95ec69; -fx-background-radius: 12; -fx-wrap-text: true;");

            VBox bubbleBox = new VBox(2, textFlow, timeRow);
            bubbleBox.setAlignment(Pos.CENTER_RIGHT);
            bubbleBox.setMaxWidth(300);

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_RIGHT);
            row.setPadding(new Insets(2, 12, 2, 60));
            row.getChildren().addAll(bubbleBox, avatar);

            setGraphic(row);
        } else {
            Text nameText = new Text(nickname);
            nameText.setStyle("-fx-font-size: 11px; -fx-fill: #999;");

            timeRow.getChildren().add(timeText);
            timeRow.setAlignment(Pos.CENTER_LEFT);

            textFlow.setStyle("-fx-padding: 8 14; -fx-background-color: white; -fx-background-radius: 12; -fx-wrap-text: true; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 3, 0, 0, 1);");

            VBox bubbleBox = new VBox(2, nameText, textFlow, timeRow);
            bubbleBox.setAlignment(Pos.CENTER_LEFT);
            bubbleBox.setMaxWidth(300);

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(2, 60, 2, 12));
            row.getChildren().addAll(avatar, bubbleBox);

            setGraphic(row);
        }
    }

    private ImageView createAvatar(String url) {
        ImageView avatar = new ImageView();
        avatar.setFitWidth(AVATAR_SIZE);
        avatar.setFitHeight(AVATAR_SIZE);
        avatar.setPreserveRatio(true);
        avatar.setSmooth(true);
        avatar.setStyle("-fx-background-radius: 18; -fx-background-color: #e8e8e8;");

        if (url != null && !url.isEmpty()) {
            try {
                avatar.setImage(new Image(url, AVATAR_SIZE, AVATAR_SIZE, true, true));
            } catch (Exception e) {
                // default gray
            }
        }
        return avatar;
    }
}
