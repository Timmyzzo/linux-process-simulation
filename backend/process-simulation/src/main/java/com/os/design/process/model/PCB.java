package com.os.design.process.model;

import lombok.Data;

/**
 * 进程控制块 (Process Control Block)
 * 模拟 Linux 内核中的 struct task_struct
 */
@Data
public class PCB {
    // === 基础信息 ===
    private Integer pid;            // 进程标识符 (PID)
    private String name;            // 进程名称
    private String color;           // 前端展示颜色 (可视化加分项)

    // === 调度信息 ===
    private int arrivalTime;        // 到达时间 (相对于开始模拟的时间点)
    private int serviceTime;        // 服务时间 (需要运行的总时长)
    private int priority;           // 优先级 (数字越大，优先级越高，模拟 nice 值)

    // === 运行状态 ===
    private int remainingTime;      // 剩余运行时间
    private int usedTime;           // 已运行时间
    private ProcessState state;     // 当前状态

    // === 统计信息 (用于算法评估) ===
    private int startTime;          // 开始运行时间
    private int finishTime;         // 完成时间
    private int turnAroundTime;     // 周转时间 = 完成时间 - 到达时间
    private double weightedTurnAroundTime; // 带权周转时间 = 周转时间 / 服务时间

    /**
     * 构造函数：创建一个新进程
     */
    public PCB(Integer pid, String name, int arrivalTime, int serviceTime, int priority, String color) {
        this.pid = pid;
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.serviceTime = serviceTime;
        this.priority = priority;
        this.color = color;

        // 初始化状态
        this.remainingTime = serviceTime;
        this.usedTime = 0;
        this.state = ProcessState.READY; // 默认为就绪
    }
}