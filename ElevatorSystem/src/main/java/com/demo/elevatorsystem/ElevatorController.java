package com.demo.elevatorsystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class ElevatorController {
    // 通过 FXML 注入的文本标签，对应 hello-view.fxml 中 fx:id="welcomeText" 的 Label 控件
    @FXML
    private Label startText;

    /**
     * 按钮点击事件处理函数
     * 对应 hello-view.fxml 中按钮的 onAction="#onHelloButtonClick" 属性
     */
    @FXML
    protected void onHelloButtonClick() throws IOException {
        Stage stage = (Stage) startText.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("mainpage.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        stage.setScene(scene);
    }
}