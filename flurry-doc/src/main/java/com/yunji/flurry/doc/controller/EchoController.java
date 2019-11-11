package com.yunji.flurry.doc.controller;

import com.yunji.flurry.doc.util.BaseObject;
import com.yunji.flurry.doc.util.MixUtils;
import com.yunji.flurry.process.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * EchoController
 *
 * @author tangliu
 * @date 15/10/8
 */
@Controller
@RequestMapping("/echo")
public class EchoController {
    @Autowired
    private Post post;

    @ModelAttribute
    public void populateModel(Model model) {
        model.addAttribute("tagName", "test");
    }


    @RequestMapping
    @ResponseBody
    public Object echo(String service, String version) {
        try {
            return post.post(service, version, MixUtils.ECHO_NAME, null, null);
        } catch (Exception e) {
            return new BaseObject(1000L, "Echo返回异常: " + e.getMessage());
        }
    }


}
