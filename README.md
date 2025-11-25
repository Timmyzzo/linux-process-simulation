假如你是一名优秀的计算机专业的学生，你的操作系统原理课程设计选题是**进程管理模拟**，请你根据要求从头到尾详细地完成课程设计，你的选题是进程管理模拟，你是组长，你不善言辞尤其是演讲和答辩，但是想尽可能拿高分，又不想过多的演讲和答辩，请使用java设计，可以用springboot+vue的前后端分离设计，然后我有个阿里云服务器可以部署上去，可能可以作为加分项？但是我又想到一个其他问题，这不是在在 Linux 环境下编写吗？是不是要在VMware上的虚拟机编程？是不是不能用java，请你为我分析能用什么？如果可以用服务器的话，我发你的我服务器配置情况是Ubuntu 24.04，我记得我以前安装和配置过一些东西但我现在不记得了，请你指导我查看，我疑似安装过docker，配置了my sql，具体我也不清楚，忘记了，我现在已经成功连上了服务器反正。技术栈的话，springboot3+vue3最简单的即可，我想先在本地可以调试好，然后你再教我部署到服务器还是什么？具体要求如下：

课程设计的主要内容、要求：本次课程设计，均要求采用高级语言编写和调试程序，要求在屏幕上演示相关题目的模拟内容、设计功能，显示每一次操作的结果。具体详细功能和界面由学生自行设计。要求程序要有各种异常处理，保证程序的可控制性和可连续执行。鼓励创新设计

**选题：进程管理模拟**

（此题可以作为全国大学生计算机系统能力大赛操作系统设计赛（华东区域赛）OS 原理赛道赛题实验）

**设计内容：**在 Linux 环境下编写程序模拟实现操作系统的进程管理功能。

**具体要求：**

分析研究 Linux 进程管理源代码，在此基础上设计程序模拟实现进程管理，验证进程调度算法，并对结果进行分析比较。要求有图形化界面。设计须完成以下任务：

1.   Linux 进程管理源代码分析。
2.   设计程序模拟实现进程管理的基本功能，包括进程控制（进程创建、进程撤消、进程阻塞、进程唤醒）、进程调度等。鼓励模拟实现 Linux 或 OpenEuler 等开源操作系统中进程管理的新功能。

3.   **进程调度算法性能比较**。进程调度支持三种以上的调度算法，要对不同的调度算法的周转时间和带权周转时间进行比较，需对不同的调度算法进行深入分析比较。

三种基本进程调度算法如下,另需实现一种新的进程调度算法：

（1） 先来先服务调度算法；

（2） 最高优先数优先调度算法；

（3） “轮转法”调度算法

**考核形式：**

提交课程设计报告的打印稿（双面打印）、电子稿、程序，演示程序并进行设计内容的答辩。

**成绩评定规则：**

（1）成绩由三部分组成：报告、程序、答辩及小组成员之间的合作（出勤为基本条件）。

三、课程设计计划进度

第 11 周和第 12 周周二、周五下午，第 13 周周二下午，13:00-16:00

四、分组及报告要求

**1**、分组

三个同学一组，自由组合。每个小组选择完成 6 个题目中的一个，或者选择 OS 竞赛赛题。由组长在希冀平台上 Gitlab 作业中创建小组项目，将小组成员加入项目，共同完成代码和文档提交

**提交材料**

**1**）个人材料提交

（1）**每人交一份课程设计报告打印稿**，介绍系统总体功能及重点介绍个人完成部分，小组成员报告不得相同。报告格式见《课程设计报告》。课程设计报告打印稿，答辩时交给答辩老师。

（2）**每人交一份课程设计报告电子稿**。提交到智慧树，提交截止时间为答辩前一天晚上 23:59 之前，未提交者以旷考处理。课程设计报告电子版命名方式：学号+姓名+设计题目.doc , 例如：20211234-张三-进程管理模拟.doc

2）**小组材料提交**（提交截止时间为：答辩前一天晚上 23:59 之前）由小组在希冀平台 Gitlab 作业中的小组项目中提交，包括：

1 .完整工程文件，包括系统源代码(有代码注释，中英文均可)、辅助编译的文件、辅助建立编译环境的文件等；

2 .设计文档。包括设计开发的思路过程、各个模块的设计方案、实现重点与创新点、功能与性能的测试情况（包括与类似项目的对比分析）等。

3 .过程性资料。包括开发日志、课程设计中问题汇总文档（内容包括：遇到的问题（有截图）、解决方法等。）、线上仓库提交记录等。





首先我不是不想在形式主义的演讲上浪费精力，我是有一些口吃，不擅长演讲

然后是一些服务器现状：

Docker version 28.4.0, build d8eb465

docker ps
CONTAINER ID   IMAGE                          COMMAND                  CREATED        STATUS        PORTS                                                    NAMES
b954f1dcb283   openlistteam/openlist:latest   "/entrypoint.sh"         5 weeks ago    Up 5 weeks    0.0.0.0:5244->5244/tcp, [::]:5244->5244/tcp, 5245/tcp    openlist
d0b0048d8150   mysql:8.0                      "docker-entrypoint.s…"   2 months ago   Up 2 months   0.0.0.0:3306->3306/tcp, [::]:3306->3306/tcp, 33060/tcp   mysql-server

java version "21.0.8" 2025-07-15 LTS



然后我打算开始进行Springboot的骨架代码的下载，我打算在 [Spring Initializr](https://start.spring.io/)上下载，其中一些选项我应该选什么？文件的group，name等等都请你为我构思好，然后再进行后续

------

Project

[Gradle - Groovy](https://start.spring.io/)[Gradle - Kotlin](https://start.spring.io/)[Maven](https://start.spring.io/)

Language

[Java](https://start.spring.io/)[Kotlin](https://start.spring.io/)[Groovy](https://start.spring.io/)

Spring Boot

[4.0.1 (SNAPSHOT)](https://start.spring.io/)[4.0.0](https://start.spring.io/)[3.5.9 (SNAPSHOT)](https://start.spring.io/)[3.5.8](https://start.spring.io/)[3.4.13 (SNAPSHOT)](https://start.spring.io/)[3.4.12](https://start.spring.io/)

Project Metadata

Group

Artifact

Name

Description

Package name

Packaging

[Jar](https://start.spring.io/)[War](https://start.spring.io/)

Configuration

[Properties](https://start.spring.io/)[YAML](https://start.spring.io/)

Java

[25](https://start.spring.io/)[21](https://start.spring.io/)[17](https://start.spring.io/)

DependenciesAdd dependencies...Ctrl + b

-   **Spring Web Web**Build web, including RESTful, applications using Spring MVC. Uses Apache Tomcat as the default embedded container.
-   **Lombok Developer Tools**Java annotatio



哦对，我自己电脑的配置：

C:\Users\Acer>mvn -version
Apache Maven 3.9.11 (3e54c93a704957b63ee3494413a2b544fd3d825b)
Maven home: D:\software\tools\maven\apache-maven-3.9.11
Java version: 21.0.8, vendor: Oracle Corporation, runtime: C:\Program Files\Java\jdk-21
Default locale: zh_CN, platform encoding: UTF-8
OS name: "windows 11", version: "10.0", arch: "amd64", family: "windows"

C:\Users\Acer>java -version
java version "21.0.8" 2025-07-15 LTS

请你一步一步来，你前面的总流程我已经大概清楚，现在请你对我每个步骤的详细提问进行解答