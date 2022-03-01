package com.example.myewaste.model.utils;

import com.example.myewaste.model.item.ItemType;

import java.util.ArrayList;

public class ListItemType {
    private String nameItemMaster;
    private ArrayList<ItemType> itemTypes;

    public ListItemType(String nameItemMaster, ArrayList<ItemType> itemTypes) {
        this.nameItemMaster = nameItemMaster;
        this.itemTypes = itemTypes;
    }

    public ListItemType() {
    }

    public String getNameItemMaster() {
        return nameItemMaster;
    }

    public void setNameItemMaster(String nameItemMaster) {
        this.nameItemMaster = nameItemMaster;
    }

    public ArrayList<ItemType> getItemTypes() {
        return itemTypes;
    }

    public void setItemTypes(ArrayList<ItemType> itemTypes) {
        this.itemTypes = itemTypes;
    }
}
