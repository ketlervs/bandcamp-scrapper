package br.com.forestech.bandcampscrapping.model;

import java.util.ArrayList;
import java.util.List;

public class Link {
    private String link;
    private String genero;
    private List<Link> tracks = new ArrayList<>();

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getGenero() {
        return this.genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    
    public Link (String link, String genero){
        this.link = link;
        this.genero = genero;
    }
    public Link (String link){
        this.link = link;
    }
    

    public List<Link> getTracks() {
        return this.tracks;
    }

    public void setTracks(List<Link> tracks) {
        this.tracks = tracks;
    }
    
}
