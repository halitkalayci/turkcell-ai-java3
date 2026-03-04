package com.turkcell.productservice.dto.response;

import java.util.List;

public class ProductPageResponse {

    private List<ProductResponse> items;
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;

    public List<ProductResponse> getItems() { return items; }
    public void setItems(List<ProductResponse> items) { this.items = items; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalItems() { return totalItems; }
    public void setTotalItems(long totalItems) { this.totalItems = totalItems; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
