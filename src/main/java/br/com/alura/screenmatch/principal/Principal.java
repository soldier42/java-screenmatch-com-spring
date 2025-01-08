package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private String menu = """
            1) Buscar séries
            2) Buscar episódios
            3) Listar séries buscadas
            4) Buscar série por título
            5) Buscar séries por ator
            6) Top 5 séries
            7) Buscar séries por categoria
            8) Filtrar séries
            
            0) Sair
            """;

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";

    private List<DadosSerie> listaDeSeries = new ArrayList<>();

    @Autowired
    private SerieRepository repository;

    private List<Serie> series = new ArrayList<>();

    public Principal(SerieRepository repository) {
        this.repository = repository;
    }

    public void exibeMenu() {
        var entradaUsuario = -1;
        while (entradaUsuario != 0) {
            System.out.println(menu);
            entradaUsuario = leitura.nextInt();
            leitura.nextLine(); // Limpando buffer
            switch (entradaUsuario) {
                case 0:
                    System.out.println("Saindo ...");
                    break;
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodiosPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    buscarSeriePorMaxTempEAval();
                    break;
                default:
                    System.out.println(entradaUsuario);
                    System.out.println("Opção Inválida!\n");
                    break;
            }
        }
    }

    private void listarSeriesBuscadas() {
        series = repository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getTitulo))
                .forEach(System.out::println);
    }

    public void buscarEpisodiosPorSerie() {
        listarSeriesBuscadas();
        System.out.println("Escolha uma série para busca");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repository.findByTituloContainingIgnoreCase(nomeSerie);

        List<DadosTemporada> temporadas = new ArrayList<>();
        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            temporadas.addAll(getDadosTemporada(getDadosSerie(serieEncontrada.getTitulo())));
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setListaDeEpisodios(episodios);
            repository.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    public void buscarSerieWeb() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var dadosDaSerie = getDadosSerie(nomeSerie);
        repository.save(new Serie(dadosDaSerie));
        System.out.println(dadosDaSerie);
    }

    public DadosSerie getDadosSerie(String nomeSerie) {
        System.out.println(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        var dados = conversor.obterDados(json, DadosSerie.class);

        return dados;
    }

    public List<DadosTemporada> getDadosTemporada(DadosSerie dados) {
        List<DadosTemporada> temporadas = new ArrayList<>();
        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            var json = consumo.obterDados(ENDERECO + dados.titulo().replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        return temporadas;
    }

    public void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serieBuscada = repository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBuscada.isPresent()) {
            System.out.println("Dados da série: " + serieBuscada.get());
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    public void buscarSeriesPorAtor() {
        System.out.println("Digite o nome do ator: ");
        var nomeDoAtor = leitura.nextLine();

        System.out.println("Avaliações a partir de que valor?");
        var avaliacao = leitura.nextDouble();

        List<Serie> listaDeSeries = repository.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeDoAtor, avaliacao);
        listaDeSeries.forEach(s ->
                System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao())
        );
    }

    private void buscarTop5Series() {
        List<Serie> serieTop = repository.findTop5ByOrderByAvaliacaoDesc();
        serieTop.forEach(s ->
                System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao())
        );
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Qual categoria/gênero deseja buscar?");
        var nomeCategoria = leitura.nextLine();
        Categoria categoria = Categoria.fromPortuguese(nomeCategoria);
        List<Serie> listaDeSeries = repository.findByGenero(categoria);

        listaDeSeries.forEach(System.out::println);
    }

    private void buscarSeriePorMaxTempEAval() {
        System.out.println("Digite a quantidade máxima de temporadas das séries que você deseja buscar: ");
        var quantDeTemporadas = leitura.nextInt();

        System.out.println("Digite a avaliação máxima das séries a serem buscadas: ");
        var avaliacao = leitura.nextDouble();


        List<Serie> series = repository.filtrarSerie(quantDeTemporadas, avaliacao);
        series.forEach(System.out::println);
    }
}
//        for(int i = 0; i < dados.totalTemporadas(); i++){
//            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
//            for(int j = 0; j< episodiosTemporada.size(); j++){
//                System.out.println(episodiosTemporada.get(j).titulo());
//            }
//        }

//        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

//        List<String> nomes = Arrays.asList("Jacque", "Iasmin", "Paulo", "Rodrigo", "Nico");
//
//        nomes.stream().sorted().limit(3).filter(n -> n.startsWith("N")).map(n -> n.toUpperCase())
//                .forEach(System.out::println);


//        System.out.println("\nTop 5 episódios");
//        dadosEpisodios.stream()
//                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
//                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
//                .limit(5)
//                .forEach(System.out::println);


//        System.out.println("A partir de que ano você deseja ver os episódios? ");
//        var ano = leitura.nextInt();
//        leitura.nextLine();
//
//        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//
//        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//
//        episodios.stream()
//                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
//                .forEach(e -> System.out.println(
//                        "Temporada: " + e.getTemporada() +
//                                ", Episódio: " + e.getTitulo() +
//                                ", Data lançamento: " + e.getDataLancamento().format(formatador)
//                ));

//        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
//                .filter(e -> e.getAvaliacao() > 0.0)
//                .collect(Collectors.groupingBy(Episodio::getTemporada, Collectors.averagingDouble(Episodio::getAvaliacao)));
//
//        System.out.println(avaliacoesPorTemporada);
//
//        DoubleSummaryStatistics est = episodios.stream()
//                .filter(e -> e.getAvaliacao() > 0.0)
//                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
//
//        System.out.println(est);