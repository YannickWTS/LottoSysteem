package be.wts.lottosysteem.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateBetaald(@NotNull Boolean betaald) {}
