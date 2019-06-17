package nl.ronaldteune.sss.model;

import java.util.List;

public class Index {
    private String name;
    private List<Artist> artist;

    public Index(String name, List<Artist> artist) {
        this.name = name;
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public List<Artist> getArtist() {
        return artist;
    }
}
