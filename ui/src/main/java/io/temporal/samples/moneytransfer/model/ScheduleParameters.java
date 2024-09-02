package io.temporal.samples.moneytransfer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"scenario"})
public class ScheduleParameters {
    private int amount;
    private int interval;
    private int count;
}
