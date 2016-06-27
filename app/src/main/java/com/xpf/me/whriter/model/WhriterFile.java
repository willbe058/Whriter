package com.xpf.me.whriter.model;

import java.io.Serializable;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by pengfeixie on 16/5/29.
 */
public class WhriterFile extends RealmObject implements Serializable {

    @PrimaryKey
    private String id;

    private String title = "";

    private long createDate;

    private long modifyDate;

    private WhriterFile currentFolder;

    private WhriterFile previousFolder;

    private boolean isFile;

    private boolean isRoot;

    private String content = "";

    private String preview = "";

    private int count;

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }

    public long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(long modifyDate) {
        this.modifyDate = modifyDate;
    }

    private RealmList<WhriterFile> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public WhriterFile getCurrentFolder() {
        return currentFolder;
    }

    public void setCurrentFolder(WhriterFile currentFolder) {
        this.currentFolder = currentFolder;
    }

    public WhriterFile getPreviousFolder() {
        return previousFolder;
    }

    public void setPreviousFolder(WhriterFile previousFolder) {
        this.previousFolder = previousFolder;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public RealmList<WhriterFile> getChildren() {
        return children;
    }

    public void setChildren(RealmList<WhriterFile> children) {
        this.children = children;
    }
}
