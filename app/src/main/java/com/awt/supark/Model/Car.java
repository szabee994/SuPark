package com.awt.supark.Model;

/**
 * Created by Róbert on 2015.11.05..
 */
public class Car {
    String name,licens;
    int sqlid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLicens() {
        return licens;
    }

    public void setLicens(String licens) {
        this.licens = licens;
    }

    public int getSqlid() {return sqlid;}

    public void setSqlid(int sqlid) {this.sqlid = sqlid;}
}
