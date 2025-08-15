import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ElevatorGUI extends JFrame {
    private ElevatorSystem elevatorSystem;
    private List<JPanel> elevatorPanels;
    private List<JLabel> elevatorStatusLabels;
    private List<JButton[]> floorButtons; // 电梯内部按钮
    private JButton[][] externalButtons; // 外部呼叫按钮 [楼层][0-上行/1-下行]
    private JPanel[][] floorButtonPanels;
    private List<ElevatorVisualizer> elevatorVisualizers; // 电梯可视化组件
    private JPanel mainPanel; // 主面板引用
    private JTextArea logTextArea; // 日志文本区域
    
    // 单例模式，用于日志系统
    private static ElevatorGUI instance;
    
    // 定义支持中文的字体
    private final Font chineseFont = new Font("宋体", Font.PLAIN, 12);
    private final Font chineseBoldFont = new Font("宋体", Font.BOLD, 12);
    private final Font buttonFont = new Font("宋体", Font.BOLD, 16);
    
    // UI更新频率
    private static final int UI_UPDATE_INTERVAL = 100; // 毫秒
    
    public ElevatorGUI(ElevatorSystem elevatorSystem) {
        this.elevatorSystem = elevatorSystem;
        this.elevatorPanels = new ArrayList<>();
        this.elevatorStatusLabels = new ArrayList<>();
        this.floorButtons = new ArrayList<>();
        this.externalButtons = new JButton[ElevatorSystem.getTotalFloors()][2]; // [楼层][0-上行/1-下行]
        this.floorButtonPanels = new JPanel[ElevatorSystem.getTotalFloors()][2]; // 每层有上行和下行按钮
        this.elevatorVisualizers = new ArrayList<>();
        
        // 设置实例，用于日志系统
        instance = this;
        
        initializeGUI();
        
        // 初始化完成后输出电梯状态信息
        SwingUtilities.invokeLater(() -> {
            logMessage("===== 电梯调度系统 GUI 已启动 =====");
            logMessage("系统共有 " + elevatorSystem.getElevators().size() + " 部电梯，" 
                     + ElevatorSystem.getTotalFloors() + " 层楼");
            logMessage("所有电梯初始位置为 1 层，状态为空闲");
            logMessage("==============================");
        });
    }
    
    private void initializeGUI() {
        setTitle("电梯调度系统 - LOOK算法");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);  // 增加窗口宽度
        setLayout(new BorderLayout());
        
        // 创建主面板
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        // 创建顶部信息面板
        JPanel infoPanel = new JPanel();
        JLabel infoLabel = new JLabel("本系统采用LOOK电梯调度算法，五部电梯协同工作");
        infoLabel.setFont(chineseFont);
        infoPanel.add(infoLabel);
        
        // 添加状态显示标签
        JLabel systemStatusLabel = new JLabel("系统状态: 正常运行中");
        systemStatusLabel.setFont(chineseBoldFont);
        systemStatusLabel.setForeground(Color.GREEN);
        infoPanel.add(systemStatusLabel);
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        
        // 创建左侧电梯外部呼叫按钮区域
        JPanel externalButtonsPanel = createExternalButtonsPanel();
        mainPanel.add(externalButtonsPanel, BorderLayout.WEST);
        
        // 创建电梯显示区域
        JPanel elevatorsPanel = createElevatorsPanel();
        mainPanel.add(elevatorsPanel, BorderLayout.CENTER);
        
        // 创建右侧面板，包含操作说明和日志
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        
        // 添加操作说明面板
        JPanel instructionPanel = createInstructionPanel();
        rightPanel.add(instructionPanel, BorderLayout.NORTH);
        
        // 添加日志面板
        JPanel logPanel = createLogPanel();
        rightPanel.add(logPanel, BorderLayout.CENTER);
        
        mainPanel.add(rightPanel, BorderLayout.EAST);
        
        // 添加主面板到窗口
        add(mainPanel);
        
        // 创建更新UI的线程
        startUIUpdateThread();
        
        // 显示窗口
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private JPanel createInstructionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("操作说明"));
        
        JTextArea instructionText = new JTextArea(
            "电梯系统使用说明：\n\n" +
            "1. 左侧为各楼层的外部呼叫按钮\n" +
            "   - 上箭头(↑)：呼叫上行电梯\n" +
            "   - 下箭头(↓)：呼叫下行电梯\n" +
            "   - 即使部分电梯报警，其他电梯仍会响应\n\n" +
            "2. 中间为五部电梯的运行状态\n" +
            "   - 蓝色方块表示正常电梯\n" +
            "   - 红色方块表示报警状态电梯\n" +
            "   - 白色箭头表示电梯运行方向\n\n" +
            "3. 每部电梯右侧为内部楼层按钮\n" +
            "   - 代表进入电梯后选择目标楼层\n\n" +
            "4. 底部为控制按钮\n" +
            "   - 开门：手动打开电梯门（报警状态仍可用）\n" +
            "   - 关门：手动关闭电梯门（报警状态仍可用）\n" +
            "   - 报警：触发电梯紧急停止\n" +
            "     (报警后电梯将停止运行并变红)\n" +
            "   - 再次点击报警按钮可重置电梯\n\n" +
            "5. 电梯调度采用LOOK算法：\n" +
            "   - 电梯沿一个方向运行直到该方向\n" +
            "     没有更多请求\n" +
            "   - 然后改变方向处理另一方向请求\n" +
            "   - 五部电梯协作，选择最优方案\n"
        );
        instructionText.setEditable(false);
        instructionText.setBackground(new Color(240, 240, 240));
        instructionText.setFont(chineseFont);
        instructionText.setRows(10);  // 设置固定行数，减小高度
        
        panel.add(new JScrollPane(instructionText), BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createExternalButtonsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(ElevatorSystem.getTotalFloors(), 1));
        panel.setBorder(BorderFactory.createTitledBorder("楼层外部呼叫按钮"));
        
        for (int floor = ElevatorSystem.getTotalFloors(); floor >= 1; floor--) {
            final int f = floor;
            JPanel floorPanel = new JPanel();
            floorPanel.setLayout(new BorderLayout());
            floorPanel.setBorder(BorderFactory.createTitledBorder(floor + " 层"));
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(1, 2, 5, 0));
            
            // 创建上行按钮
            JButton upButton = new JButton("↑");
            upButton.setFont(buttonFont);
            upButton.setForeground(Color.RED);
            upButton.setBackground(null); // 初始状态无色
            upButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 按下上行按钮
                    elevatorSystem.getFloors().get(f - 1).pressUpButton();
                    upButton.setBackground(Color.GREEN); // 高亮显示已按下
                }
            });
            
            // 创建下行按钮
            JButton downButton = new JButton("↓");
            downButton.setFont(buttonFont);
            downButton.setForeground(Color.BLUE);
            downButton.setBackground(null); // 初始状态无色
            downButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 按下下行按钮
                    elevatorSystem.getFloors().get(f - 1).pressDownButton();
                    downButton.setBackground(Color.GREEN); // 高亮显示已按下
                }
            });
            
            // 禁用顶层的上行按钮和底层的下行按钮
            if (floor == ElevatorSystem.getTotalFloors()) {
                upButton.setEnabled(false);
            }
            
            if (floor == 1) {
                downButton.setEnabled(false);
            }
            
            buttonPanel.add(upButton);
            buttonPanel.add(downButton);
            floorPanel.add(buttonPanel, BorderLayout.CENTER);
            
            // 保存按钮引用
            externalButtons[floor - 1][0] = upButton;
            externalButtons[floor - 1][1] = downButton;
            
            panel.add(floorPanel);
        }
        
        return panel;
    }
    
    private JPanel createElevatorsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, elevatorSystem.getElevators().size(), 10, 0));
        panel.setBorder(BorderFactory.createTitledBorder("电梯状态"));
        
        for (int i = 0; i < elevatorSystem.getElevators().size(); i++) {
            JPanel elevatorPanel = new JPanel();
            elevatorPanel.setLayout(new BorderLayout());
            elevatorPanel.setBorder(BorderFactory.createTitledBorder("电梯 " + (i + 1)));
            
            // 电梯当前状态显示
            JLabel statusLabel = new JLabel("当前楼层: 1  状态: 空闲", JLabel.CENTER);
            statusLabel.setFont(chineseFont);
            elevatorPanel.add(statusLabel, BorderLayout.NORTH);
            elevatorStatusLabels.add(statusLabel);
            
            // 创建电梯可视化组件
            ElevatorVisualizer visualizer = new ElevatorVisualizer(ElevatorSystem.getTotalFloors());
            elevatorPanel.add(visualizer, BorderLayout.CENTER);
            elevatorVisualizers.add(visualizer);
            
            // 电梯内部楼层按钮
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(ElevatorSystem.getTotalFloors(), 1));
            JButton[] buttons = new JButton[ElevatorSystem.getTotalFloors()];
            
            for (int floor = ElevatorSystem.getTotalFloors(); floor >= 1; floor--) {
                final int f = floor;
                final int elevatorId = i;
                
                JButton floorButton = new JButton(String.valueOf(floor));
                floorButton.setFont(chineseFont);
                floorButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // 按下电梯内的楼层按钮
                        elevatorSystem.getElevators().get(elevatorId).pressFloorButton(f);
                        floorButton.setBackground(Color.YELLOW); // 高亮显示按下的按钮
                    }
                });
                
                buttonPanel.add(floorButton);
                buttons[floor - 1] = floorButton;
            }
            
            JScrollPane scrollPane = new JScrollPane(buttonPanel);
            scrollPane.setPreferredSize(new Dimension(60, 500));
            elevatorPanel.add(scrollPane, BorderLayout.EAST);
            
            // 电梯控制按钮（开门、关门等）
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new GridLayout(1, 3));  // 改为3列，增加报警按钮
            
            final int elevatorIndex = i;
            JButton openButton = new JButton("开门");
            openButton.setFont(chineseFont);
            openButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 开门功能实现
                    elevatorSystem.getElevators().get(elevatorIndex).openDoor();
                }
            });
            
            JButton closeButton = new JButton("关门");
            closeButton.setFont(chineseFont);
            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 关门功能实现
                    elevatorSystem.getElevators().get(elevatorIndex).closeDoor();
                }
            });
            
            // 新增报警按钮
            JButton alarmButton = new JButton("报警");
            alarmButton.setFont(chineseFont);
            alarmButton.setBackground(Color.RED);
            alarmButton.setForeground(Color.WHITE);
            alarmButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 报警功能实现
                    Elevator elevator = elevatorSystem.getElevators().get(elevatorIndex);
                    if (!elevator.isAlarmed()) {
                        elevator.triggerAlarm();
                        alarmButton.setText("重置报警");
                    } else {
                        elevator.resetAlarm();
                        alarmButton.setText("报警");
                    }
                }
            });
            
            controlPanel.add(openButton);
            controlPanel.add(closeButton);
            controlPanel.add(alarmButton);
            
            elevatorPanel.add(controlPanel, BorderLayout.SOUTH);
            
            panel.add(elevatorPanel);
            elevatorPanels.add(elevatorPanel);
            floorButtons.add(buttons);
        }
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("运行日志"));
        
        // 创建日志文本区域
        logTextArea = new JTextArea(20, 30);  // 增加行数
        logTextArea.setEditable(false);
        logTextArea.setFont(chineseFont);
        
        // 添加欢迎信息
        logTextArea.setText("欢迎使用电梯调度系统\n使用LOOK算法进行电梯调度\n操作日志将显示在此区域\n\n");
        
        // 设置自动滚动
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // 安装日志系统
        installLogSystem();
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 添加清除按钮
        JButton clearButton = new JButton("清除日志");
        clearButton.setFont(chineseFont);
        clearButton.addActionListener(e -> logTextArea.setText("欢迎使用电梯调度系统\n"));
        panel.add(clearButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // 安装日志系统
    private void installLogSystem() {
        // 移除过去的重定向代码，使用自定义日志方法替代
    }
    
    // 添加日志消息到日志区域，解决中文乱码问题
    public static void logMessage(String message) {
        if (instance != null && instance.logTextArea != null) {
            // 在EDT线程中更新UI
            SwingUtilities.invokeLater(() -> {
                instance.logTextArea.append(message + "\n");
                // 确保滚动到最新内容
                instance.logTextArea.setCaretPosition(instance.logTextArea.getDocument().getLength());
                
                // 同时输出到控制台
                System.out.println(message);
            });
        }
    }
    
    // 使用Swing Timer来更新UI，避免线程同步问题
    private void startUIUpdateThread() {
        // 使用Swing Timer代替普通线程，减少线程同步问题
        Timer updateTimer = new Timer(UI_UPDATE_INTERVAL, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateElevatorStatus();
            }
        });
        updateTimer.setCoalesce(true); // 合并多余事件，提高性能
        updateTimer.start();
    }
    
    private void updateElevatorStatus() {
        // 这个方法已经在EDT线程中，不需要使用invokeLater
        
        // 更新系统状态
        boolean allAlarmed = elevatorSystem.areAllElevatorsAlarmed();
        JLabel systemStatusLabel = null;
        
        // 寻找系统状态标签
        for (Component comp : ((JPanel)mainPanel.getComponent(0)).getComponents()) {
            if (comp instanceof JLabel && ((JLabel)comp).getText().startsWith("系统状态")) {
                systemStatusLabel = (JLabel)comp;
                break;
            }
        }
        
        if (systemStatusLabel != null) {
            if (allAlarmed) {
                systemStatusLabel.setText("系统状态: 所有电梯报警中，无法响应新请求");
                systemStatusLabel.setForeground(Color.RED);
            } else {
                systemStatusLabel.setText("系统状态: 正常运行中");
                systemStatusLabel.setForeground(Color.GREEN);
            }
        }
        
        // 更新电梯状态
        for (int i = 0; i < elevatorSystem.getElevators().size(); i++) {
            Elevator elevator = elevatorSystem.getElevators().get(i);
            
            // 更新电梯状态标签，添加报警状态显示
            String statusText = String.format("当前楼层: %d  状态: %s  方向: %s%s", 
                elevator.getCurrentFloor(), 
                getStateText(elevator.getState()),
                getDirectionText(elevator.getDirection()),
                elevator.isAlarmed() ? "  【报警中】" : "");
            
            elevatorStatusLabels.get(i).setText(statusText);
            
            // 更新报警按钮文本
            JPanel controlPanel = (JPanel) elevatorPanels.get(i).getComponent(3); // 控制面板在索引3的位置
            JButton alarmButton = (JButton) controlPanel.getComponent(2); // 报警按钮在索引2的位置
            if (elevator.isAlarmed()) {
                alarmButton.setText("重置报警");
            } else {
                alarmButton.setText("报警");
            }
            
            // 更新电梯可视化组件
            elevatorVisualizers.get(i).updateElevatorPosition(
                elevator.getCurrentFloor(), 
                elevator.getState(), 
                elevator.getDirection(),
                elevator.isAlarmed()
            );
            
            // 更新电梯内按钮状态
            List<Integer> requestedFloors = elevator.getRequestedFloors();
            for (int floor = 0; floor < ElevatorSystem.getTotalFloors(); floor++) {
                JButton button = floorButtons.get(i)[floor];
                if (requestedFloors.contains(floor + 1)) {
                    button.setBackground(Color.YELLOW);
                } else {
                    button.setBackground(null);
                }
            }
        }
        
        // 更新外部呼叫按钮状态
        for (int floor = 0; floor < ElevatorSystem.getTotalFloors(); floor++) {
            Floor floorObj = elevatorSystem.getFloors().get(floor);
            
            // 更新上行按钮状态
            JButton upButton = externalButtons[floor][0];
            if (upButton != null) {
                if (floorObj.isUpButtonPressed()) {
                    upButton.setBackground(Color.GREEN);
                } else {
                    upButton.setBackground(null);
                }
            }
            
            // 更新下行按钮状态
            JButton downButton = externalButtons[floor][1];
            if (downButton != null) {
                if (floorObj.isDownButtonPressed()) {
                    downButton.setBackground(Color.GREEN);
                } else {
                    downButton.setBackground(null);
                }
            }
        }
    }
    
    private String getStateText(ElevatorState state) {
        switch (state) {
            case MOVING: return "移动中";
            case STOPPED: return "停止";
            case DOOR_OPENING: return "开门中";
            case DOOR_OPENED: return "门已打开";
            case DOOR_CLOSING: return "关门中";
            default: return "未知";
        }
    }
    
    private String getDirectionText(Direction direction) {
        switch (direction) {
            case UP: return "上行";
            case DOWN: return "下行";
            case IDLE: return "空闲";
            default: return "未知";
        }
    }
    
    // 电梯可视化组件 - 内部类
    class ElevatorVisualizer extends JPanel {
        private int totalFloors;
        private int currentFloor = 1;
        private ElevatorState state = ElevatorState.STOPPED;
        private Direction direction = Direction.IDLE;
        private boolean alarmed = false;
        
        public ElevatorVisualizer(int totalFloors) {
            this.totalFloors = totalFloors;
            setPreferredSize(new Dimension(80, 500));
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }
        
        public void updateElevatorPosition(int floor, ElevatorState state, Direction direction, boolean alarmed) {
            this.currentFloor = floor;
            this.state = state;
            this.direction = direction;
            this.alarmed = alarmed;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            
            // 绘制电梯井道
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(10, 10, width - 20, height - 20);
            
            // 计算每层高度
            int floorHeight = (height - 20) / totalFloors;
            
            // 绘制楼层分隔线
            g2d.setColor(Color.GRAY);
            for (int i = 1; i < totalFloors; i++) {
                int y = height - 10 - i * floorHeight;
                g2d.drawLine(10, y, width - 10, y);
            }
            
            // 计算电梯位置
            int elevatorTop = height - 10 - currentFloor * floorHeight;
            int elevatorHeight = floorHeight;
            
            // 绘制电梯 - 根据alarmed状态改变颜色
            Color elevatorColor = alarmed ? Color.RED : Color.BLUE;
            
            if (state == ElevatorState.DOOR_OPENED) {
                // 门打开状态 - 绘制为分开的两扇门
                g2d.setColor(elevatorColor);
                g2d.fillRect(15, elevatorTop, (width - 30) / 2 - 5, elevatorHeight);
                g2d.fillRect(width / 2 + 5, elevatorTop, (width - 30) / 2 - 5, elevatorHeight);
            } else if (state == ElevatorState.DOOR_OPENING || state == ElevatorState.DOOR_CLOSING) {
                // 门正在开关状态 - 绘制为部分打开的门
                g2d.setColor(elevatorColor);
                int doorGap = (width - 30) / 4;
                g2d.fillRect(15, elevatorTop, (width - 30) / 2 - doorGap, elevatorHeight);
                g2d.fillRect(width / 2 + doorGap, elevatorTop, (width - 30) / 2 - doorGap, elevatorHeight);
            } else {
                // 关门状态 - 绘制为完整的电梯
                g2d.setColor(elevatorColor);
                g2d.fillRect(15, elevatorTop, width - 30, elevatorHeight);
            }
            
            // 如果处于报警状态，绘制报警标志
            if (alarmed) {
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("宋体", Font.BOLD, 16));
                g2d.drawString("!", width / 2 - 5, elevatorTop + floorHeight / 2 + 5);
            } else {
                // 绘制方向指示器
                g2d.setColor(Color.WHITE);
                int arrowX = width / 2;
                int arrowY = elevatorTop + floorHeight / 2;
                int arrowSize = Math.min(floorHeight, width) / 4;
                
                if (direction == Direction.UP) {
                    // 上行箭头
                    int[] xPoints = {arrowX, arrowX + arrowSize, arrowX - arrowSize};
                    int[] yPoints = {arrowY - arrowSize, arrowY + arrowSize, arrowY + arrowSize};
                    g2d.fillPolygon(xPoints, yPoints, 3);
                } else if (direction == Direction.DOWN) {
                    // 下行箭头
                    int[] xPoints = {arrowX, arrowX + arrowSize, arrowX - arrowSize};
                    int[] yPoints = {arrowY + arrowSize, arrowY - arrowSize, arrowY - arrowSize};
                    g2d.fillPolygon(xPoints, yPoints, 3);
                }
            }
            
            // 绘制楼层数字
            g2d.setColor(Color.BLACK);
            g2d.setFont(chineseFont);
            for (int i = 1; i <= totalFloors; i++) {
                float yPos = (i - 0.5f) * floorHeight;
                int y = height - 5 - (int)yPos;
                g2d.drawString(String.valueOf(i), 2, y);
            }
        }
    }

    // 添加一个显示系统消息的方法
    private void showSystemMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "系统消息", JOptionPane.INFORMATION_MESSAGE);
    }
} 