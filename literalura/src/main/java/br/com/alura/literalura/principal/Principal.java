package br.com.alura.literalura.principal;

import br.com.alura.literalura.dto.LivroDTO;
import br.com.alura.literalura.dto.ResultadoBuscaDTO;
import br.com.alura.literalura.dto.DadosAutor;
import br.com.alura.literalura.model.Autor;
import br.com.alura.literalura.model.Livro;
import br.com.alura.literalura.repository.AutorRepository;
import br.com.alura.literalura.repository.LivroRepository;
import br.com.alura.literalura.service.ConsumoApi;
import br.com.alura.literalura.service.ConverteDados;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

@Service
public class Principal implements CommandLineRunner {

    private final Scanner leitura = new Scanner(System.in);
    private final ConsumoApi consumo = new ConsumoApi();
    private final ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://gutendex.com/books/?search=";

    private final LivroRepository livroRepository;
    private final AutorRepository autorRepository;

    public Principal(LivroRepository livroRepository, AutorRepository autorRepository) {
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
    }

    @Override
    public void run(String... args) {
        exibeMenu();
    }

    private void exibeMenu() {
        int opcao = -1;
        while (opcao != 0) {
            System.out.println("""
                    \n=== Menu Literalura ===
                    1 - Buscar livro por título
                    2 - Listar livros registrados
                    3 - Listar autores registrados
                    4 - Listar autores vivos em determinado ano
                    5 - Listar livros por idioma
                    0 - Sair
                    """);

            try {
                opcao = Integer.parseInt(leitura.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número.");
                continue;
            }

            switch (opcao) {
                case 1 -> buscarLivroPeloTitulo();
                case 2 -> listarLivrosRegistrados();
                case 3 -> listarAutoresRegistrados();
                case 4 -> listarAutoresVivosEmDeterminadoAno();
                case 5 -> listarLivrosPorIdioma();
                case 0 -> System.out.println("Encerrando a aplicação.");
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private void buscarLivroPeloTitulo() {
        System.out.println("Digite o título do livro para buscar:");
        String titulo = leitura.nextLine();

        String url = ENDERECO + titulo.replace(" ", "+");
        String json = consumo.obterDados(url);
        ResultadoBuscaDTO resultado = conversor.obterDados(json, ResultadoBuscaDTO.class);

        if (resultado.results().isEmpty()) {
            System.out.println("Nenhum livro encontrado com esse título.");
            return;
        }

        LivroDTO dto = resultado.results().get(0);

        DadosAutor dadosAutor = dto.authors().isEmpty()
                ? new DadosAutor("Autor desconhecido", null, null)
                : dto.authors().get(0);

        Autor autor = autorRepository.findByNomeContainingIgnoreCase(dadosAutor.nome())
                .orElseGet(() -> {
                    Autor novoAutor = new Autor(dadosAutor.nome(), dadosAutor.anoNascimento(), dadosAutor.anoFalecimento());
                    return autorRepository.save(novoAutor);
                });

        Livro livro = new Livro(dto, autor);
        livroRepository.save(livro);

        System.out.println("\nLivro salvo com sucesso:");
        System.out.println(livro);
    }

    private void listarLivrosRegistrados() {
        List<Livro> livros = livroRepository.findAll();

        if (livros.isEmpty()) {
            System.out.println("Nenhum livro registrado.");
            return;
        }

        livros.stream()
                .sorted((l1, l2) -> l1.getTitulo().compareToIgnoreCase(l2.getTitulo()))
                .forEach(livro -> {
                    System.out.println("\n" + livro);
                });
    }

    public void listarAutoresRegistrados() {
        List<Autor> autores = autorRepository.findAll();

        if (autores.isEmpty()) {
            System.out.println("Nenhum autor registrado.");
            return;
        }

        System.out.println("\nAutores registrados:\n");

        autores.forEach(autor -> {
            System.out.println("Autor: " + autor.getNome());
            System.out.println("Ano de nascimento: " +
                    (autor.getAnoNascimento() != null ? autor.getAnoNascimento() : "desconhecido"));
            System.out.println("Ano de falecimento: " +
                    (autor.getAnoFalecimento() != null ? autor.getAnoFalecimento() : "desconhecido"));

            String titulos = autor.getLivros().stream()
                    .map(Livro::getTitulo)
                    .collect(Collectors.joining(", "));

            System.out.println("Livros: " + (titulos.isEmpty() ? "Nenhum" : titulos));
            System.out.println("-------------------------------------");
        });
    }

    private void listarAutoresVivosEmDeterminadoAno() {
        System.out.print("Digite o ano para verificar autores vivos nesse período: ");
        int ano = Integer.parseInt(leitura.nextLine());

        List<Autor> autores = autorRepository.findAll().stream()
                .filter(a -> a.getAnoNascimento() != null && a.getAnoNascimento() <= ano)
                .filter(a -> a.getAnoFalecimento() == null || a.getAnoFalecimento() >= ano)
                .toList();

        if (autores.isEmpty()) {
            System.out.println("Nenhum autor encontrado vivo nesse ano.");
            return;
        }

        System.out.println("\n=== Autores vivos no ano de " + ano + " ===\n");

        for (Autor autor : autores) {
            System.out.println("Autor: " + autor.getNome());
            System.out.println("Nascimento: " + (autor.getAnoNascimento() != null ? autor.getAnoNascimento() : "Desconhecido"));
            System.out.println("Falecimento: " + (autor.getAnoFalecimento() != null ? autor.getAnoFalecimento() : "Ainda vivo"));

            String obras = autor.getLivros().stream()
                    .map(Livro::getTitulo)
                    .collect(Collectors.joining(", "));

            System.out.println("Obras/Livros: " + (!obras.isBlank() ? obras : "Nenhum registrado"));
            System.out.println("-".repeat(40));
        }
    }

    private void listarLivrosPorIdioma() {
        System.out.println("""
        Escolha um idioma para buscar os livros:
        ----------------------------------------
        pt - Português
        en - Inglês
        es - Espanhol
        fr - Francês
        de - Alemão
        """);

        System.out.print("Digite o código do idioma: ");
        String idioma = leitura.nextLine().trim();

        List<Livro> livros = livroRepository.findByIdiomaIgnoreCase(idioma);

        if (livros.isEmpty()) {
            System.out.println("Nenhum livro encontrado no idioma informado.");
        } else {
            livros.forEach(l -> System.out.println("\n" + l));
            System.out.println("\nTotal de livros encontrados: " + livros.size());
        }
    }

}
