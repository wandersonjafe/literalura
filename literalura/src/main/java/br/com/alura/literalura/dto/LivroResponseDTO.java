package br.com.alura.literalura.dto;

public record LivroResponseDTO(
        String titulo,
        String idioma,
        Integer downloads
) {}
