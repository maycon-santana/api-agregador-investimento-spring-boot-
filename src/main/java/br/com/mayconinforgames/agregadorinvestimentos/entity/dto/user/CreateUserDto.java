package br.com.mayconinforgames.agregadorinvestimentos.entity.dto.user;

public record CreateUserDto(
        String username,
        String email,
        String password
) {
}
