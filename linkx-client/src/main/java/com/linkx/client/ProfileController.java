package com.linkx.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;

public class ProfileController {

    @FXML private Text usernameText;
    @FXML private TextField nicknameField;
    @FXML private TextField signatureField;
    @FXML private ComboBox<String> genderBox;
    @FXML private TextField regionField;
    @FXML private Text createTimeText;
    @FXML private Button saveBtn;
    @FXML private Text messageText;

    @FXML
    public void initialize() {
        genderBox.setItems(FXCollections.observableArrayList("未知", "男", "女"));
        genderBox.getSelectionModel().select(0);

        saveBtn.setOnAction(e -> handleSave());

        loadProfile();
    }

    private void loadProfile() {
        new Thread(() -> {
            try {
                ApiClient.UserData user = ApiClient.getMyProfile();
                Platform.runLater(() -> {
                    usernameText.setText(user.username);
                    nicknameField.setText(user.nickname);
                    signatureField.setText(user.signature != null ? user.signature : "");
                    regionField.setText(user.region != null ? user.region : "");
                    createTimeText.setText(user.createTime != null ? user.createTime.substring(0, 10) : "");

                    if (user.gender != null) {
                        genderBox.getSelectionModel().select(user.gender);
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> messageText.setText("加载资料失败: " + ex.getMessage()));
            }
        }).start();
    }

    private void handleSave() {
        String nickname = nicknameField.getText().trim();
        String signature = signatureField.getText().trim();
        String region = regionField.getText().trim();
        int genderIndex = genderBox.getSelectionModel().getSelectedIndex();

        if (nickname.isEmpty()) {
            messageText.setText("昵称不能为空");
            return;
        }

        saveBtn.setDisable(true);
        saveBtn.setText("保存中...");

        new Thread(() -> {
            try {
                ApiClient.updateProfile(nickname, signature, genderIndex, region);
                Platform.runLater(() -> {
                    messageText.setText("保存成功");
                    messageText.setStyle("-fx-fill: green;");
                    saveBtn.setDisable(false);
                    saveBtn.setText("保存修改");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    messageText.setText("保存失败: " + ex.getMessage());
                    messageText.setStyle("-fx-fill: red;");
                    saveBtn.setDisable(false);
                    saveBtn.setText("保存修改");
                });
            }
        }).start();
    }
}
