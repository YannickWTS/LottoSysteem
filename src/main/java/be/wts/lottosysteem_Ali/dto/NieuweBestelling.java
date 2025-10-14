package be.wts.lottosysteem_Ali.dto;

import jakarta.validation.constraints.NotBlank;

public record NieuweBestelling(long klantId,
                               @NotBlank String spelType,
                               @NotBlank String maand,
                               Boolean betaald
) {}
