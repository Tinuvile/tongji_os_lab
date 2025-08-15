import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Elevator implements Runnable {
    private int id; // 电梯编号
    private int currentFloor; // 当前楼层
    private Direction direction; // 当前方向
    private ElevatorState state; // 电梯状态
    private List<Integer> requestedFloors; // 请求楼层列表
    private ElevatorSystem elevatorSystem; // 电梯系统引用
    private final Lock lock = new ReentrantLock(); // 锁，用于线程安全
    private Direction outsideCallDirection; // 外部呼叫的方向（用于电梯到达楼层后确定下一步方向）
    private boolean alarmed = false; // 电梯是否处于报警状态
    
    // 移动和门操作的模拟时间参数 - 减少以加快GUI响应
    private static final int FLOOR_MOVE_TIME = 500; // 每层移动时间(ms)，从1000减少到300
    private static final int DOOR_OPEN_TIME = 500; // 开门时间(ms)，从1000减少到500
    private static final int DOOR_WAIT_TIME = 1000; // 门保持开启时间(ms)，从2000减少到1000
    private static final int DOOR_CLOSE_TIME = 500; // 关门时间(ms)，从1000减少到500
    private static final int CHECK_INTERVAL = 100; // 检查请求间隔(ms)，从500减少到100
    
    public Elevator(int id, ElevatorSystem elevatorSystem) {
        this.id = id;
        this.currentFloor = 1; // 初始在第一层
        this.direction = Direction.IDLE;
        this.state = ElevatorState.STOPPED;
        this.requestedFloors = new ArrayList<>();
        this.elevatorSystem = elevatorSystem;
        this.outsideCallDirection = Direction.IDLE;
    }
    
    // 设置外部呼叫的方向
    public void setOutsideCallDirection(Direction direction) {
        this.outsideCallDirection = direction;
        System.out.println("电梯 " + id + " 接收到方向为 " + (direction == Direction.UP ? "上行" : "下行") + " 的外部呼叫");
    }
    
    // 按下楼层按钮
    public void pressFloorButton(int floor) {
        lock.lock();
        try {
            if (!requestedFloors.contains(floor) && floor != currentFloor) {
                requestedFloors.add(floor);
                System.out.println("电梯 " + id + " 接收到前往 " + floor + " 层的请求");
                
                // 判断方向
                if (direction == Direction.IDLE) {
                    if (floor > currentFloor) {
                        direction = Direction.UP;
                    } else {
                        direction = Direction.DOWN;
                    }
                }
                
                // 通知其他电梯
                elevatorSystem.syncRequest(this, floor);
            }
        } finally {
            lock.unlock();
        }
    }
    
    // 开门 - 修改为不阻塞UI线程，不自动关门，允许在报警状态下操作
    public void openDoor() {
        // 修改条件，即使在报警状态也允许开门，但不能在移动中开门
        if (state != ElevatorState.MOVING) {
            state = ElevatorState.DOOR_OPENING;
            String message = "电梯 " + id + " 在 " + currentFloor + " 层开门" + (alarmed ? "（报警状态）" : "");
            System.out.println(message);
            ElevatorGUI.logMessage(message);
            
            // 使用新线程执行开门操作，不阻塞UI
            new Thread(() -> {
                try {
                    Thread.sleep(DOOR_OPEN_TIME); // 模拟开门时间
                    state = ElevatorState.DOOR_OPENED;
                    String doorOpenedMsg = "电梯 " + id + " 在 " + currentFloor + " 层门已完全打开";
                    System.out.println(doorOpenedMsg);
                    ElevatorGUI.logMessage(doorOpenedMsg);
                    // 不再自动关门
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    // 关门 - 修改为不阻塞UI线程，允许在报警状态下操作
    public void closeDoor() {
        // 修改条件，即使在报警状态也允许关门
        if (state == ElevatorState.DOOR_OPENED) {
            state = ElevatorState.DOOR_CLOSING;
            String message = "电梯 " + id + " 在 " + currentFloor + " 层关门" + (alarmed ? "（报警状态）" : "");
            System.out.println(message);
            ElevatorGUI.logMessage(message);
            
            // 使用新线程执行关门操作，不阻塞UI
            new Thread(() -> {
                try {
                    Thread.sleep(DOOR_CLOSE_TIME); // 模拟关门时间
                    state = ElevatorState.STOPPED;
                    String doorClosedMsg = "电梯 " + id + " 在 " + currentFloor + " 层门已完全关闭";
                    System.out.println(doorClosedMsg);
                    ElevatorGUI.logMessage(doorClosedMsg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    // 移动到指定楼层
    private void moveToFloor(int targetFloor) {
        if (currentFloor == targetFloor) return;
        
        state = ElevatorState.MOVING;
        direction = (targetFloor > currentFloor) ? Direction.UP : Direction.DOWN;
        
        String message = "电梯 " + id + " 从 " + currentFloor + " 层" + 
                       (direction == Direction.UP ? "上行" : "下行") + "至 " + targetFloor + " 层";
        System.out.println(message);
        ElevatorGUI.logMessage(message);
        
        // 模拟电梯移动
        try {
            while (currentFloor != targetFloor) {
                Thread.sleep(FLOOR_MOVE_TIME); // 每层楼移动的时间 - 减少为300ms提高响应速度
                currentFloor += (direction == Direction.UP) ? 1 : -1;
                String posMsg = "电梯 " + id + " 当前位置：" + currentFloor + " 层";
                System.out.println(posMsg);
                ElevatorGUI.logMessage(posMsg);
            }
            state = ElevatorState.STOPPED;
            
            // 通知当前楼层电梯已到达
            elevatorSystem.getFloors().get(currentFloor - 1).elevatorArrived(direction);
            
            // 如果是响应外部呼叫，则设置下一步方向
            if (outsideCallDirection != Direction.IDLE) {
                direction = outsideCallDirection;
                String dirMsg = "电梯 " + id + " 响应外部呼叫，下一步方向设为: " + 
                              (direction == Direction.UP ? "上行" : "下行");
                System.out.println(dirMsg);
                ElevatorGUI.logMessage(dirMsg);
                outsideCallDirection = Direction.IDLE; // 重置
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    // 处理请求
    private void processRequests() {
        lock.lock();
        try {
            // 如果电梯处于报警状态，不处理任何请求
            if (alarmed) {
                return;
            }
            
            if (!requestedFloors.isEmpty()) {
                // 使用LOOK算法寻找下一个目标楼层
                int nextFloor = findNextFloorUsingLOOK();
                if (nextFloor != -1) {
                    moveToFloor(nextFloor);
                    requestedFloors.remove(Integer.valueOf(nextFloor));
                    openDoor();
                }
            } else {
                direction = Direction.IDLE; // 没有请求时设为空闲状态
            }
        } finally {
            lock.unlock();
        }
    }
    
    // 寻找下一个要去的楼层
    private int findNextFloorUsingLOOK() {
        if (requestedFloors.isEmpty()) return -1;
        
        // 如果是空闲状态，选择最近的楼层
        if (direction == Direction.IDLE) {
            int closest = requestedFloors.get(0);
            int minDistance = Math.abs(closest - currentFloor);
            
            for (int floor : requestedFloors) {
                int distance = Math.abs(floor - currentFloor);
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = floor;
                }
            }
            
            // 确定前进方向
            direction = (closest > currentFloor) ? Direction.UP : Direction.DOWN;
            return closest;
        }
        
        // 按当前方向寻找目标楼层
        int nextFloor = -1;
        int minDistance = Integer.MAX_VALUE;
        
        // 先检查当前方向上的请求
        for (int floor : requestedFloors) {
            if ((direction == Direction.UP && floor >= currentFloor) || 
                (direction == Direction.DOWN && floor <= currentFloor)) {
                int distance = Math.abs(floor - currentFloor);
                if (distance < minDistance) {
                    minDistance = distance;
                    nextFloor = floor;
                }
            }
        }
        
        // 如果当前方向没有更多请求，则改变方向
        if (nextFloor == -1) {
            direction = (direction == Direction.UP) ? Direction.DOWN : Direction.UP;
            String dirChangeMsg = "电梯 " + id + " 无更多 " + 
                             (direction == Direction.DOWN ? "上行" : "下行") + " 请求，改变方向为 " +
                             (direction == Direction.UP ? "上行" : "下行");
            System.out.println(dirChangeMsg);
            ElevatorGUI.logMessage(dirChangeMsg);
            
            // 在新方向上寻找最近的请求
            return findNextFloorUsingLOOK();
        }
        
        return nextFloor;
    }
    
    // 报警功能 - 触发电梯报警
    public void triggerAlarm() {
        lock.lock();
        try {
            alarmed = true;
            // 如果电梯正在移动，立即停止
            if (state == ElevatorState.MOVING) {
                state = ElevatorState.STOPPED;
            }
            String alarmMsg = "电梯 " + id + " 触发报警！电梯已停止运行";
            System.out.println(alarmMsg);
            ElevatorGUI.logMessage(alarmMsg);
        } finally {
            lock.unlock();
        }
    }
    
    // 重置报警 - 让电梯恢复正常工作
    public void resetAlarm() {
        lock.lock();
        try {
            alarmed = false;
            String resetMsg = "电梯 " + id + " 报警已重置，电梯恢复运行";
            System.out.println(resetMsg);
            ElevatorGUI.logMessage(resetMsg);
        } finally {
            lock.unlock();
        }
    }
    
    // 线程运行方法
    @Override
    public void run() {
        while (true) {
            // 只有在非报警状态下才处理请求
            if (!alarmed) {
                processRequests();
            }
            
            try {
                Thread.sleep(CHECK_INTERVAL); // 定期检查请求 - 减少到100ms提高响应速度
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
    
    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }
    
    public int getCurrentFloor() {
        return currentFloor;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public ElevatorState getState() {
        return state;
    }
    
    public List<Integer> getRequestedFloors() {
        return new ArrayList<>(requestedFloors);
    }
    
    public Direction getOutsideCallDirection() {
        return outsideCallDirection;
    }
    
    public boolean isAlarmed() {
        return alarmed;
    }
}

// 电梯方向枚举
enum Direction {
    UP, DOWN, IDLE
}

// 电梯状态枚举
enum ElevatorState {
    MOVING, STOPPED, DOOR_OPENING, DOOR_OPENED, DOOR_CLOSING
} 