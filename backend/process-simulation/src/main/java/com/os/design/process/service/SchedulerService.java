package com.os.design.process.service;

import com.os.design.process.model.PCB;
import com.os.design.process.model.ProcessState;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * 核心调度器服务
 * 模拟 OS 内核的调度逻辑
 */
@Service
public class SchedulerService {

    // === 核心数据结构 (内存中的 OS) ===

    // 全局时间 (模拟系统时钟)
    private int currentTime = 0;

    // 是否正在运行
    private boolean isRunning = false;

    // 当前选用的算法: "FCFS", "RR", "PSA", "SRTF"
    private String currentAlgorithm = "FCFS";

    // 时间片大小 (仅用于 RR 算法)
    private int timeSlice = 2;
    // 当前进程在当前时间片内已经运行的时间
    private int currentSliceUsed = 0;

    // === 进程队列 ===
    // 后备队列 (还没到达时间的进程)
    private List<PCB> backupQueue = Collections.synchronizedList(new ArrayList<>());

    // 就绪队列 (等待 CPU)
    private List<PCB> readyQueue = Collections.synchronizedList(new ArrayList<>());

    // 阻塞队列 (等待 I/O)
    private List<PCB> blockedQueue = Collections.synchronizedList(new ArrayList<>());

    // 完成队列 (已结束)
    private List<PCB> finishedQueue = Collections.synchronizedList(new ArrayList<>());

    // 当前正在 CPU 上跑的进程
    private PCB runningProcess = null;

    /**
     * 重置系统 (清空所有状态)
     */
    public void reset() {
        this.currentTime = 0;
        this.isRunning = false;
        this.runningProcess = null;
        this.currentSliceUsed = 0;
        this.backupQueue.clear();
        this.readyQueue.clear();
        this.blockedQueue.clear();
        this.finishedQueue.clear();
    }

    /**
     * 添加新进程 (用户提交)
     */
    public void addProcess(PCB pcb) {
        // === 补全数据 (防止空指针) ===
        if (pcb.getUsedTime() == null) pcb.setUsedTime(0);
        // 关键：剩余时间 = 服务时间
        if (pcb.getRemainingTime() == null) pcb.setRemainingTime(pcb.getServiceTime());

        // 如果到达时间 <= 当前时间，直接进就绪队列；否则进后备队列
        if (pcb.getArrivalTime() <= currentTime) {
            pcb.setState(ProcessState.READY);
            this.readyQueue.add(pcb);
        } else {
            this.backupQueue.add(pcb);
        }
    }

    /**
     * 核心逻辑：时钟中断模拟 (每次调用代表过了 1 秒)
     */
    public synchronized void tick() {
        if (!isRunning) return;

        // 1. 时间流逝
        currentTime++;

        // 2. 检查后备队列：有没有新进程到达？
        Iterator<PCB> it = backupQueue.iterator();
        while (it.hasNext()) {
            PCB p = it.next();
            if (p.getArrivalTime() <= currentTime) {
                p.setState(ProcessState.READY);
                readyQueue.add(p);
                it.remove();
            }
        }

        // 3. 处理当前正在运行的进程
        if (runningProcess != null) {
            // 更新运行时间
            runningProcess.setUsedTime(runningProcess.getUsedTime() + 1);
            runningProcess.setRemainingTime(runningProcess.getRemainingTime() - 1);
            currentSliceUsed++;

            // A. 判断是否完成
            if (runningProcess.getRemainingTime() <= 0) {
                finishProcess(runningProcess);
                runningProcess = null;
                currentSliceUsed = 0;
            }
            // B. 抢占式逻辑 (PSA 和 SRTF)
            else if ("PSA".equals(currentAlgorithm) || "SRTF".equals(currentAlgorithm)) {
                boolean shouldPreempt = false;

                if ("PSA".equals(currentAlgorithm)) {
                    // PSA: 检查是否有优先级更高的 (数值越大优先级越高)
                    shouldPreempt = readyQueue.stream()
                            .anyMatch(p -> p.getPriority() > runningProcess.getPriority());
                } else {
                    // SRTF: 检查是否有剩余时间更短的
                    shouldPreempt = readyQueue.stream()
                            .anyMatch(p -> p.getRemainingTime() < runningProcess.getRemainingTime());
                }

                if (shouldPreempt) {
                    // 被抢占！
                    runningProcess.setState(ProcessState.READY);
                    readyQueue.add(runningProcess); // 放回就绪队列
                    runningProcess = null;          // 腾出 CPU
                    currentSliceUsed = 0;
                }
            }
            // C. RR 时间片逻辑
            else if ("RR".equals(currentAlgorithm) && currentSliceUsed >= timeSlice) {
                // 时间片到了，放回就绪队列队尾
                runningProcess.setState(ProcessState.READY);
                readyQueue.add(runningProcess);
                runningProcess = null;
                currentSliceUsed = 0;
            }
        }

        // 4. 进程调度 (如果 CPU 空闲)
        if (runningProcess == null && !readyQueue.isEmpty()) {
            dispatch();
        }
    }

    /**
     * 调度决策：从就绪队列选一个进程给 CPU
     */
    private void dispatch() {
        // 根据算法排序就绪队列
        sortReadyQueue();

        // 取出第一个
        PCB next = readyQueue.remove(0);
        next.setState(ProcessState.RUNNING);

        // 如果是第一次运行，记录开始时间
        if (next.getUsedTime() == 0) {
            next.setStartTime(currentTime);
        }

        this.runningProcess = next;
    }

    /**
     * 核心算法实现区：根据不同策略对就绪队列排序
     */
    private void sortReadyQueue() {
        switch (currentAlgorithm) {
            case "FCFS": // 先来先服务：按到达时间
                readyQueue.sort(Comparator.comparingInt(PCB::getArrivalTime));
                break;
            case "PSA":  // 优先级调度：按优先级倒序 (数值大优先级高)
                readyQueue.sort((p1, p2) -> p2.getPriority() - p1.getPriority());
                break;
            case "SRTF": // 最短剩余时间优先：按剩余时间升序
                readyQueue.sort(Comparator.comparingInt(PCB::getRemainingTime));
                break;
            case "RR":   // 轮转法：不做排序，直接取队头 (FIFO)
                break;
            default:
                break;
        }
    }

    /**
     * 强制撤销进程 (Kill) - 新增功能
     */
    public synchronized void killProcess(int pid) {
        // 1. 检查运行区
        // 使用 equals 比较，更安全
        if (runningProcess != null && runningProcess.getPid().equals(pid)) {
            runningProcess = null;
            currentSliceUsed = 0;
            return;
        }

        // 2. 检查就绪队列 (使用 Objects.equals 或 equals)
        readyQueue.removeIf(p -> p.getPid().equals(pid));

        // 3. 检查阻塞队列
        blockedQueue.removeIf(p -> p.getPid().equals(pid));

        // 4. 检查后备队列
        backupQueue.removeIf(p -> p.getPid().equals(pid));
    }

    /**
     * 进程完成处理
     */
    private void finishProcess(PCB p) {
        p.setState(ProcessState.FINISHED);
        p.setFinishTime(currentTime);
        // 计算周转时间 = 完成 - 到达
        p.setTurnAroundTime(p.getFinishTime() - p.getArrivalTime());
        // 计算带权周转 = 周转 / 服务
        if (p.getServiceTime() > 0) {
            p.setWeightedTurnAroundTime((double) p.getTurnAroundTime() / p.getServiceTime());
        } else {
            p.setWeightedTurnAroundTime( ( double ) 0 );
        }
        finishedQueue.add(p);
    }

    // === Getters & Setters for Controller ===

    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("currentTime", currentTime);
        status.put("isRunning", isRunning);
        status.put("algorithm", currentAlgorithm);
        status.put("runningProcess", runningProcess);
        status.put("readyQueue", new ArrayList<>(readyQueue));
        status.put("blockedQueue", new ArrayList<>(blockedQueue));
        status.put("finishedQueue", new ArrayList<>(finishedQueue));
        status.put("backupQueue", new ArrayList<>(backupQueue));
        return status;
    }

    public void setRunning(boolean running) { isRunning = running; }
    public void setAlgorithm(String algo) { this.currentAlgorithm = algo; }
    public void setTimeSlice(int slice) { this.timeSlice = slice; }

    public void blockCurrentProcess() {
        if (runningProcess != null) {
            runningProcess.setState(ProcessState.BLOCKED);
            blockedQueue.add(runningProcess);
            runningProcess = null;
            currentSliceUsed = 0;
        }
    }

    public void wakeUpProcess(int pid) {
        Iterator<PCB> it = blockedQueue.iterator();
        while (it.hasNext()) {
            PCB p = it.next();
            if (p.getPid().equals(pid)) {
                p.setState(ProcessState.READY);
                readyQueue.add(p);
                it.remove();
                break;
            }
        }
    }
}