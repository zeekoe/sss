package nl.ronaldteune.sss.model;

import java.util.ArrayList;
import java.util.List;

public class Directory {
    private String id;
    private String name;
    private String parent;
    private List<Child> child = new ArrayList<>();

    public void addChild(Child c) {
        child.add(c);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public List<Child> getChild() {
        return child;
    }
}
