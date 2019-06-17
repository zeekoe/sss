package nl.ronaldteune.sss.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/** @noinspection unused*/
@DatabaseTable(tableName = "albums")
public class DBAlbum {
    public DBAlbum() {
    }

    public DBAlbum(int id, String url, String artist, String title) {
        this.id = id;
        this.url = url;
        this.artist = artist;
        this.title = title;
    }

    @DatabaseField private int id;
    @DatabaseField private String url;
    @DatabaseField private String artist;
    @DatabaseField private String title;

    public int getId() {
        return id;
    }

    public String getIdString() {
        return "" + id;
    }


    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
