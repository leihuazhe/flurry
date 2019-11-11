package com.yunji.flurry.doc.controller;

import com.google.gson.Gson;
import com.yunji.flurry.doc.dto.MetaDto;
import com.yunji.flurry.metadata.OptimizedService;
import com.yunji.flurry.metadata.core.ExportServiceManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Denim.leihz 2019-08-01 4:00 PM
 */
@RestController()
@RequestMapping("/api/meta")
public class MetadataController {

    private final Gson gson = new Gson();


    @RequestMapping("/show")
    public String getMetadata() {
        Map<String, OptimizedService> serviceMap = ExportServiceManager.getInstance().getServiceMetadataMap();

        List<MetaDto> metaDtoList = new ArrayList<>();

        serviceMap.forEach((key, value) -> {

            String service = value.service.getNamespace() + "." + value.service.getName();
            String version = value.service.getMeta().version;

            List<String> methods = value.service.getMethods().stream().map(x -> x.name).collect(Collectors.toList());

            MetaDto metaDto = MetaDto.builder().serviceName(service)
                    .methodList(methods)
                    .version(version).build();

            metaDtoList.add(metaDto);

        });

        return gson.toJson(metaDtoList);
    }
}
