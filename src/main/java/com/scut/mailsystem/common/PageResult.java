package com.scut.mailsystem.common;

import java.util.List;

public class PageResult<T> {

    private Integer page;
    private Integer size;
    private Long total;
    private Integer totalPages;
    private List<T> records;

    public PageResult() {
    }

    public PageResult(Integer page, Integer size, Long total, Integer totalPages, List<T> records) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = totalPages;
        this.records = records;
    }

    public static <T> PageResult<T> of(Integer page, Integer size, Long total, List<T> records) {
        long safeTotal = total == null ? 0L : total;
        int totalPages = 0;
        if (safeTotal > 0 && size != null && size > 0) {
            totalPages = (int) ((safeTotal + size - 1) / size);
        }
        return new PageResult<>(page, size, safeTotal, totalPages, records);
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
