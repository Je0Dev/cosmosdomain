package com.cosmosdomain.controller;

import com.cosmosdomain.dto.TmdbPersonDetail;
import com.cosmosdomain.service.TmdbClient;
import com.cosmosdomain.config.TmdbProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PersonController {

    private final TmdbClient tmdbClient;
    private final TmdbProperties props;

    public PersonController(TmdbClient tmdbClient, TmdbProperties props) {
        this.tmdbClient = tmdbClient;
        this.props = props;
    }

    @GetMapping("/person/{tmdbId}")
    public String personDetail(@PathVariable Long tmdbId, Model model) {
        TmdbPersonDetail person = tmdbClient.getPersonDetail(tmdbId);
        if (person == null) {
            return "redirect:/search";
        }
        model.addAttribute("person", person);
        model.addAttribute("imageBaseUrl", props.getImageBaseUrl());
        model.addAttribute("posterSize", props.getPosterSize());
        return "person-detail";
    }
}