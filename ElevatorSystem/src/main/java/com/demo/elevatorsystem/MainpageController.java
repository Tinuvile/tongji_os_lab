package com.demo.elevatorsystem;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainpageController {
    // 界面元素注入
    @FXML private VBox floorButtons;    // 楼层按钮容器（垂直排列）
    @FXML private HBox elevatorColumns; // 电梯列容器（水平排列5部电梯）
    @FXML private Text statusText;       // 底部状态显示文本

    @FXML
    public void initialize() {
        /* 初始化20层电梯外呼按钮
         * 每层包含上行/下行按钮和楼层号显示
         * 按20层到1层的倒序排列 */
        for (int i = 20; i >= 1; i--) {
            HBox floorBox = new HBox(5); // 每层水平容器（5像素间距）
            Button upBtn = new Button("↑");   // 上行呼叫按钮
            Button downBtn = new Button("↓"); // 下行呼叫按钮
            Text floorText = new Text("" + i);// 楼层数字显示

            // 设置按钮基础样式
            upBtn.setStyle("-fx-base: #c0c0c0;");
            downBtn.setStyle("-fx-base: #c0c0c0;");

            floorBox.getChildren().addAll(upBtn, downBtn, floorText);
            floorButtons.getChildren().add(floorBox);
        }

        // 初始化5部电梯操作面板
        for (int i = 1; i <= 5; i++) {
            VBox elevatorPanel = createElevatorPanel(i);
            elevatorColumns.getChildren().add(elevatorPanel);
        }
    }

    // 创建单个电梯操作面板
    private VBox createElevatorPanel(int elevatorId) {
        VBox elevator = new VBox(15); // 电梯主容器（15像素垂直间距）
        elevator.setStyle("-fx-border-color: #999; -fx-padding: 10;"); // 边框样式

        // 状态显示：电梯编号 + 当前楼层 + 方向 + 运行状态
        Text status = new Text("电梯#" + elevatorId + " 1层 ▲ 运行");

        // 功能按钮列（垂直排列）
        HBox controlPanel = new HBox(15);
        Button openBtn = new Button("开门");
        Button closeBtn = new Button("关门");
        Button alarmBtn = new Button("报警");
        controlPanel.getChildren().addAll(alarmBtn, openBtn, closeBtn);

        // 楼层按钮垂直排列（添加滚动条容器更佳）
        VBox floorBtnContainer = new VBox(5); // 垂直排列，间距5像素
        for (int i = 20; i >= 1; i--) {
            Button btn = new Button(i + "层");
            btn.setPrefWidth(80);  // 设置统一宽度
            floorBtnContainer.getChildren().add(btn);
        }

        // 组合电梯面板元素
        elevator.getChildren().addAll(status, controlPanel, floorBtnContainer);

        return elevator;
    }
}

















