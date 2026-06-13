package com.linkx.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ChatController {

    @FXML private ListView<String> sessionList;
    @FXML private ListView<String> messageList;
    @FXML private TextField messageInput;
    @FXML private Button sendBtn;
    @FXML private Label chatTargetLabel;

    private Long currentTargetId;

    @FXML
    public void initialize() {
        sendBtn.setOnAction(e -> handleSend());

        sessionList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectSession(newVal);
            }
        });

        loadSessions();
    }

    private void loadSessions() {
        new Thread(() -> {
            try {
                java.util.List<ApiClient.SessionData> sessions = ApiClient.getSessions();
                Platform.runLater(() -> {
                    sessionList.getItems().clear();
                    for (ApiClient.SessionData s : sessions) {
                        String display = s.targetNickname + " - " + (s.lastMessage != null ? s.lastMessage : "");
                        sessionList.getItems().add(display);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void selectSession(String sessionStr) {
        new Thread(() -> {
            try {
                java.util.List<ApiClient.SessionData> sessions = ApiClient.getSessions();
                for (ApiClient.SessionData s : sessions) {
                    if (sessionStr.startsWith(s.targetNickname)) {
                        currentTargetId = s.targetId;
                        Platform.runLater(() -> {
                            chatTargetLabel.setText("与 " + s.targetNickname + " 的对话");
                            loadMessages(s.targetId);
                        });
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void loadMessages(Long targetId) {
        new Thread(() -> {
            try {
                java.util.List<ApiClient.MessageData> messages = ApiClient.getChatHistory(targetId);
                Platform.runLater(() -> {
                    messageList.getItems().clear();
                    for (ApiClient.MessageData m : messages) {
                        String prefix = m.fromUserId.equals(ApiClient.getUserId()) ? "我" : m.fromNickname;
                        String display = prefix + ": " + m.content;
                        messageList.getItems().add(display);
                    }
                });
                ApiClient.markAsRead(targetId);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleSend() {
        if (currentTargetId == null) {
            return;
        }

        String content = messageInput.getText().trim();
        if (content.isEmpty()) {
            return;
        }

        sendBtn.setDisable(true);

        new Thread(() -> {
            try {
                ApiClient.sendMessage(currentTargetId, content);
                Platform.runLater(() -> {
                    messageInput.clear();
                    loadMessages(currentTargetId);
                    loadSessions();
                    sendBtn.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> sendBtn.setDisable(false));
                ex.printStackTrace();
            }
        }).start();
    }
}
