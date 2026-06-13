package com.linkx.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Button profileBtn;
    @FXML private Button contactsBtn;
    @FXML private Button chatBtn;
    @FXML private Button logoutBtn;

    @FXML
    public void initialize() {
        profileBtn.setOnAction(e -> showProfile());
        contactsBtn.setOnAction(e -> showContacts());
        chatBtn.setOnAction(e -> showChat());
        logoutBtn.setOnAction(e -> handleLogout());

        // 默认显示资料页
        showProfile();
    }

    private void showProfile() {
        try {
            Parent profile = FXMLLoader.load(getClass().getResource("/fxml/profile.fxml"));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showContacts() {
        try {
            Parent friends = FXMLLoader.load(getClass().getResource("/fxml/friends.fxml"));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(friends);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showChat() {
        // TODO: Sprint 4 单聊系统
        contentArea.getChildren().clear();
        javafx.scene.control.Label label = new javafx.scene.control.Label("消息功能开发中...");
        label.setStyle("-fx-font-size: 18px; -fx-text-fill: #999;");
        contentArea.getChildren().add(label);
    }

    private void handleLogout() {
        ApiClient.setTokens(null, null, null);
        try {
            LinkXClientApp.showLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
