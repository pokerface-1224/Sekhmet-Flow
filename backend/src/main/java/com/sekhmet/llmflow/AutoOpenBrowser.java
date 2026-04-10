package com.sekhmet.llmflow;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.awt.Desktop;
import java.net.URI;

// @Component  // 已禁用：取消启动时自动打开浏览器
public class AutoOpenBrowser {

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        String url = "http://localhost:1224";
        // 这里的打印非常重要，如果你在控制台没看到这一行，说明这个类没被 Spring 扫描到
        System.out.println(">>> [系统通知] 后端启动完成，正在尝试自动打开前端网页...");

        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("win")) {
                // Windows 环境：使用 ProcessBuilder 替代 Runtime.exec，消除警告
                new ProcessBuilder("cmd", "/c", "start", url).start();
            } else if (os.contains("mac")) {
                // Mac 环境
                new ProcessBuilder("open", url).start();
            } else {
                // Linux 环境 (如有图形界面)
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    new ProcessBuilder("xdg-open", url).start();
                }
            }
        } catch (Exception e) {
            System.err.println(">>> [错误] 自动打开浏览器失败，请手动访问: " + url);
            e.printStackTrace();
        }
    }
}
