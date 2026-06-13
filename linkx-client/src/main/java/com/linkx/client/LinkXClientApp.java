package com.linkx.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LinkXClientApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showLogin();
    }

    public static void showLogin() throws Exception {
        Parent root = FXMLLoader.load(LinkXClientApp.class.getResource("/fxml/login.fxml"));
        primaryStage.setTitle("LinkX IM - 登录");
        primaryStage.setScene(new Scene(root, 400, 500));
        primaryStage.show();
    }

    public static void showRegister() throws Exception {
        Parent root = FXMLLoader.load(LinkXClientApp.class.getResource("/fxml/register.fxml"));
        primaryStage.setTitle("LinkX IM - 注册");
        primaryStage.setScene(new Scene(root, 400, 550));
        primaryStage.show();
    }

    public static void showMain() throws Exception {
        Parent root = FXMLLoader.load(LinkXClientApp.class.getResource("/fxml/main.fxml"));
        primaryStage.setTitle("LinkX IM");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
