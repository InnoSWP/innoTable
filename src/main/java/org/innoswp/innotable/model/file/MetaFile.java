package org.innoswp.innotable.model.file;

public abstract class MetaFile {
    protected final String content;

    public MetaFile(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
