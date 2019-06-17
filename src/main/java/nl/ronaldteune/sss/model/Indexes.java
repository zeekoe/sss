package nl.ronaldteune.sss.model;

import java.util.ArrayList;
import java.util.List;

public class Indexes {
    private List<Index> index = new ArrayList<>();

    public Indexes(List<Index> index) {
        this.index = index;
    }

    public String getIgnoredArticles() {
        return "";
    }

    public Integer getLastModified() {
        return 0;
    }

    public List<Index> getIndex() {
        return index;
    }
}
