package com.renthouses.enviroment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.joda.time.DateTime;

@Data
@AllArgsConstructor
@Builder
public class FreeDateDto {

    private DateTime startDate;
    private DateTime endDate;
    private String message;
    private Integer colorId;
    @NonNull private boolean isFree;

}
