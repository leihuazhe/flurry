package com.yunji.gateway.doc.dto;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Clob;

/**
 * @author struy
 */
@Entity
@Table(name = "test_template")
@Data
public class TestTemplate {

    @Id
    private String id;
    /**
     * 服务名
     */
    private String serviceName;
    /**
     * 服务版本
     */
    private String version;
    /**
     * 方法名
     */
    private String method;
    /**
     * 请求json串
     */
    private Clob template;
    /**
     * 模版标签名
     */
    private String label;
    private java.sql.Timestamp createDate;
    private java.sql.Timestamp updateDate;
}
