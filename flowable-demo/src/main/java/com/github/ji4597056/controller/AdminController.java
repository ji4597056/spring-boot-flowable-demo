package com.github.ji4597056.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;

/**
 * admin controller
 *
 * @author Jeffrey
 * @since 2017/4/26 11:15
 */
@Controller
@RequestMapping("/admin")
@ApiIgnore
public class AdminController {

    /**
     * redirect swagger-ui
     *
     * @return swagger-ui
     */
    @GetMapping("/api")
    public String redirectSwaggerApi() {
        return "redirect:/swagger-ui.html";
    }

}
