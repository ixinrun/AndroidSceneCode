package com.bigrun.assortdemo.model;

/**
 * Created by BigRun on 2015/12/14.
 */
public class CarImagesBean {

    /**
     * DDCode : 00010000
     * DDValue : 阿尔法.罗密欧
     * initial : A
     */

    private String DDCode;
    private String DDValue;
    private String initial;

    public void setDDCode(String DDCode) {
        this.DDCode = DDCode;
    }

    public void setDDValue(String DDValue) {
        this.DDValue = DDValue;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }

    public String getDDCode() {
        return DDCode;
    }

    public String getDDValue() {
        return DDValue;
    }

    public String getInitial() {
        return initial;
    }
}
