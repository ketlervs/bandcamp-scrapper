package br.com.forestech.bandcampscrapping.controller;


import java.util.List;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.forestech.bandcampscrapping.service.Scrapper;

@RestController
@RequestMapping(path = "/recorder")
public class RecorderController {

    @RequestMapping(method = RequestMethod.GET, path = "/albuns-track-free")
    public List<String> getAlbunsOrTracksFreeByRecorder(@RequestParam("records") @ModelAttribute List<String> records) {
        System.out.println("xablau");
        return (new Scrapper()).getAlbunsOrTracksFreeByRecorder(records);
    }

}