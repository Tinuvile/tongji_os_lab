//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("电梯调度系统启动中...");
        
        // 创建并启动电梯系统
        ElevatorSystem elevatorSystem = new ElevatorSystem();
        elevatorSystem.start();
        
        // 启动GUI界面
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ElevatorGUI(elevatorSystem);
            }
        });
        
        // 简单的命令行界面，用于测试
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        System.out.println("\n命令说明：");
        System.out.println("1. 请求电梯: request <楼层> <方向(up/down)>");
        System.out.println("2. 按下电梯内的楼层按钮: press <电梯编号> <目标楼层>");
        System.out.println("3. 退出: exit");
        
        while (running) {
            System.out.print("\n输入命令: ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("exit")) {
                running = false;
            } else {
                processCommand(input, elevatorSystem);
            }
        }
        
        // 关闭电梯系统
        elevatorSystem.shutdown();
        scanner.close();
        System.out.println("电梯系统已关闭");
        System.exit(0);
    }
    
    // 处理用户输入的命令
    private static void processCommand(String command, ElevatorSystem elevatorSystem) {
        String[] parts = command.split("\\s+");
        
        try {
            switch (parts[0].toLowerCase()) {
                case "request":
                    // 从楼层请求电梯
                    if (parts.length >= 3) {
                        int floor = Integer.parseInt(parts[1]);
                        String direction = parts[2].toLowerCase();
                        
                        if (floor < 1 || floor > ElevatorSystem.getTotalFloors()) {
                            System.out.println("无效的楼层，有效范围: 1-" + ElevatorSystem.getTotalFloors());
                            return;
                        }
                        
                        if (direction.equals("up")) {
                            elevatorSystem.getFloors().get(floor - 1).pressUpButton();
                        } else if (direction.equals("down")) {
                            elevatorSystem.getFloors().get(floor - 1).pressDownButton();
                        } else {
                            System.out.println("无效的方向，请使用 up 或 down");
                        }
                    } else {
                        System.out.println("语法错误: request <楼层> <方向(up/down)>");
                    }
                    break;
                    
                case "press":
                    // 按下电梯内的楼层按钮
                    if (parts.length >= 3) {
                        int elevatorId = Integer.parseInt(parts[1]);
                        int targetFloor = Integer.parseInt(parts[2]);
                        
                        if (elevatorId < 1 || elevatorId > elevatorSystem.getElevators().size()) {
                            System.out.println("无效的电梯编号，有效范围: 1-" + elevatorSystem.getElevators().size());
                            return;
                        }
                        
                        if (targetFloor < 1 || targetFloor > ElevatorSystem.getTotalFloors()) {
                            System.out.println("无效的楼层，有效范围: 1-" + ElevatorSystem.getTotalFloors());
                            return;
                        }
                        
                        elevatorSystem.getElevators().get(elevatorId - 1).pressFloorButton(targetFloor);
                    } else {
                        System.out.println("语法错误: press <电梯编号> <目标楼层>");
                    }
                    break;
                    
                default:
                    System.out.println("未知命令，请使用以下命令之一:");
                    System.out.println("1. 请求电梯: request <楼层> <方向(up/down)>");
                    System.out.println("2. 按下电梯内的楼层按钮: press <电梯编号> <目标楼层>");
                    System.out.println("3. 退出: exit");
            }
        } catch (NumberFormatException e) {
            System.out.println("请输入有效的数字");
        } catch (Exception e) {
            System.out.println("发生错误: " + e.getMessage());
        }
    }
}