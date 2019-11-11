package com.yunji.gateway.doc.controller;

import com.yunji.gateway.doc.dto.EventDto;
import com.yunji.gateway.doc.dto.EventVo;
import com.yunji.gateway.doc.util.ServiceAnnotationsUtil;
import com.yunji.gateway.doc.util.ServiceJsonUtil;
import com.yunji.gateway.metadata.OptimizedService;
import com.yunji.gateway.metadata.core.ExportServiceManager;
import com.yunji.gateway.metadata.tag.Method;
import com.yunji.gateway.metadata.tag.Service;
import com.yunji.gateway.metadata.tag.Struct;
import com.yunji.gateway.metadata.tag.TEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Api服务Controller
 *
 * @author leihuazhe
 * @date 2018-01-12 20:01
 */
@Controller
@RequestMapping(value = "api")
public class ApiServiceController {
    private static Logger LOGGER = LoggerFactory.getLogger(ApiServiceController.class);

    @ModelAttribute
    public void populateModel(Model model) {
        model.addAttribute("tagName", "api");
    }

    @RequestMapping(value = "index", method = RequestMethod.GET)
    public String api(HttpServletRequest request) {
        Map<String, OptimizedService> metaServices = ExportServiceManager.getInstance().getServiceMetadataMap();

        List<Service> services = metaServices.values().stream().map(OptimizedService::getService).collect(Collectors.toList());
        Map<String, Collection<Service>> groupedServices = ServiceAnnotationsUtil.groupingServices(services);
        request.setAttribute("services", services);
        request.setAttribute("groupedServices", groupedServices);
        return "api/api";
    }

    @RequestMapping(value = "service/{serviceName}/{version}", method = RequestMethod.GET)
    @Transactional(rollbackFor = Exception.class)
    public String service(HttpServletRequest request, @PathVariable String serviceName, @PathVariable String version) {
        Map<String, OptimizedService> metaServices = ExportServiceManager.getInstance().getServiceMetadataMap();
        OptimizedService metaService = ExportServiceManager.getInstance().getMetadata(serviceName, version);

        Service service = metaService.getService();
        List<Service> services = metaServices.values().stream().map(OptimizedService::getService).collect(Collectors.toList());
        List<EventVo> events = ServiceAnnotationsUtil.findEvents(service);
        request.setAttribute("events", events);
        request.setAttribute("service", ServiceAnnotationsUtil.excludeMethods(service));
        request.setAttribute("services", services);
        StringBuilder logBuilder = new StringBuilder();

        metaServices.forEach((k, v) -> logBuilder.append(k + ",  "));
        LOGGER.info("请求service列表展示，当前服务实例为 {}", logBuilder);

        return "api/service";
    }

    @RequestMapping(value = "method/{serviceName}/{version}/{methodName}", method = RequestMethod.GET)
    @Transactional(rollbackFor = Exception.class)
    public String method(HttpServletRequest request, @PathVariable String serviceName, @PathVariable String version, @PathVariable String methodName) {
        OptimizedService optimizedService = ExportServiceManager.getInstance().getMetadata(serviceName, version);
        Method seleted = optimizedService.getMethodMap().get(methodName);
        List<Method> methods = optimizedService.getService().getMethods();

        Collections.sort(methods, Comparator.comparing(Method::getName));
        List<EventDto> events = ServiceAnnotationsUtil.findEventsByMethod(seleted);

        request.setAttribute("events", events);
        request.setAttribute("service", optimizedService.getService());
        request.setAttribute("methods", methods);
        request.setAttribute("method", seleted);
        return "api/method";
    }

    @RequestMapping(value = "findmethod/{serviceName}/{version}/{methodName}", method = RequestMethod.GET)
    @ResponseBody
    public Method findMethod(@PathVariable String serviceName, @PathVariable String version, @PathVariable String methodName) {
        OptimizedService optimizedService = ExportServiceManager.getInstance().getMetadata(serviceName, version);

        return optimizedService.getMethodMap().get(methodName);
    }

    @RequestMapping(value = "struct/{serviceName}/{version}/{ref}", method = RequestMethod.GET)
    @Transactional(rollbackFor = Exception.class)
    public String struct(HttpServletRequest request, @PathVariable String serviceName, @PathVariable String version, @PathVariable String ref) {
        OptimizedService optimizedService = ExportServiceManager.getInstance().getMetadata(serviceName, version);

        request.setAttribute("struct", optimizedService.getOptimizedStructs().get(ref).getStruct());
        request.setAttribute("service", optimizedService.getService());
        request.setAttribute("structs", optimizedService.getService().getStructDefinitions());
        return "api/struct";
    }

    @RequestMapping(value = "findstruct/{serviceName}/{version}/{fullStructName}", method = RequestMethod.GET)
    @ResponseBody
    public Struct findStruct(@PathVariable String serviceName, @PathVariable String version, @PathVariable String fullStructName) {
        OptimizedService optimizedService = ExportServiceManager.getInstance().getMetadata(serviceName, version);

        return optimizedService.getOptimizedStructs().get(fullStructName).getStruct();
    }

    @RequestMapping(value = "findEnum/{serviceName}/{version}/{ref}", method = RequestMethod.GET)
    @ResponseBody
    public TEnum findEnum(@PathVariable String serviceName, @PathVariable String version, @PathVariable String ref) {
        OptimizedService optimizedService = ExportServiceManager.getInstance().getMetadata(serviceName, version);

        return optimizedService.getEnumMap().get(ref);
    }

    /**
     * 返回枚举 json async
     *
     * @param request
     * @param serviceName
     * @param version
     * @param ref
     * @return
     */
    @RequestMapping(value = "enum/{serviceName}/{version}/{ref}", method = RequestMethod.GET)
    @Transactional(rollbackFor = Exception.class)
    public String anEnum(HttpServletRequest request, @PathVariable String serviceName, @PathVariable String version, @PathVariable String ref) {
        OptimizedService optimizedService = ExportServiceManager.getInstance().getMetadata(serviceName, version);

        request.setAttribute("anEnum", optimizedService.getEnumMap().get(ref));

        request.setAttribute("service", optimizedService.getService());
        request.setAttribute("enums", optimizedService.getService().getEnumDefinitions());
        return "api/enum";
    }


    @RequestMapping(value = "test/{serviceName}/{version}/{methodName}", method = RequestMethod.GET)
    @Transactional(rollbackFor = Exception.class)
    public String goTest(HttpServletRequest request, @PathVariable String serviceName, @PathVariable String version, @PathVariable String methodName) {
        OptimizedService optimizedService = ExportServiceManager.getInstance().getMetadata(serviceName, version);

        request.setAttribute("service", optimizedService.getService());
        request.setAttribute("method", optimizedService.getMethodMap().get(methodName));
        request.setAttribute("services", ExportServiceManager.getInstance().getServiceMetadataMap().values()
                .stream().map(OptimizedService::getService).collect(Collectors.toList()));
        return "api/test";
    }

    @RequestMapping(value = "findService/{serviceName}/{version}", method = RequestMethod.GET)
    @ResponseBody
    public Service findService(@PathVariable String serviceName, @PathVariable String version) {
        return ExportServiceManager.getInstance().getMetadata(serviceName, version).getService();
    }

    @RequestMapping(value = "findServiceAfterRefresh/{serviceName}/{version}/{refresh}", method = RequestMethod.GET)
    @ResponseBody
    public Service findService(@PathVariable String serviceName, @PathVariable String version, @PathVariable boolean refresh) {
        /*if (refresh) {
            serviceCache.reloadServices();
        }*/
        return ExportServiceManager.getInstance().getMetadata(serviceName, version).getService();
    }

    /**
     * 解析枚举信息，转为Json格式
     *
     * @param serviceName 服务名
     * @param version     版本号
     * @return Json 字符串
     * @author maple.lei
     */
    @RequestMapping(value = "enum/{serviceName}/{version}/jsonEnum", method = RequestMethod.GET)
    @Transactional(rollbackFor = Exception.class)
    @ResponseBody
    public Object getEnumJson(@PathVariable String serviceName, @PathVariable String version) {
        Service service = ExportServiceManager.getInstance().getMetadata(serviceName, version).getService();
        Map<String, Object> stringObjectMap = ServiceJsonUtil.executeJson(service);
        return stringObjectMap.get(ServiceJsonUtil.JSONOBJ);

    }

    @RequestMapping(value = "enum/{serviceName}/{version}/jsonEnumString", method = RequestMethod.GET)
    @Transactional(rollbackFor = Exception.class)
    @ResponseBody
    public String getEnumJsonString(@PathVariable String serviceName, @PathVariable String version) {
        Service service = ExportServiceManager.getInstance().getMetadata(serviceName, version).getService();
        Map<String, Object> stringObjectMap = ServiceJsonUtil.executeJson(service);

        return (String) stringObjectMap.get(ServiceJsonUtil.JSONSTR);
    }

}
