package no.lavankumar.sentinelpay.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotBlank @Size(max = 16) String decision,
        @NotBlank @Size(max = 500) String note) {
}
