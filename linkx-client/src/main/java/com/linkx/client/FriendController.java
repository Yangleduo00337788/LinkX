package com.linkx.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class FriendController {

    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private Button refreshBtn;
    @FXML private ListView<String> friendListView;
    @FXML private ListView<String> requestListView;
    @FXML private ListView<String> searchResultView;
    @FXML private Label statusText;

    @FXML
    public void initialize() {
        searchBtn.setOnAction(e -> handleSearch());
        refreshBtn.setOnAction(e -> loadAll());

        loadAll();
    }

    private void loadAll() {
        loadFriends();
        loadRequests();
    }

    private void loadFriends() {
        new Thread(() -> {
            try {
                java.util.List<ApiClient.FriendData> friends = ApiClient.getFriendList();
                Platform.runLater(() -> {
                    friendListView.getItems().clear();
                    for (ApiClient.FriendData f : friends) {
                        String display = f.friendNickname + " (@" + f.friendUsername + ")";
                        friendListView.getItems().add(display);
                    }
                    statusText.setText("好友数: " + friends.size());
                });
            } catch (Exception ex) {
                Platform.runLater(() -> statusText.setText("加载好友失败: " + ex.getMessage()));
            }
        }).start();
    }

    private void loadRequests() {
        new Thread(() -> {
            try {
                java.util.List<ApiClient.FriendRequestData> requests = ApiClient.getFriendRequests();
                Platform.runLater(() -> {
                    requestListView.getItems().clear();
                    for (ApiClient.FriendRequestData r : requests) {
                        String display = r.fromNickname + " (@" + r.fromUsername + ") - " + (r.message != null ? r.message : "无验证消息");
                        requestListView.getItems().add(display);
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> statusText.setText("加载申请失败: " + ex.getMessage()));
            }
        }).start();
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            statusText.setText("请输入搜索关键词");
            return;
        }

        searchBtn.setDisable(true);
        new Thread(() -> {
            try {
                java.util.List<ApiClient.UserData> users = ApiClient.searchUsers(keyword);
                Platform.runLater(() -> {
                    searchResultView.getItems().clear();
                    for (ApiClient.UserData u : users) {
                        String display = u.nickname + " (@" + u.username + ") [ID: " + u.id + "]";
                        searchResultView.getItems().add(display);
                    }
                    searchBtn.setDisable(false);
                    statusText.setText("搜索到 " + users.size() + " 个用户");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusText.setText("搜索失败: " + ex.getMessage());
                    searchBtn.setDisable(false);
                });
            }
        }).start();
    }
}
