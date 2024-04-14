package br.com.forestech.bandcampscrapping.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrackController {

    @RequestMapping(method = RequestMethod.GET, path = "/")
    public String getAlbunsOrTracksFreeByRecorder() {
        return "Greetings from Spring Boot!";
    }

}