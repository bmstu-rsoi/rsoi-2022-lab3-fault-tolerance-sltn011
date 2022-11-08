package ru.RSOI.Gateway.Model;

import java.util.List;

public class MCarsPage {

    public int page;
    public int pageSize;
    public int totalElements;
    public List<MCarInfo> items;

    public MCarsPage(int page, int pageSize, int totalElements, List<MCarInfo> items) {
        this.page = page;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.items = items;
    }
}
