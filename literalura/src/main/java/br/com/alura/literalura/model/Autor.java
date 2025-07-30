package br.com.alura.literalura.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "autores")
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nome;
    private Integer anoNascimento;
    private Integer anoFalecimento;

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Livro> livros = new ArrayList<>();
    public Autor() {}

    public Autor(String nome, Integer anoNascimento, Integer anoFalecimento) {
        this.nome = nome;
        this.anoNascimento = anoNascimento;
        this.anoFalecimento = anoFalecimento;
    }

    public void adicionarLivro(Livro livro) {
        livros.add(livro);
        livro.setAutor(this);
    }


    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Integer getAnoNascimento() { return anoNascimento; }
    public Integer getAnoFalecimento() { return anoFalecimento; }
    public List<Livro> getLivros() { return livros; }

    public void setNome(String nome) { this.nome = nome; }
    public void setAnoNascimento(Integer anoNascimento) { this.anoNascimento = anoNascimento; }
    public void setAnoFalecimento(Integer anoFalecimento) { this.anoFalecimento = anoFalecimento; }

    @Override
    public String toString() {
        return "Autor: " + nome +
                " | Nasc.: " + (anoNascimento != null ? anoNascimento : "desconhecido") +
                " | Falec.: " + (anoFalecimento != null ? anoFalecimento : "desconhecido");
    }
}
