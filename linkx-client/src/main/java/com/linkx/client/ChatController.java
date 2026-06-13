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
    private Timer refreshTimer;
    private ObservableList<String> sessionItems = FXCollections.observableArrayList();
    private ObservableList<String> messageItems = FXCollections.observableArrayList();
    private java.util.Map<String, Long> sessionMap = new java.util.HashMap<>();

    @FXML
    public void initialize() {
        sendBtn.setOnAction(e -> handleSend());
        messageInput.setOnAction(e -> handleSend());

        sessionList.setItems(sessionItems);
        messageList.setItems(messageItems);
        messageList.setCellFactory(list -> new ChatCell());

        sessionList.setOnMouseClicked(event -> {
            String selected = sessionList.getSelectionModel().getSelectedItem();
            if (selected != null && sessionMap.containsKey(selected)) {
                currentTargetId = sessionMap.get(selected);
                String name = selected.split(" - ")[0].replaceAll(" \\[\\d+\\]", "");
                chatTargetLabel.setText(name);
                loadMessages(currentTargetId);
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

    private void loadSessions() {
        new Thread(() -> {
            try {
                List<ApiClient.SessionData> sessions = ApiClient.getSessions();
                Platform.runLater(() -> {
                    String selectedBefore = sessionList.getSelectionModel().getSelectedItem();
                    sessionItems.clear();
                    sessionMap.clear();
                    for (ApiClient.SessionData s : sessions) {
                        String unread = s.unreadCount > 0 ? " [" + s.unreadCount + "]" : "";
                        String display = s.targetNickname + unread + " - " + (s.lastMessage != null ? s.lastMessage : "");
                        sessionItems.add(display);
                        sessionMap.put(display, s.targetId);
                    }
                    if (selectedBefore != null && sessionMap.containsKey(selectedBefore)) {
                        int idx = sessionItems.indexOf(selectedBefore);
                        if (idx >= 0) sessionList.getSelectionModel().select(idx);
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
                ApiClient.UserData myProfile = ApiClient.getMyProfile();
                String myAvatar = (myProfile != null && myProfile.avatar != null) ? myProfile.avatar : "";
                String myNickname = (myProfile != null && myProfile.nickname != null) ? myProfile.nickname : "我";

                Platform.runLater(() -> {
                    messageItems.clear();
                    Long myId = ApiClient.getUserId();
                    for (ApiClient.MessageData m : messages) {
                        String time = "";
                        if (m.createTime != null && m.createTime.length() >= 16) {
                            time = m.createTime.substring(11, 16);
                        }
                        if (m.fromUserId != null && m.fromUserId.equals(myId)) {
                            String read = (m.status != null && m.status == 1) ? "已读" : "已送达";
                            messageItems.add("ME|" + myNickname + "|" + myAvatar + "|" + time + "|" + m.content + "|" + read);
                        } else {
                            String name = m.fromNickname != null ? m.fromNickname : "对方";
                            messageItems.add("OTHER|" + name + "||" + time + "|" + m.content + "|");
                        }
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
        if (currentTargetId == null) return;
        String content = messageInput.getText().trim();
        if (content.isEmpty()) return;

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
