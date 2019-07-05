package org.apache.dubbo.demo;

import java.io.Serializable;
import java.util.Map;

public class Column implements Serializable {
    private final String name;

    private final int count;

    private final Map<String, String> attachments;

    public Column(String name, int count, Map<String, String> attachments) {
        this.name = name;
        this.count = count;
        this.attachments = attachments;
    }


    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    @Override
    public String toString() {
        return "Column{" +
                "name='" + name + '\'' +
                ", count=" + count +
                ", attachments=" + attachments +
                '}';
    }
}
