package com.yunji.flurry.doc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * Api服务Controller
 *
 * @author leihuazhe
 * @date 2018-01-12 20:01
 */
@Controller
@RequestMapping("/")
public class IndexController {
    @ModelAttribute
    public void populateModel(Model model) {
        model.addAttribute("tagName", "index");
    }

    @RequestMapping(method = RequestMethod.GET)
    public String index(HttpServletRequest request) {
        return "index";
    }
}
