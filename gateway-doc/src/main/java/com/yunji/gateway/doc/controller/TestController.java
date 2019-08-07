package com.yunji.gateway.doc.controller;

import com.yunji.api.doc.openapi.post.Post;
import com.yunji.gateway.doc.dto.TestTemplate;
import com.yunji.gateway.doc.dto.TestTemplateVo;
import com.yunji.gateway.doc.repository.TestTemplateRepository;
import com.yunji.gateway.doc.util.DataConvertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 测试Controller
 *
 * @author tangliu
 * @date 15/10/8
 */
@Controller
@RequestMapping("test")
public class TestController {
    private final Logger LOGGER = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private TestTemplateRepository repository;

    @Autowired
    private Post post;

    @ModelAttribute
    public void populateModel(Model model) {
        model.addAttribute("tagName", "test");
    }


    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String test(HttpServletRequest req) {

        String jsonParameter = req.getParameter("parameter");
        String serviceName = req.getParameter("serviceName");
        String versionName = req.getParameter("version");
        String methodName = req.getParameter("methodName");

        return post.post(serviceName, versionName, methodName, jsonParameter, req);
    }

    /**
     * 保存测试模版
     *
     * @param serviceName
     * @param version
     * @param method
     * @param template
     * @return
     */
    @PostMapping("/template/save")
    @ResponseBody
    public ResponseEntity<?> saveTemplate(@RequestParam("serviceName") String serviceName,
                                          @RequestParam("version") String version,
                                          @RequestParam("method") String method,
                                          @RequestParam("template") String template,
                                          @RequestParam("label") String label) {

        try {
            TestTemplate testTemplate = new TestTemplate();
            testTemplate.setId(UUID.randomUUID().toString());
            testTemplate.setServiceName(serviceName);
            testTemplate.setVersion(version);
            testTemplate.setMethod(method);
            testTemplate.setTemplate(DataConvertUtil.String2Clob(template));
            testTemplate.setLabel(label);
            testTemplate.setCreateDate(new Timestamp(System.currentTimeMillis()));
            testTemplate.setUpdateDate(new Timestamp(System.currentTimeMillis()));
            repository.save(testTemplate);
            LOGGER.info("::保存请求模版成功 [服务=>{}:{}:接口=>{}:{}:模版=>{}]", serviceName, version, method, label, template);
        } catch (Exception e) {
            LOGGER.info("::保存请求模版出错！[{}:{}:{}:{}:{}]", serviceName, version, method, label, template);
            LOGGER.info("{}", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("save template Error");
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("save template successful");
    }

    /**
     * 获取测试模版历史
     *
     * @param serviceName
     * @param version
     * @param method
     * @return
     */
    @GetMapping("/template/query")
    @ResponseBody
    public ResponseEntity<?> queryTemplate(@RequestParam("serviceName") String serviceName,
                                           @RequestParam("version") String version,
                                           @RequestParam("method") String method) {
        List<TestTemplateVo> vos = new ArrayList<>();
        try {
            List<TestTemplate> testTemplates = repository.findByServiceNameAndVersionAndMethod(serviceName, version, method);
            testTemplates.forEach(t -> {
                TestTemplateVo templateVo = new TestTemplateVo();
                templateVo.setId(t.getId());
                templateVo.setServiceName(t.getServiceName());
                templateVo.setLabel(t.getLabel());
                templateVo.setMethod(t.getMethod());
                templateVo.setTemplate(DataConvertUtil.Clob2String(t.getTemplate()));
                templateVo.setCreateDate(t.getCreateDate());
                templateVo.setUpdateDate(t.getUpdateDate());
                vos.add(templateVo);
            });
        } catch (Exception e) {
            LOGGER.info("::获取[ {}:{}:{} ] 请求模版出错！", serviceName, version, method);
            LOGGER.info("{}", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("query templates Error");
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(vos);
    }

    /**
     * 删除测试模版
     *
     * @param id
     * @return
     */
    @PostMapping("/template/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteTemplate(@PathVariable String id) {
        try {
            repository.delete(id);
        } catch (Exception e) {
            LOGGER.info("::删除[ {} ] 请求模版出错！", id);
            LOGGER.info("{}", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("delete template Error");
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(id);
    }

}
