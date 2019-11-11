package com.yunji.flurry.doc.dto;

import lombok.Data;

/**
 * @author struy
 */
@Data
public class TestTemplateVo {

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
    private String template;
    /**
     * 模版标签名
     */
    private String label;
    private java.sql.Timestamp createDate;
    private java.sql.Timestamp updateDate;

}
