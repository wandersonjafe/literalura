package br.com.alura.literalura.service;

import br.com.alura.literalura.dto.DadosAutor;
import br.com.alura.literalura.dto.LivroDTO;
import br.com.alura.literalura.dto.LivroResponseDTO;
import br.com.alura.literalura.dto.ResultadoBuscaDTO;
import br.com.alura.literalura.model.Autor;
import br.com.alura.literalura.model.Livro;
import br.com.alura.literalura.repository.AutorRepository;
import br.com.alura.literalura.repository.LivroRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LivroService {

    private final LivroRepository livroRepository;
    private final AutorRepository autorRepository;
    private final ConverteDados conversor;
    private final ConsumoApi consumoApi;

    private final String ENDERECO = "https://gutendex.com/books/?search=";

    // Injeção de dependência via construtor
    public LivroService(LivroRepository livroRepository,
                        AutorRepository autorRepository,
                        ConverteDados conversor,
                        ConsumoApi consumoApi) {
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
        this.conversor = conversor;
        this.consumoApi = consumoApi;
    }

    // Busca livro pelo título, salva no banco e retorna como DTO
    public LivroResponseDTO buscarESalvarPorTitulo(String titulo) {
        String url = ENDERECO + titulo.replace(" ", "+");
        String json = consumoApi.obterDados(url);
        ResultadoBuscaDTO resultado = conversor.obterDados(json, ResultadoBuscaDTO.class);

        if (resultado.results().isEmpty()) {
            return null;
        }

        LivroDTO dto = resultado.results().get(0);
        DadosAutor dadosAutor = dto.authors().isEmpty()
                ? new DadosAutor("Autor desconhecido", null, null)
                : dto.authors().get(0);

        Autor autor = autorRepository.findByNomeContainingIgnoreCase(dadosAutor.nome())
                .orElseGet(() -> autorRepository.save(
                        new Autor(dadosAutor.nome(), dadosAutor.anoNascimento(), dadosAutor.anoFalecimento()))
                );

        Livro livro = new Livro(dto, autor);
        livroRepository.save(livro);

        return new LivroResponseDTO(livro.getTitulo(), livro.getIdioma(), livro.getDownloads());
    }

    public List<LivroResponseDTO> listarLivros() {
        return livroRepository.findAll().stream()
                .map(l -> new LivroResponseDTO(l.getTitulo(), l.getIdioma(), l.getDownloads()))
                .toList();
    }

    // Lista livros filtrados por idioma
    public List<LivroResponseDTO> listarPorIdioma(String idioma) {
        return converteDados(livroRepository.findByIdiomaIgnoreCase(idioma));
    }

    public long contarPorIdioma(String idioma) {
        return livroRepository.countByIdiomaIgnoreCase(idioma);
    }

    // Conversão privada de entidade para DTO
    private List<LivroResponseDTO> converteDados(List<Livro> livros) {
        return livros.stream()
                .map(l -> new LivroResponseDTO(l.getTitulo(), l.getIdioma(), l.getDownloads()))
                .collect(Collectors.toList());
    }

}
