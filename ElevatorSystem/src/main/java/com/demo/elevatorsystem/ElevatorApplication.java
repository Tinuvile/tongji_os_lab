package com.demo.elevatorsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ElevatorApplication extends Application {

    /**
     * JavaFX 应用程序入口点，初始化并显示主界面
     * @param stage 应用程序的主窗口容器
     */
    @Override
    public void start(Stage stage) throws IOException {
        // 加载 FXML 界面定义文件（资源路径：com/demo/elevatorsystem/hello-view.fxml）
        FXMLLoader fxmlLoader = new FXMLLoader(ElevatorApplication.class.getResource("hello-view.fxml"));

        // 创建场景图，设置初始窗口尺寸为 320x240 像素
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        // 配置窗口属性
        stage.setTitle("电梯调度系统入口");       // 设置窗口标题
        stage.setScene(scene);          // 将场景绑定到窗口
        stage.show();                   // 显示窗口
    }

    /**
     * 程序主入口，启动 JavaFX 应用框架
     * @param args 命令行参数（本程序未使用）
     */
    public static void main(String[] args) {
        launch();                        // 启动 JavaFX 应用程序生命周期管理
    }
}