package com.svartberg.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
public class Price {

    private long id;
    private String productCode;
    private int number;
    private int depart;
    private Date begin;
    private Date end;
    private long value;

    public Price(Price old) {
        this.id=old.id;
        this.productCode= old.productCode;
        this.number=old.number;
        this.depart=old.depart;
        this.begin=new Date(old.begin.getTime());
        this.end=new Date(old.end.getTime());
        this.value=old.value;
    }
}
