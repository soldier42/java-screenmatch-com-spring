package br.com.alura.screenmatch.repository;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {
    Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);
    List<Serie> findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(String nomeDoAtor, Double avaliacao);
    List<Serie> findTop5ByOrderByAvaliacaoDesc();

    List<Serie> findByGenero(Categoria categoria);

    // JPQL
    @Query("SELECT s FROM Serie s WHERE s.totalTemporadas <= :totalTemporadas AND s.avaliacao >= :avaliacao")
    List<Serie> filtrarSerie(Integer totalTemporadas, double avaliacao);

    @Query("SELECT e FROM Serie s JOIN s.listaDeEpisodios e WHERE e.titulo iLIKE %:trechoEpisodio%")
    List<Episodio> filtrarEpisodioPorNome(String trechoEpisodio);

    @Query("SELECT e FROM Serie s JOIN s.listaDeEpisodios e WHERE s = :serie ORDER BY e.avaliacao DESC LIMIT 5")
    List<Episodio> topEpisodiosPorSerie(Serie serie);

    @Query("SELECT e FROM Serie s JOIN s.listaDeEpisodios e WHERE s = :serie AND YEAR(e.dataLancamento) >= :ano")
    List<Episodio> filtrarEpisodiosDepoisDaData(Serie serie, Integer ano);
}
