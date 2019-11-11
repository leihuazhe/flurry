package com.yunji.flurry.doc.controller;

import com.yunji.flurry.metadata.OptimizedService;
import com.yunji.flurry.metadata.core.ExportServiceManager;
import com.yunji.flurry.metadata.tag.Method;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author struy
 * @date 2017/04/17
 */
@Controller
@RequestMapping("search")
public class SearchController {

    /**
     * 只针对文档站点进行使用。url展示
     */
    Map<String, String> urlMappings = new ConcurrentHashMap<>();

    @ModelAttribute
    public void populateModel(Model model) {
        model.addAttribute("tagName", "search");
    }

    @RequestMapping(method = RequestMethod.POST)
    public String index(HttpServletRequest request) {
        Collection<OptimizedService> values = ExportServiceManager.getInstance().getServiceMetadataMap().values();

        loadServiceUrl(values);

        String searchText = request.getParameter("searchText");
        List<SearchResultItem> resultList = new ArrayList<>();

        Set<String> keys = urlMappings.keySet();
        for (String key : keys) {

            if (key.toUpperCase().contains(searchText.toUpperCase())) {
                Set<String> results = Collections.singleton(urlMappings.get(key));
                for (String value : results) {

                    SearchResultItem s = new SearchResultItem();
                    s.setName(key);
                    s.setUrl(value);

                    resultList.add(s);
                }
            }
        }
        request.setAttribute("services", values);
        request.setAttribute("resultList", resultList);
        return "api/search";
    }


    /**
     * 将service和service中的方法、结构体、枚举和字段名分别设置对应的url，以方便搜索
     */
    private void loadServiceUrl(Collection<OptimizedService> optimizedServices) {

        for (OptimizedService optimizedService : optimizedServices) {
            com.yunji.flurry.metadata.tag.Service service = optimizedService.service;
            //将service和service中的方法、结构体、枚举和字段名分别设置对应的url，以方便搜索
            urlMappings.put(service.getName(), "api/service/" + service.name + "/" + service.meta.version + ".htm");
            List<com.yunji.flurry.metadata.tag.Method> methods = service.getMethods();
            for (int i = 0; i < methods.size(); i++) {
                Method method = methods.get(i);
                urlMappings.put(method.name, "api/method/" + service.name + "/" + service.meta.version + "/" + method.name + ".htm");
            }

            List<com.yunji.flurry.metadata.tag.Struct> structs = service.getStructDefinitions();
            for (int i = 0; i < structs.size(); i++) {
                com.yunji.flurry.metadata.tag.Struct struct = structs.get(i);
                urlMappings.put(struct.name, "api/struct/" + service.name + "/" + service.meta.version + "/" + struct.namespace + "." + struct.name + ".htm");

                List<com.yunji.flurry.metadata.tag.Field> fields = struct.getFields();
                for (int j = 0; j < fields.size(); j++) {
                    com.yunji.flurry.metadata.tag.Field field = fields.get(j);
                    urlMappings.put(field.name, "api/struct/" + service.name + "/" + service.meta.version + "/" + struct.namespace + "." + struct.name + ".htm");
                }
            }

            List<com.yunji.flurry.metadata.tag.TEnum> tEnums = service.getEnumDefinitions();
            for (int i = 0; i < tEnums.size(); i++) {
                com.yunji.flurry.metadata.tag.TEnum tEnum = tEnums.get(i);
                urlMappings.put(tEnum.name, "api/enum/" + service.name + "/" + service.meta.version + "/" + tEnum.namespace + "." + tEnum.name + ".htm");
            }
        }
    }


    public class SearchResultItem {

        public String name;

        public String url;

        public SearchResultItem() {
            name = "";
            url = "";
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
