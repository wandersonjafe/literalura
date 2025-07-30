package br.com.alura.literalura.model;

import br.com.alura.literalura.dto.LivroDTO;
import jakarta.persistence.*;

@Entity
@Table(name = "livros")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String idioma;
    private Integer downloads;

    @ManyToOne(fetch = FetchType.EAGER)
    private Autor autor;

    public Livro() {}

    public Livro(LivroDTO dto, Autor autor) {
        this.titulo = dto.title();
        this.idioma = dto.languages().isEmpty() ? "desconhecido" : dto.languages().get(0);
        this.downloads = dto.download_count();
        this.autor = autor;
    }

    // Getters e Setters

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getIdioma() { return idioma; }
    public Integer getDownloads() { return downloads; }
    public Autor getAutor() { return autor; }

    public void setAutor(Autor autor) { this.autor = autor; }

    @Override
    public String toString() {
        return "Título: " + titulo +
                "\nAutor: " + (autor != null ? autor.getNome() : "desconhecido") +
                "\nIdioma: " + idioma +
                "\nDownloads: " + downloads;
    }
}
