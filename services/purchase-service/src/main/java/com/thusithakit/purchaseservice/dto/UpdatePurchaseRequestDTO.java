package com.thusithakit.purchaseservice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdatePurchaseRequestDTO(

    @NotBlank
    @Size(max = 255)
    String itemName,

    String brand,

    String model,

    @DecimalMin("0.00")
    BigDecimal purchasePrice,

    @Pattern(regexp = "^[A-Z]{3}$")
    String currency,

    String sellerName,

    @NotNull
    @PastOrPresent
    LocalDate purchaseDate,

    String notes
) {}
