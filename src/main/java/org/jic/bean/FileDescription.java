package org.jic.bean;


public class FileDescription {
    private String name;
    private long size;
    private int parent;
    private int id;

    public FileDescription(String name, long size, int id, int parent) {
        this.name = name;
        this.size = size;
        this.id = id;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
