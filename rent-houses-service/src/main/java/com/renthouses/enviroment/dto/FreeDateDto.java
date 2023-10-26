package com.renthouses.enviroment.dto;

import com.google.api.client.util.DateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
@Builder
public class FreeDateDto {

    private DateTime freeDate;
    private String message;
    private Integer colorId;
    @NonNull private boolean isFree;

}
