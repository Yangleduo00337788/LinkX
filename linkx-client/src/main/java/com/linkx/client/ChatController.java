package com.linkx.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Timer;
import java.util.TimerTask;

public class ChatController {

    @FXML private ListView<String> sessionList;
    @FXML private ListView<String> messageList;
    @FXML private TextField messageInput;
    @FXML private Button sendBtn;
    @FXML private Label chatTargetLabel;

    private Long currentTargetId;
    private String currentTargetName;
    private Timer refreshTimer;

    @FXML
    public void initialize() {
        sendBtn.setOnAction(e -> handleSend());

        sessionList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                handleSessionClick(newVal);
            }
        });

        loadSessions();

        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    loadSessions();
                    if (currentTargetId != null) {
                        loadMessages(currentTargetId);
                    }
                });
            }
        }, 3000, 3000);
    }

    private void handleSessionClick(String sessionStr) {
        new Thread(() -> {
            try {
                java.util.List<ApiClient.SessionData> sessions = ApiClient.getSessions();
                for (ApiClient.SessionData s : sessions) {
                    String unread = s.unreadCount > 0 ? " [" + s.unreadCount + "]" : "";
                    String display = s.targetNickname + unread + " - " + (s.lastMessage != null ? s.lastMessage : "");
                    if (display.equals(sessionStr)) {
                        currentTargetId = s.targetId;
                        currentTargetName = s.targetNickname;
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

    private void loadSessions() {
        new Thread(() -> {
            try {
                java.util.List<ApiClient.SessionData> sessions = ApiClient.getSessions();
                int selectedIndex = sessionList.getSelectionModel().getSelectedIndex();
                Platform.runLater(() -> {
                    sessionList.getItems().clear();
                    for (ApiClient.SessionData s : sessions) {
                        String unread = s.unreadCount > 0 ? " [" + s.unreadCount + "]" : "";
                        String display = s.targetNickname + unread + " - " + (s.lastMessage != null ? s.lastMessage : "");
                        sessionList.getItems().add(display);
                    }
                    if (selectedIndex >= 0 && selectedIndex < sessionList.getItems().size()) {
                        sessionList.getSelectionModel().select(selectedIndex);
                    }
                });
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
                    Long myId = ApiClient.getUserId();
                    for (ApiClient.MessageData m : messages) {
                        String prefix;
                        if (m.fromUserId != null && m.fromUserId.equals(myId)) {
                            prefix = "我";
                        } else {
                            prefix = m.fromNickname != null ? m.fromNickname : "对方";
                        }
                        String display = prefix + ": " + m.content;
                        messageList.getItems().add(display);
                    }
                    if (!messageList.getItems().isEmpty()) {
                        messageList.scrollTo(messageList.getItems().size() - 1);
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

    public void shutdown() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
    }
}
