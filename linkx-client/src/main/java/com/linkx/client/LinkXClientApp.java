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
        Scene scene = new Scene(root, 400, 500);
        scene.getStylesheets().add(LinkXClientApp.class.getResource("/css/style.css").toExternalForm());
        primaryStage.setTitle("LinkX IM - 登录");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void showRegister() throws Exception {
        Parent root = FXMLLoader.load(LinkXClientApp.class.getResource("/fxml/register.fxml"));
        Scene scene = new Scene(root, 400, 550);
        scene.getStylesheets().add(LinkXClientApp.class.getResource("/css/style.css").toExternalForm());
        primaryStage.setTitle("LinkX IM - 注册");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void showMain() throws Exception {
        Parent root = FXMLLoader.load(LinkXClientApp.class.getResource("/fxml/main.fxml"));
        Scene scene = new Scene(root, 960, 650);
        scene.getStylesheets().add(LinkXClientApp.class.getResource("/css/style.css").toExternalForm());
        primaryStage.setTitle("LinkX IM");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
