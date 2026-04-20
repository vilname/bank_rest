package com.example.bankcards.util.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Getter
@Setter
public class PaginationRequest {

    @Min(value = 0, message = "Page must be greater than or equal to 0")
    private int page = 1;

    @Min(value = 1, message = "Limit must be greater than 0")
    private int limit = 20;

    private int offset;

    public PaginationRequest() {
        this.offset = (this.page - 1) * this.limit;
    }

    public Pageable toPageable() {
        return PageRequest.of(page - 1, limit);
    }
}
