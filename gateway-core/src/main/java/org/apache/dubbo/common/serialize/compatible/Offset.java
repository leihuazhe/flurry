package org.apache.dubbo.common.serialize.compatible;

/**
 * @author Denim.leihz 2019-09-26 5:58 PM
 */
public class Offset {

    private int startIndex;

    private int endIndex;

    public Offset(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public Offset(int startIndex) {
        this.startIndex = startIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    @Override
    public String toString() {
        return "Offset{" + "startIndex=" + startIndex + ", endIndex=" + endIndex + '}';
    }
}
