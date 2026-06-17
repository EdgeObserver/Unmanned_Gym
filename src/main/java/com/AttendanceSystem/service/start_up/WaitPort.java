package com.AttendanceSystem.service.start_up;

public class WaitPort {
    public static void waitPorts(int... ports) throws InterruptedException {
        for (int port : ports) {
            System.out.println(">>> 开始检测端口 " + port);
            boolean ready = false;
            for (int i = 1; i <= 30; i++) {
                System.out.println("waiting " + port + " (" + i + ")");
                try (java.net.Socket s = new java.net.Socket("localhost", port)) {
                    ready = true;
                    System.out.println("=> 端口 " + port + " 已就绪");
                    break;
                } catch (Exception ignore) {
                    Thread.sleep(1000);
                }
            }
            if (!ready) {
                System.err.println("端口 " + port + " 30 秒内未就绪，终止启动");
                System.exit(1);
            }
        }
        System.out.println(">>> 全部端口就绪，继续启动");
    }
}