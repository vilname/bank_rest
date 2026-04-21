package com.example.bankcards.util.dto;

import lombok.Data;

import java.util.List;

@Data
public class PaginationResponse<T> {

    private List<T> content;
    private PaginationItem pagination;

    public PaginationResponse(List<T> content, PaginationRequest pagination, int total) {
        this.content = content;
        this.pagination = new PaginationItem(pagination.getPage(), pagination.getLimit(), total);
    }
}
