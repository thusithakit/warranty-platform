package com.thusithakit.purchaseservice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePurchaseRequestDTO(

    @NotBlank
    @Size(max = 255)
    String itemName,

    @Size(max = 150)
    String brand,

    @Size(max = 150)
    String model,

    @DecimalMin("0.00")
    BigDecimal purchasePrice,

    @Pattern(regexp = "^[A-Z]{3}$")
    String currency,

    @Size(max = 255)
    String sellerName,

    @NotNull
    @PastOrPresent
    LocalDate purchaseDate,

    String notes
) {}
