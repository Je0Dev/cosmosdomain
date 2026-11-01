package com.cosmosdomain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the public landing page.
 */
@Controller
public class HomeController {

    // The only public page; it adapts its call-to-action based on login state.
    @GetMapping("/")
    public String home() {
        return "home";
    }
}