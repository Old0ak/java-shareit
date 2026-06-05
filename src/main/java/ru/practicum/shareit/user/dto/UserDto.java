package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class UserDto {

    Long id;

    @Email
    String email;

    String name;
}
