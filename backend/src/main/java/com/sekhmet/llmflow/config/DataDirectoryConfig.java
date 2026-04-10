package com.sekhmet.llmflow.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 数据目录与数据源配置
 * 自动解析 data 目录路径，兼容 IDE 和 Maven 两种启动方式
 */
@Configuration
@Slf4j
public class DataDirectoryConfig {

    @Value("${sekhmet.data-dir:../data}")
    private String configuredDataDir;

    @Getter
    private Path resolvedDataDir;

    @PostConstruct
    public void init() throws IOException {
        resolvedDataDir = resolveDataDirectory();
        Files.createDirectories(resolvedDataDir);
        log.info("Data directory resolved to: {}", resolvedDataDir);
    }

    /**
     * 配置 SQLite 数据源
     * 使用解析后的数据目录路径，确保与工作流存储在同一目录下
     */
    @Bean
    public DataSource dataSource() throws IOException {
        Path dataDir = resolveDataDirectory();
        Files.createDirectories(dataDir);

        String dbPath = dataDir.resolve("sekhmet.db").toAbsolutePath().toString();
        log.info("SQLite database path: {}", dbPath);

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:sqlite:" + dbPath);
        ds.setDriverClassName("org.sqlite.JDBC");
        // SQLite 只支持单连接写入
        ds.setMaximumPoolSize(1);
        return ds;
    }

    /**
     * 解析数据目录路径
     * 优先使用配置的路径，如果不存在则尝试从项目根目录查找
     */
    private Path resolveDataDirectory() {
        Path configured = Paths.get(configuredDataDir);
        if (Files.exists(configured)) {
            return configured.toAbsolutePath().normalize();
        }

        // 回退：尝试 ./data (适用于从项目根目录运行，如 IDE 启动)
        Path fallback = Paths.get("data");
        if (Files.exists(fallback)) {
            log.info("Using fallback data directory: ./data (IDE mode)");
            return fallback.toAbsolutePath().normalize();
        }

        // 都不存在，使用配置值并创建目录
        return configured.toAbsolutePath().normalize();
    }
}
