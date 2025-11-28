package com.os.design.process.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 进程控制块 (Process Control Block)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PCB {
    // === 基础信息 ===
    private Integer pid;            // 进程标识符
    private String name;            // 进程名
    private String color;           // 颜色

    // === 调度信息 ===
    // 这里的 int 全部改为 Integer，防止前端没传值时报错
    private Integer arrivalTime;        // 到达时间
    private Integer serviceTime;        // 服务时间
    private Integer priority;           // 优先级

    // === 运行状态 ===
    // 给一些字段设置默认值，以防万一
    private Integer remainingTime;      // 剩余时间
    private Integer usedTime = 0;       // 已运行时间 (默认为0)
    private ProcessState state = ProcessState.READY; // 默认就绪

    // === 统计信息 ===
    private Integer startTime;
    private Integer finishTime;
    private Integer turnAroundTime;
    private Double weightedTurnAroundTime; // double 也要改 Double

    // 为了方便逻辑处理，我们还需要确保初始化时 remainingTime = serviceTime
    // 但因为使用了 Lombok，我们可以在 Service 层处理，或者前端传过来
    // 最简单的办法：在 SchedulerService.addProcess 里补全数据
}