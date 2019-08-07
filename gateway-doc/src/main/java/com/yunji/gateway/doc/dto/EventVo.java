package com.yunji.gateway.doc.dto;

import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * @author with struy.
 * Create by 2018/2/25 22:33
 * email :yq1724555319@gmail.com
 */
@Data
public class EventVo {

    /**
     * 触发方法列表
     */
    private List<String> touchMethods;

    /**
     * 事件结构体名称
     */
    private String event;


    /**
     * 事件简称
     */
    private String shortName;

    /**
     * 事件简介
     */
    private String mark;

    public EventVo() {
    }

    public EventVo(List<String> touchMethods, String event, String shortName, String mark) {
        this.touchMethods = touchMethods;
        this.event = event;
        this.shortName = shortName;
        this.mark = mark;
    }


}
