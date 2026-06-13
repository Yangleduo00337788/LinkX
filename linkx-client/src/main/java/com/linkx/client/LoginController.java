package com.linkx.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginBtn;
    @FXML private Button registerBtn;
    @FXML private Label errorText;

    @FXML
    public void initialize() {
        loginBtn.setOnAction(e -> handleLogin());
        registerBtn.setOnAction(e -> handleRegister());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorText.setText("请输入用户名和密码");
            return;
        }

        loginBtn.setDisable(true);
        loginBtn.setText("登录中...");

        new Thread(() -> {
            try {
                ApiClient.AuthData data = ApiClient.login(username, password);
                Platform.runLater(() -> {
                    try {
                        LinkXClientApp.showMain();
                    } catch (Exception ex) {
                        errorText.setText("打开主界面失败");
                        loginBtn.setDisable(false);
                        loginBtn.setText("登录");
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    errorText.setText(ex.getMessage());
                    loginBtn.setDisable(false);
                    loginBtn.setText("登录");
                });
            }
        }).start();
    }

    private void handleRegister() {
        try {
            LinkXClientApp.showRegister();
        } catch (Exception ex) {
            errorText.setText("打开注册页面失败");
        }
    }
}
