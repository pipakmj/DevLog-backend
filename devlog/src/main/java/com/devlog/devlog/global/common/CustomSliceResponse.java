package com.devlog.devlog.global.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomSliceResponse<T>{
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private boolean hasNext;

    public CustomSliceResponse(Slice<T> slice){
        this.content = slice.getContent();
        this.pageNumber = slice.getNumber();
        this.pageSize = slice.getSize();
        this.hasNext = slice.hasNext();
    }
}
