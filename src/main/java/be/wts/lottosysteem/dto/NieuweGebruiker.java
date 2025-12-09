package be.wts.lottosysteem.dto;

import jakarta.validation.constraints.NotBlank;

public record NieuweGebruiker(@NotBlank(message = "Gebruikersnaam mag niet leeg zijn.") String gebruikersnaam,
                              @NotBlank(message = "Wachtwoord mag niet leeg zijn.") String wachtwoord,
                              String rol) {}
