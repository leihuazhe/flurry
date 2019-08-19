package com.yunji.gateway.doc.controller;

import com.yunji.gateway.process.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @Desc: RestApiController
 * post 请求示例
 * post  http://127.0.0.1:8700/rest/com.today.soa.idgen.service.IDService/1.0.0/genId
 * parameter = {"body":{"request":{"bizTag":"suplier","step":1}}}
 * @author: maple.lei
 * @Date: 2018-01-24 9:25
 */
@RestController
@RequestMapping("rest")
public class RestApiController {


    @Autowired
    private Post post;

    @PostMapping(value = "{service}/{version}/{method}")
    public String rest(@PathVariable(value = "service") String service,
                       @PathVariable(value = "version") String version,
                       @PathVariable(value = "method") String method,
                       @RequestParam(value = "parameter") String parameter,
                       HttpServletRequest req) {

        return post.post(service, version, method, parameter, req);
    }
}
