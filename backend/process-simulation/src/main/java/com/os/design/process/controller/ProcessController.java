package com.os.design.process.controller;

import com.os.design.process.common.Result;
import com.os.design.process.model.PCB;
import com.os.design.process.service.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // 允许跨域，方便前端调试
public class ProcessController {

    @Autowired
    private SchedulerService schedulerService;

    // 1. 获取系统全量状态 (前端每秒轮询这个接口)
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus() {
        return Result.success(schedulerService.getSystemStatus());
    }

    // 2. 添加进程
    @PostMapping("/process")
    public Result<String> addProcess(@RequestBody PCB pcb) {
        schedulerService.addProcess(pcb);
        return Result.success("Process added");
    }

    // 3. 控制：开始/暂停
    @PostMapping("/control/run")
    public Result<String> run(@RequestParam boolean run) {
        schedulerService.setRunning(run);
        return Result.success(run ? "System started" : "System paused");
    }

    // 4. 控制：时钟步进 (前端每秒调一次，驱动系统走一秒)
    @PostMapping("/control/tick")
    public Result<String> tick() {
        schedulerService.tick();
        return Result.success("Ticked");
    }

    // 5. 控制：重置
    @PostMapping("/control/reset")
    public Result<String> reset() {
        schedulerService.reset();
        return Result.success("System reset");
    }

    // 6. 设置算法
    @PostMapping("/config/algorithm")
    public Result<String> setAlgorithm(@RequestParam String algorithm, @RequestParam(defaultValue = "2") int timeSlice) {
        schedulerService.setAlgorithm(algorithm);
        schedulerService.setTimeSlice(timeSlice);
        return Result.success("Algorithm switched to " + algorithm);
    }

    // 7. 手动阻塞当前进程
    @PostMapping("/process/block")
    public Result<String> block() {
        schedulerService.blockCurrentProcess();
        return Result.success("Process blocked");
    }

    // 8. 唤醒进程
    @PostMapping("/process/wake")
    public Result<String> wake(@RequestParam int pid) {
        schedulerService.wakeUpProcess(pid);
        return Result.success("Process woke up");
    }
}