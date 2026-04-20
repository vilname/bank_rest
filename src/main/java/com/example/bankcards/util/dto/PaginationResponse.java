package com.example.bankcards.util.dto;

import lombok.Data;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Data
public class PaginationResponse<T> {

    private List<T> content;
    private PaginationItem pagination;

    public PaginationResponse(List<T> content, Pageable pageable, int totalPage) {
        this.content = content;
        this.pagination = new PaginationItem(pageable.getPageNumber(), pageable.getPageSize(), totalPage);
    }
}
