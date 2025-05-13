package com.demo.elevatorsystem;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class Elevator extends Task<Void> {
    // 电梯基础属性
    private final int id;
    private final SimpleIntegerProperty currentFloor = new SimpleIntegerProperty(1);
    private final SimpleIntegerProperty targetFloor = new SimpleIntegerProperty(1);
    private final Set<Integer> internalRequests = new ConcurrentSkipListSet<>();

    // 电梯状态枚举
    public enum ElevatorState {
        IDLE, MOVING_UP, MOVING_DOWN, OPENING, CLOSING, ALARM
    }

    private final SimpleObjectProperty<ElevatorState> state = new SimpleObjectProperty<>(ElevatorState.IDLE);

    public Elevator(int id) {
        this.id = id;
    }

    // 内呼请求调用
    public void addInternalRequest(int floor) {
        if (floor >= 1 && floor <= 20) {
            internalRequests.add(floor);
            updateTargetFloor();
        }
    }

    // 更新目标楼层
    private void updateTargetFloor() {
        if (!internalRequests.isEmpty()) {
            state.set(ElevatorState.IDLE);
            return;
        }

        int nextTarget = findNearestTarget();
        targetFloor.set(nextTarget);
        state.set(nextTarget > currentFloor.get() ?
                ElevatorState.MOVING_UP : ElevatorState.MOVING_DOWN);
    }

    // 寻找最近的目标楼层
    private int findNearestTarget() {
        return internalRequests.stream()
                .min((a, b) -> Math.abs(a - currentFloor.get()) - Math.abs(b - currentFloor.get()))
                .orElse(currentFloor.get());
    }

    // 电梯运行逻辑
    @Override
    protected abstract Void call() throws Exception;

    public int getId() {
        return id;
    }

    public SimpleIntegerProperty currentFloorProperty() {
        return currentFloor;
    }

    public SimpleIntegerProperty targetFloorProperty() {
        return targetFloor;
    }


}