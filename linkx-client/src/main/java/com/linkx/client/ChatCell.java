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
        setPadding(new Insets(2, 0, 2, 0));
        setPrefWidth(USE_COMPUTED_SIZE);
        setMaxWidth(Double.MAX_VALUE);
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

        Text contentText = new Text(content);
        contentText.setStyle("-fx-font-size: 14px;");
        TextFlow textFlow = new TextFlow(contentText);
        textFlow.setMaxWidth(280);
        textFlow.setLineSpacing(2);

        Text timeText = new Text(time);
        timeText.setStyle("-fx-font-size: 10px; -fx-fill: #bfbfbf;");

        if ("ME".equals(type)) {
            textFlow.setStyle("-fx-padding: 10 14; -fx-background-color: #95ec69; -fx-background-radius: 12;");

            Text readText = new Text(" " + readStatus);
            readText.setStyle("-fx-font-size: 10px; -fx-fill: #1890ff;");

            HBox timeRow = new HBox(2, timeText, readText);
            timeRow.setAlignment(Pos.CENTER_RIGHT);

            VBox bubble = new VBox(2, textFlow, timeRow);
            bubble.setAlignment(Pos.CENTER_RIGHT);

            HBox row = new HBox(6, bubble, avatar);
            row.setAlignment(Pos.CENTER_RIGHT);
            row.setPadding(new Insets(0, 12, 0, 50));

            setGraphic(row);
        } else {
            textFlow.setStyle("-fx-padding: 10 14; -fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 3, 0, 0, 1);");

            Text nameText = new Text(nickname);
            nameText.setStyle("-fx-font-size: 11px; -fx-fill: #bfbfbf;");

            HBox timeRow = new HBox(2, timeText);
            timeRow.setAlignment(Pos.CENTER_LEFT);

            VBox bubble = new VBox(2, nameText, textFlow, timeRow);
            bubble.setAlignment(Pos.CENTER_LEFT);

            HBox row = new HBox(6, avatar, bubble);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(0, 50, 0, 12));

            setGraphic(row);
        }
    }

    private ImageView createAvatar(String url) {
        ImageView avatar = new ImageView();
        avatar.setFitWidth(AVATAR_SIZE);
        avatar.setFitHeight(AVATAR_SIZE);
        avatar.setPreserveRatio(true);
        avatar.setSmooth(true);
        avatar.setStyle("-fx-background-radius: 18; -fx-background-color: #e6e6e6;");

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
