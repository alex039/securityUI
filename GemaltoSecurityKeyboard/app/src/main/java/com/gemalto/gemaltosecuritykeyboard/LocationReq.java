package com.gemalto.gemaltosecuritykeyboard;
import java.util.Arrays;

/**
 * Created by chengcheng on 2017/12/27.
 */

public class LocationReq {

    private Location[] index;
    private String fileName;
    public Location[] getIndex() {
        return index;
    }
    public void setIndex(Location[] index) {
        this.index = index;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    @Override
    public String toString() {
        return "LocationReq [index=" + Arrays.toString(index) + ", fileName=" + fileName + "]";
    }

}

