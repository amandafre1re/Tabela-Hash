import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Supplier;


public class Main {


    private static final int RODADAS_AQUECIMENTO = 20;
    private static final int RODADAS_MEDIDAS = 31;

    public static void main(String[] args) {
        String arquivo = args.length > 0 ? args[0] : "female_names.txt";

        // ---------- 1. Leitura do arquivo ----------
        List<String> nomes;
        try {
            nomes = lerNomes(arquivo);
        } catch (IOException e) {
            System.out.println("Nao consegui ler o arquivo: " + arquivo);
            System.out.println("Coloque o female_names.txt na mesma pasta do programa.");
            return;
        }


        // Nomes que existem: a lista sem repetidos.
        List<String> presentes = new ArrayList<>(new LinkedHashSet<>(nomes));

        // Nomes que nao existem: nenhum nome do arquivo contem o caractere "#".
        List<String> ausentes = new ArrayList<>();
        for (String nome : presentes) {
            ausentes.add("#" + nome);
        }

        //  Testes
        List<Supplier<TabelaHash>> fabricas = new ArrayList<>();
        fabricas.add(TabelaHashPorLetras::new);
        fabricas.add(TabelaHashPolinomial::new);

        int f = fabricas.size();
        long[][] temposInsercao = new long[f][RODADAS_MEDIDAS];
        long[][] temposPresentes = new long[f][RODADAS_MEDIDAS];
        long[][] temposAusentes = new long[f][RODADAS_MEDIDAS];
        TabelaHash[] tabelas = new TabelaHash[f];
        long[] comparacoesInsercao = new long[f];
        long[] comparacoesPresentes = new long[f];
        long[] comparacoesAusentes = new long[f];
        int[] achadosPresentes = new int[f];
        int[] achadosAusentes = new int[f];

        for (int rodada = 0; rodada < RODADAS_AQUECIMENTO + RODADAS_MEDIDAS; rodada++) {
            for (int passo = 0; passo < f; passo++) {
                // Alterna quem vai primeiro, para nenhuma tabela levar vantagem pela ordem.
                int t = (rodada + passo) % f;
                TabelaHash tabela = fabricas.get(t).get();

                long inicio = System.nanoTime();
                for (String nome : nomes) tabela.inserir(nome);
                long fimInsercao = System.nanoTime();

                long c1 = tabela.getComparacoes();
                int achou1 = contarEncontrados(tabela, presentes);
                long fimPresentes = System.nanoTime();

                long c2 = tabela.getComparacoes();
                int achou2 = contarEncontrados(tabela, ausentes);
                long fimAusentes = System.nanoTime();

                if (rodada >= RODADAS_AQUECIMENTO) {
                    int r = rodada - RODADAS_AQUECIMENTO;
                    temposInsercao[t][r] = fimInsercao - inicio;
                    temposPresentes[t][r] = fimPresentes - fimInsercao;
                    temposAusentes[t][r] = fimAusentes - fimPresentes;
                }

                // Os contadores dao o mesmo resultado em toda rodada; guardamos o da ultima.
                tabelas[t] = tabela;
                comparacoesInsercao[t] = c1;
                comparacoesPresentes[t] = c2 - c1;
                comparacoesAusentes[t] = tabela.getComparacoes() - c2;
                achadosPresentes[t] = achou1;
                achadosAusentes[t] = achou2;
            }
        }

        //  Relatorio final
        linha();
        System.out.println("RELATORIO COMPARATIVO - TABELAS HASH");
        linha();
        System.out.println("Arquivo: " + arquivo);
        System.out.println("Nomes lidos: " + nomes.size());
        System.out.println("Nomes repetidos ignorados: " + (nomes.size() - presentes.size()));
        System.out.println("Capacidade da tabela: " + TabelaHash.CAPACIDADE + " posicoes");
        System.out.println("Tratamento de colisoes: encadeamento exterior");
        System.out.println("Tabela 1: " + tabelas[0].getNome() + " = (letras - 1) % 32");
        System.out.println("Tabela 2: " + tabelas[1].getNome() + " = polinomial base 31 % 32");

        titulo("1. COLISOES");
        cabecalho();
        linhaDados("Chaves inseridas", tabelas[0].getQuantidade(), tabelas[1].getQuantidade());
        linhaDados("Colisoes (insercoes em posicao ocupada)",
                tabelas[0].getColisoes(), tabelas[1].getColisoes());
        linhaDados("Comparacoes de nomes ao inserir",
                comparacoesInsercao[0], comparacoesInsercao[1]);

        titulo("2. TEMPOS EM MILISSEGUNDOS (mediana de " + RODADAS_MEDIDAS + " rodadas)");
        cabecalho();
        double[] insercao = {mediana(temposInsercao[0]), mediana(temposInsercao[1])};
        double[] buscaPresentes = {mediana(temposPresentes[0]), mediana(temposPresentes[1])};
        double[] buscaAusentes = {mediana(temposAusentes[0]), mediana(temposAusentes[1])};
        linhaTempo("Insercao de todas as chaves", insercao[0], insercao[1]);
        linhaTempo("Busca de " + presentes.size() + " nomes presentes",
                buscaPresentes[0], buscaPresentes[1]);
        linhaTempo("Busca de " + ausentes.size() + " nomes ausentes",
                buscaAusentes[0], buscaAusentes[1]);
        linhaTempo("Busca total", buscaPresentes[0] + buscaAusentes[0],
                buscaPresentes[1] + buscaAusentes[1]);

        titulo("3. BUSCAS: RESULTADO E COMPARACOES");
        cabecalho();
        linhaDados("Presentes encontrados", achadosPresentes[0], achadosPresentes[1]);
        linhaDados("Ausentes encontrados (deve ser 0)", achadosAusentes[0], achadosAusentes[1]);
        linhaDecimal("Comparacoes por busca (presentes)",
                (double) comparacoesPresentes[0] / presentes.size(),
                (double) comparacoesPresentes[1] / presentes.size());
        linhaDecimal("Comparacoes por busca (ausentes)",
                (double) comparacoesAusentes[0] / ausentes.size(),
                (double) comparacoesAusentes[1] / ausentes.size());

        titulo("4. RESUMO DA DISTRIBUICAO");
        cabecalho();
        linhaDados("Posicoes ocupadas (de 32)",
                tabelas[0].getPosicoesOcupadas(), tabelas[1].getPosicoesOcupadas());
        linhaDados("Menor lista", tabelas[0].getMenorLista(), tabelas[1].getMenorLista());
        linhaDados("Maior lista", tabelas[0].getMaiorLista(), tabelas[1].getMaiorLista());
        linhaDecimal("Media de chaves por posicao", tabelas[0].getMedia(), tabelas[1].getMedia());
        linhaDecimal("Desvio padrao", tabelas[0].getDesvioPadrao(), tabelas[1].getDesvioPadrao());

        titulo("5. DISTRIBUICAO E COLISOES POR POSICAO (clusterizacao)");
        System.out.printf("%-8s | %-21s | %-21s%n", "", tabelas[0].getNome(), tabelas[1].getNome());
        System.out.printf("%-8s | %10s %10s | %10s %10s%n",
                "Posicao", "Chaves", "Colisoes", "Chaves", "Colisoes");
        for (int i = 0; i < TabelaHash.CAPACIDADE; i++) {
            System.out.printf("%-8d | %10d %10d | %10d %10d%n", i,
                    tabelas[0].getChavesNaPosicao(i), tabelas[0].getColisoesNaPosicao(i),
                    tabelas[1].getChavesNaPosicao(i), tabelas[1].getColisoesNaPosicao(i));
        }
        linha();
    }


    private static List<String> lerNomes(String arquivo) throws IOException {
        List<String> nomes = new ArrayList<>();
        try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                linha = linha.trim();
                if (!linha.isEmpty()) nomes.add(linha);
            }
        }
        return nomes;
    }

    private static int contarEncontrados(TabelaHash tabela, List<String> nomes) {
        int encontrados = 0;
        for (String nome : nomes) {
            if (tabela.buscar(nome)) encontrados++;
        }
        return encontrados;
    }

    // Mediana: ordena os tempos e pega o do meio. Devolve em milissegundos.
    private static double mediana(long[] temposNano) {
        long[] copia = temposNano.clone();
        Arrays.sort(copia);
        return copia[copia.length / 2] / 1_000_000.0;
    }



    private static void linha() {
        System.out.println("=".repeat(78));
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println(texto);
        System.out.println("-".repeat(78));
    }

    private static void cabecalho() {
        System.out.printf("%-42s %17s %17s%n", "", "PorLetras", "Polinomial");
    }

    private static void linhaDados(String rotulo, long a, long b) {
        System.out.printf("%-42s %17d %17d%n", rotulo, a, b);
    }

    private static void linhaDecimal(String rotulo, double a, double b) {
        System.out.printf("%-42s %17.2f %17.2f%n", rotulo, a, b);
    }

    private static void linhaTempo(String rotulo, double a, double b) {
        System.out.printf("%-42s %17.3f %17.3f%n", rotulo, a, b);
    }
}
