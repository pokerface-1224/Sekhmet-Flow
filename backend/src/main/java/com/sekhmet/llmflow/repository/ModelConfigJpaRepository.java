package com.sekhmet.llmflow.repository;

import com.sekhmet.llmflow.model.entity.ModelConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 模型配置 JPA 仓库
 * 提供对 SQLite model_configs 表的 CRUD 操作
 */
@Repository
public interface ModelConfigJpaRepository extends JpaRepository<ModelConfigEntity, String> {
    /** 按提供商查找所有模型配置 */
    List<ModelConfigEntity> findByProvider(String provider);
}
