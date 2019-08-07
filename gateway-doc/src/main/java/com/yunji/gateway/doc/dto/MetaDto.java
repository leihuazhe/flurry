package com.yunji.gateway.doc.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author Denim.leihz 2019-08-01 4:02 PM
 */
@Data
@Builder
public class MetaDto {

    private String serviceName;

    private String version;

    private List<String> methodList;
}
