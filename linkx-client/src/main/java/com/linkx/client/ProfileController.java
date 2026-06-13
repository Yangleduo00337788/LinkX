package com.linkx.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;

public class ProfileController {

    @FXML private Label usernameText;
    @FXML private TextField nicknameField;
    @FXML private ComboBox<String> genderBox;
    @FXML private Label createTimeText;
    @FXML private Button saveBtn;
    @FXML private Button changeAvatarBtn;
    @FXML private ImageView avatarView;
    @FXML private Label messageText;

    private String currentAvatarUrl;

    @FXML
    public void initialize() {
        genderBox.setItems(FXCollections.observableArrayList("未知", "男", "女"));
        genderBox.getSelectionModel().select(0);

        saveBtn.setOnAction(e -> handleSave());
        changeAvatarBtn.setOnAction(e -> handleChangeAvatar());

        loadProfile();
    }

    private void loadProfile() {
        new Thread(() -> {
            try {
                ApiClient.UserData user = ApiClient.getMyProfile();
                Platform.runLater(() -> {
                    usernameText.setText(user.username);
                    nicknameField.setText(user.nickname);
                    createTimeText.setText(user.createTime != null ? user.createTime.substring(0, 10) : "");

                    if (user.gender != null) {
                        genderBox.getSelectionModel().select(user.gender);
                    }

                    if (user.avatar != null && !user.avatar.isEmpty()) {
                        currentAvatarUrl = user.avatar;
                        loadAvatar(user.avatar);
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> messageText.setText("加载资料失败: " + ex.getMessage()));
            }
        }).start();
    }

    private void loadAvatar(String url) {
        try {
            Image image = new Image(url, 80, 80, true, true);
            avatarView.setImage(image);
        } catch (Exception e) {
            System.out.println("加载头像失败: " + e.getMessage());
        }
    }

    private void handleChangeAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择头像");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(avatarView.getScene().getWindow());
        if (file != null) {
            new Thread(() -> {
                try {
                    String url = ApiClient.uploadAvatar(file.getAbsolutePath());
                    currentAvatarUrl = url;
                    Platform.runLater(() -> {
                        loadAvatar(url);
                        messageText.setText("头像上传成功");
                        messageText.setStyle("-fx-text-fill: green;");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        messageText.setText("上传失败: " + ex.getMessage());
                        messageText.setStyle("-fx-text-fill: red;");
                    });
                }
            }).start();
        }
    }

    private void handleSave() {
        String nickname = nicknameField.getText().trim();
        int genderIndex = genderBox.getSelectionModel().getSelectedIndex();

        if (nickname.isEmpty()) {
            messageText.setText("昵称不能为空");
            return;
        }

        saveBtn.setDisable(true);
        saveBtn.setText("保存中...");

        new Thread(() -> {
            try {
                ApiClient.updateProfile(nickname, genderIndex);
                Platform.runLater(() -> {
                    messageText.setText("保存成功");
                    messageText.setStyle("-fx-text-fill: green;");
                    saveBtn.setDisable(false);
                    saveBtn.setText("保存修改");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    messageText.setText("保存失败: " + ex.getMessage());
                    messageText.setStyle("-fx-text-fill: red;");
                    saveBtn.setDisable(false);
                    saveBtn.setText("保存修改");
                });
            }
        }).start();
    }
}
