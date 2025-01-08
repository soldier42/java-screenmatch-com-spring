package br.com.alura.screenmatch.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

@Entity
@Table(name = "series")
public class Serie {

    // Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String titulo;
    private Integer totalTemporadas;
    private Double avaliacao;
    private String atores;

    @Enumerated(EnumType.STRING)
    private Categoria genero;

    private String sinopse;
    private String poster;

    @OneToMany(mappedBy = "serie", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Episodio> listaDeEpisodios = new ArrayList<>();

    // Construtores
    public Serie() {}

    public Serie(DadosSerie dadosSerie) {
        this.titulo = dadosSerie.titulo();
        this.totalTemporadas = dadosSerie.totalTemporadas();
        this.atores = dadosSerie.atores();
        this.genero = Categoria.fromString(dadosSerie.genero().split(",")[0].trim());
        this.avaliacao = OptionalDouble.of(Double.parseDouble(dadosSerie.avaliacao())).orElse(0);
        this.sinopse = dadosSerie.sinopse();

        // NÃO EXISTE MAIS PLANO GRATUITO DO OPEN AI
//        try {
//            this.sinopse = ConsultaChatGPT.obterTraducao(dadosSerie.sinopse());
//        } catch (Exception e) {
//            this.sinopse = dadosSerie.sinopse();
//        }
        this.poster = dadosSerie.poster();
    }


    // Métodos
    @Override
    public String toString() {
        return
                "genero=" + genero +
                ", titulo='" + titulo + '\'' +
                ", totalTemporadas=" + totalTemporadas +
                ", avaliacao=" + avaliacao +
                ", atores='" + atores + '\'' +
                ", sinopse='" + sinopse + '\'' +
                ", poster='" + poster + '\'' +
                ", episodios='" + listaDeEpisodios + '\'';
    }

    public String getTitulo() {
        return titulo;
    }

    public Integer getTotalTemporadas() {
        return totalTemporadas;
    }

    public Double getAvaliacao() {
        return avaliacao;
    }

    public String getAtores() {
        return atores;
    }

    public Categoria getGenero() {
        return genero;
    }

    public String getSinopse() {
        return sinopse;
    }

    public String getPoster() {
        return poster;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setTotalTemporadas(Integer totalTemporadas) {
        this.totalTemporadas = totalTemporadas;
    }

    public void setAvaliacao(Double avaliacao) {
        this.avaliacao = avaliacao;
    }

    public void setAtores(String atores) {
        this.atores = atores;
    }

    public void setGenero(Categoria genero) {
        this.genero = genero;
    }

    public void setSinopse(String sinopse) {
        this.sinopse = sinopse;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public void setListaDeEpisodios(List<Episodio> listaDeEpisodios) {
        listaDeEpisodios.forEach(e -> e.setSerie(this));
        this.listaDeEpisodios = listaDeEpisodios;
    }
}
