package be.wts.lottosysteem_Ali.dto;

import jakarta.validation.constraints.NotBlank;

public record NieuweGebruiker(@NotBlank(message = "Gebruikersnaam mag niet leeg zijn.") String gebruikersnaam,
                              @NotBlank(message = "Wachtwoord mag niet leeg zijn.") String wachtwoord,
                              @NotBlank(message = "Rol mag niet leeg zijn.") String rol) {}
