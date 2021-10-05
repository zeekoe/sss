package nl.ronaldteune.sss.model;

public class MusicFolder {
    String id;
    String name;

    public MusicFolder(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
