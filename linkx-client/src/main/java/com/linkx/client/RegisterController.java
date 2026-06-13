package com.linkx.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField nicknameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerBtn;
    @FXML private Button backBtn;
    @FXML private Text errorText;

    @FXML
    public void initialize() {
        registerBtn.setOnAction(e -> handleRegister());
        backBtn.setOnAction(e -> handleBack());
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String nickname = nicknameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (username.isEmpty() || nickname.isEmpty() || password.isEmpty()) {
            errorText.setText("请填写所有字段");
            return;
        }

        if (username.length() < 3) {
            errorText.setText("用户名至少3位");
            return;
        }

        if (password.length() < 6) {
            errorText.setText("密码至少6位");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorText.setText("两次密码不一致");
            return;
        }

        registerBtn.setDisable(true);
        registerBtn.setText("注册中...");

        new Thread(() -> {
            try {
                ApiClient.register(username, nickname, password);
                Platform.runLater(() -> {
                    try {
                        LinkXClientApp.showMain();
                    } catch (Exception ex) {
                        errorText.setText("打开主界面失败");
                        registerBtn.setDisable(false);
                        registerBtn.setText("注册");
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    errorText.setText(ex.getMessage());
                    registerBtn.setDisable(false);
                    registerBtn.setText("注册");
                });
            }
        }).start();
    }

    private void handleBack() {
        try {
            LinkXClientApp.showLogin();
        } catch (Exception ex) {
            errorText.setText("返回登录页面失败");
        }
    }
}
