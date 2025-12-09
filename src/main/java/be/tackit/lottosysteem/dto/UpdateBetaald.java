package be.tackit.lottosysteem.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateBetaald(@NotNull Boolean betaald) {}
