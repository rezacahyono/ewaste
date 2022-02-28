package com.example.myewaste.model.item;


public class UnitItem {
    private String no_unit_item;
    private String name;

    public UnitItem(String no_unit_item, String name) {
        this.no_unit_item = no_unit_item;
        this.name = name;
    }

    public UnitItem(){}

    public String getNo_unit_item() {
        return no_unit_item;
    }

    public void setNo_unit_item(String no_unit_item) {
        this.no_unit_item = no_unit_item;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


