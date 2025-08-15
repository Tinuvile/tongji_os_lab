import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ElevatorSystem {
    private static final int TOTAL_FLOORS = 20; // 总楼层数
    private static final int TOTAL_ELEVATORS = 5; // 电梯数量
    
    private List<Elevator> elevators; // 电梯列表
    private List<Floor> floors; // 楼层列表
    private ExecutorService executorService; // 线程池
    
    public ElevatorSystem() {
        elevators = new ArrayList<>();
        floors = new ArrayList<>();
        executorService = Executors.newFixedThreadPool(TOTAL_ELEVATORS);
        
        // 初始化楼层
        for (int i = 1; i <= TOTAL_FLOORS; i++) {
            floors.add(new Floor(i, this));
        }
        
        // 初始化电梯
        for (int i = 1; i <= TOTAL_ELEVATORS; i++) {
            elevators.add(new Elevator(i, this));
        }
    }
    
    // 启动电梯系统
    public void start() {
        String startMsg = "电梯系统启动，共 " + TOTAL_ELEVATORS + " 部电梯，" + TOTAL_FLOORS + " 层楼";
        System.out.println(startMsg);
        ElevatorGUI.logMessage(startMsg);
        
        // 启动所有电梯线程
        for (Elevator elevator : elevators) {
            executorService.submit(elevator);
        }
    }
    
    // 关闭电梯系统
    public void shutdown() {
        String shutdownMsg = "电梯系统关闭";
        System.out.println(shutdownMsg);
        ElevatorGUI.logMessage(shutdownMsg);
        executorService.shutdown();
    }
    
    // 从楼层按下上行或下行按钮
    public void requestElevator(int floorNumber, Direction direction) {
        String requestMsg = floorNumber + " 层请求 " + (direction == Direction.UP ? "上行" : "下行") + " 电梯";
        System.out.println(requestMsg);
        ElevatorGUI.logMessage(requestMsg);
        
        // 选择最合适的电梯处理请求
        Elevator bestElevator = findBestElevatorUsingLOOK(floorNumber, direction);
        if (bestElevator != null) {
            bestElevator.pressFloorButton(floorNumber);
            // 电梯到达后将根据呼叫方向调整自己的下一步行进方向
            bestElevator.setOutsideCallDirection(direction);
        } else {
            // 如果没有找到合适的电梯（所有电梯都处于报警状态），可以在这里添加额外处理
            String noElevatorMsg = "无法分配电梯响应请求，请等待电梯恢复正常";
            System.out.println(noElevatorMsg);
            ElevatorGUI.logMessage(noElevatorMsg);
        }
    }
    
    // 同步请求到其他电梯（实现电梯按钮互联功能）
    public void syncRequest(Elevator sourceElevator, int targetFloor) {
        String syncMsg = "同步显示：电梯 " + sourceElevator.getId() + " 前往 " + targetFloor + " 层的请求灯点亮";
        System.out.println(syncMsg);
        ElevatorGUI.logMessage(syncMsg);
        // 此处仅同步显示，不实际发送请求到其他电梯
    }
    
    // 使用LOOK算法寻找最合适的电梯处理请求
    private Elevator findBestElevatorUsingLOOK(int requestedFloor, Direction requestedDirection) {
        // 候选电梯及其评分
        Elevator selectedElevator = null;
        int lowestScore = Integer.MAX_VALUE;
        
        for (Elevator elevator : elevators) {
            // 跳过处于报警状态的电梯
            if (elevator.isAlarmed()) {
                continue;
            }
            
            int score = calculateLOOKScore(elevator, requestedFloor, requestedDirection);
            
            if (score < lowestScore) {
                lowestScore = score;
                selectedElevator = elevator;
            }
        }
        
        // 如果所有电梯都处于报警状态，返回null
        if (selectedElevator == null) {
            String alarmedMsg = "所有电梯都处于报警状态，无法响应 " + requestedFloor + " 层的请求";
            System.out.println(alarmedMsg);
            ElevatorGUI.logMessage(alarmedMsg);
            return null;
        }
        
        String selectedMsg = "LOOK算法选择电梯 " + selectedElevator.getId() + " 响应 " + requestedFloor + " 层的" + 
                          (requestedDirection == Direction.UP ? "上行" : "下行") + "请求";
        System.out.println(selectedMsg);
        ElevatorGUI.logMessage(selectedMsg);
        
        return selectedElevator;
    }
    
    // 计算LOOK算法下电梯的评分（分数越低越优先）
    private int calculateLOOKScore(Elevator elevator, int requestedFloor, Direction requestedDirection) {
        int currentFloor = elevator.getCurrentFloor();
        Direction elevatorDirection = elevator.getDirection();
        int score = 0;
        
        // 基础分数：距离
        score = Math.abs(currentFloor - requestedFloor) * 2;
        
        // 电梯静止时，直接使用距离作为分数
        if (elevatorDirection == Direction.IDLE) {
            return score;
        }
        
        // 根据LOOK算法，优先考虑电梯行进方向上的请求
        if (elevatorDirection == Direction.UP) {
            if (requestedFloor >= currentFloor) {
                // 电梯向上且请求在当前楼层或上方
                if (requestedDirection == Direction.UP) {
                    // 请求也是向上，非常匹配
                    score -= 10;
                } else {
                    // 请求向下，稍微不匹配但仍可接受
                    score -= 5;
                }
            } else {
                // 电梯向上但请求在下方，需要改变方向
                score += 20;
                if (requestedDirection == Direction.DOWN) {
                    // 如果请求也是向下，至少方向会匹配
                    score -= 2;
                }
            }
        } else if (elevatorDirection == Direction.DOWN) {
            if (requestedFloor <= currentFloor) {
                // 电梯向下且请求在当前楼层或下方
                if (requestedDirection == Direction.DOWN) {
                    // 请求也是向下，非常匹配
                    score -= 10;
                } else {
                    // 请求向上，稍微不匹配但仍可接受
                    score -= 5;
                }
            } else {
                // 电梯向下但请求在上方，需要改变方向
                score += 20;
                if (requestedDirection == Direction.UP) {
                    // 如果请求也是向上，至少方向会匹配
                    score -= 2;
                }
            }
        }
        
        // 考虑电梯当前状态
        switch (elevator.getState()) {
            case MOVING:
                // 移动中的电梯有一定惯性，稍微增加分数
                score += 2;
                break;
            case STOPPED:
                // 停止的电梯可以立即响应，稍微降低分数
                score -= 2;
                break;
            case DOOR_OPENING:
            case DOOR_OPENED:
            case DOOR_CLOSING:
                // 正在处理乘客的电梯，增加分数
                score += 5;
                break;
        }
        
        // 考虑电梯已有请求数量，请求越多分数越高
        score += elevator.getRequestedFloors().size() * 3;
        
        return score;
    }
    
    // Getter 方法
    public List<Elevator> getElevators() {
        return elevators;
    }
    
    public List<Floor> getFloors() {
        return floors;
    }
    
    public static int getTotalFloors() {
        return TOTAL_FLOORS;
    }
    
    // 检查是否所有电梯都处于报警状态
    public boolean areAllElevatorsAlarmed() {
        for (Elevator elevator : elevators) {
            if (!elevator.isAlarmed()) {
                return false;
            }
        }
        return true;
    }
} 