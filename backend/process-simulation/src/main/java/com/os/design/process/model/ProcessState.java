package com.os.design.process.model;

/**
 * 进程状态枚举
 * 对应 Linux 内核中的 TASK_RUNNING, TASK_INTERRUPTIBLE 等状态
 */
public enum ProcessState {
    READY,      // 就绪态：已进入队列，等待 CPU
    RUNNING,    // 运行态：正在占用 CPU
    BLOCKED,    // 阻塞态：等待 I/O 或事件
    FINISHED    // 终止态：执行完成
}