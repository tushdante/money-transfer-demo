package io.temporal.samples.moneytransfer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositResponse {

    @JsonProperty("chargeId")
    private String depositId;
}
