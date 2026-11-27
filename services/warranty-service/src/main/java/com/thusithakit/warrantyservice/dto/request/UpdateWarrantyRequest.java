package com.thusithakit.warrantyservice.dto.request;

import com.thusithakit.warrantyservice.entity.WarrantyType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateWarrantyRequest(

    @Size(max = 255)
    String warrantyProvider,

    @Size(max = 150)
    String warrantyReference,

    @NotNull
    WarrantyType warrantyType,

    LocalDate startDate,

    LocalDate endDate,

    @Size(max = 5000)
    String coverageDescription

) {
  @AssertTrue(message = "End date must not be before start date")
  public boolean isDateRangeValid() {
    return startDate == null
        || endDate == null
        || !endDate.isBefore(startDate);
  }
}
