package com.linkx.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
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
    private ObservableList<String> sessionItems = FXCollections.observableArrayList();
    private ObservableList<String> messageItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        sendBtn.setOnAction(e -> handleSend());

        sessionList.setItems(sessionItems);
        messageList.setItems(messageItems);

        sessionList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                String selected = sessionList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    selectSession(selected);
                }
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

    private void selectSession(String displayText) {
        new Thread(() -> {
            try {
                List<ApiClient.SessionData> sessions = ApiClient.getSessions();
                for (ApiClient.SessionData s : sessions) {
                    String unread = s.unreadCount > 0 ? " [" + s.unreadCount + "]" : "";
                    String sessionDisplay = s.targetNickname + unread + " - " + (s.lastMessage != null ? s.lastMessage : "");
                    if (sessionDisplay.equals(displayText)) {
                        currentTargetId = s.targetId;
                        currentTargetName = s.targetNickname;
                        Platform.runLater(() -> {
                            chatTargetLabel.setText("与 " + s.targetNickname + " 的对话");
                        });
                        loadMessages(s.targetId);
                        return;
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
                List<ApiClient.SessionData> sessions = ApiClient.getSessions();
                Platform.runLater(() -> {
                    String selectedBefore = sessionList.getSelectionModel().getSelectedItem();
                    sessionItems.clear();
                    for (ApiClient.SessionData s : sessions) {
                        String unread = s.unreadCount > 0 ? " [" + s.unreadCount + "]" : "";
                        String display = s.targetNickname + unread + " - " + (s.lastMessage != null ? s.lastMessage : "");
                        sessionItems.add(display);
                    }
                    if (selectedBefore != null) {
                        int idx = sessionItems.indexOf(selectedBefore);
                        if (idx >= 0) {
                            sessionList.getSelectionModel().select(idx);
                        }
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
                List<ApiClient.MessageData> messages = ApiClient.getChatHistory(targetId);
                Platform.runLater(() -> {
                    messageItems.clear();
                    Long myId = ApiClient.getUserId();
                    for (ApiClient.MessageData m : messages) {
                        String prefix;
                        if (m.fromUserId != null && m.fromUserId.equals(myId)) {
                            prefix = "我";
                        } else {
                            prefix = m.fromNickname != null ? m.fromNickname : "对方";
                        }
                        String display = prefix + ": " + m.content;
                        messageItems.add(display);
                    }
                    if (!messageItems.isEmpty()) {
                        messageList.scrollTo(messageItems.size() - 1);
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
