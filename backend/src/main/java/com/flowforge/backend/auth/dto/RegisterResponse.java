package com.flowforge.backend.auth.dto;

import java.util.UUID;

public record RegisterResponse(

    UUID id,

    String email,

    String firstName,

    String lastName

) {
}
