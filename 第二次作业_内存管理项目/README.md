# 内存管理模拟系统

## 项目概述

本项目实现了两种内存管理方式的模拟：

1. 动态分区分配方式模拟（首次适应算法和最佳适应算法）
2. 请求调页存储管理方式模拟（FIFO 和 LRU 算法）

## 功能特性

- GUI 界面，直观展示内存分配状态
- 动态分区分配：支持首次适应(First Fit)和最佳适应(Best Fit)算法
- 请求调页：支持 FIFO 和 LRU 页面置换算法
- 实时显示空闲分区链状态
- 计算并显示缺页率

## 文件结构

```text
src/
├── MemorySimulator.java      # 主程序入口
├── MainPanel.java            # 主界面面板
├── MemoryBlock.java          # 内存块类
├── PartitionManager.java     # 分区管理器
├── PartitionPanel.java       # 动态分区分配界面
├── Page.java                 # 页面类
├── PageReplacementManager.java # 页面置换管理器
└── PageReplacementPanel.java  # 请求调页界面
```

## 运行环境

- Java 8 或更高版本
- 支持 Swing GUI

## 编译和运行

```bash
javac -d bin src/*.java
java -cp bin MemorySimulator
```

也可直接运行`jar`文件

```bash
java -jar memorySimulator.jar
```

## 设计方案

### 动态分区分配

- 初始内存：640K
- 首次适应算法：从头开始找第一个足够大的空闲分区
- 最佳适应算法：找最小的足够大的空闲分区

### 请求调页存储管理

- 页面大小：10 条指令
- 内存块数：4 个
- 地址空间：32 页（320 条指令）
- 指令访问模式：50%顺序，25%前地址均匀分布，25%后地址均匀分布
