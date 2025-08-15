public class Floor {
    private int floorNumber; // 楼层号码
    private boolean upButtonPressed; // 上行按钮状态
    private boolean downButtonPressed; // 下行按钮状态
    private ElevatorSystem elevatorSystem; // 电梯系统引用
    
    public Floor(int floorNumber, ElevatorSystem elevatorSystem) {
        this.floorNumber = floorNumber;
        this.upButtonPressed = false;
        this.downButtonPressed = false;
        this.elevatorSystem = elevatorSystem;
    }
    
    // 按下上行按钮
    public void pressUpButton() {
        if (!upButtonPressed) {
            upButtonPressed = true;
            String upMsg = floorNumber + " 层按下上行按钮";
            System.out.println(upMsg);
            ElevatorGUI.logMessage(upMsg);
            
            // 即使所有电梯都报警，我们仍然更新了按钮状态
            // 通知电梯系统处理上行请求
            elevatorSystem.requestElevator(floorNumber, Direction.UP);
        }
    }
    
    // 按下下行按钮
    public void pressDownButton() {
        if (!downButtonPressed) {
            downButtonPressed = true;
            String downMsg = floorNumber + " 层按下下行按钮";
            System.out.println(downMsg);
            ElevatorGUI.logMessage(downMsg);
            
            // 即使所有电梯都报警，我们仍然更新了按钮状态
            // 通知电梯系统处理下行请求
            elevatorSystem.requestElevator(floorNumber, Direction.DOWN);
        }
    }
    
    // 电梯到达此楼层时调用，重置对应方向的按钮状态
    public void elevatorArrived(Direction direction) {
        if (direction == Direction.UP || direction == Direction.IDLE) {
            resetUpButton();
            String upArrivedMsg = floorNumber + " 层上行请求已处理";
            System.out.println(upArrivedMsg);
            ElevatorGUI.logMessage(upArrivedMsg);
        }
        
        if (direction == Direction.DOWN || direction == Direction.IDLE) {
            resetDownButton();
            String downArrivedMsg = floorNumber + " 层下行请求已处理";
            System.out.println(downArrivedMsg);
            ElevatorGUI.logMessage(downArrivedMsg);
        }
    }
    
    // 重置上行按钮状态
    public void resetUpButton() {
        upButtonPressed = false;
    }
    
    // 重置下行按钮状态
    public void resetDownButton() {
        downButtonPressed = false;
    }
    
    // Getter 方法
    public int getFloorNumber() {
        return floorNumber;
    }
    
    public boolean isUpButtonPressed() {
        return upButtonPressed;
    }
    
    public boolean isDownButtonPressed() {
        return downButtonPressed;
    }
} 