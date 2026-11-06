import java.util.LinkedList;


public abstract class TabelaHash {

    // Limite pedido no enunciado. "final" significa que nunca muda.
    public static final int CAPACIDADE = 32;

    // O corredor: um vetor em que cada posicao e uma lista de nomes.
    protected final LinkedList<String>[] tabela;

    private int quantidade = 0;           // nomes guardados na tabela
    private long colisoes = 0;            // insercoes que cairam em posicao ocupada
    private long comparacoes = 0;         // quantas vezes dois nomes foram comparados
    private final int[] colisoesPorPosicao = new int[CAPACIDADE];

    // Criar um vetor de listas gera um aviso inofensivo do Java; esta linha o silencia.
    public TabelaHash() {
        // Os quartos nascem null: a lista so e criada quando chega o primeiro nome.
        tabela = new LinkedList[CAPACIDADE];
    }

    //  recebe um nome e devolve o indice, de 0 a CAPACIDADE - 1.
    protected abstract int hash(String chave);

    // Insere o nome. Devolve false se ele ja estava na tabela.
    public boolean inserir(String chave) {
        if (buscar(chave)) return false;

        int i = hash(chave);

        if (tabela[i] == null) {
            tabela[i] = new LinkedList<>();       // 1a chave desta posicao
        }

        // Ja havia alguem nesta lista? Entao esta insercao e uma colisao.
        if (!tabela[i].isEmpty()) {
            colisoes++;
            colisoesPorPosicao[i]++;
        }

        tabela[i].add(chave);                     // vai para o fim da lista
        quantidade++;
        return true;
    }

    // A mesma funcao hash leva a posicao certa; depois, busca sequencial SO naquela lista.
    public boolean buscar(String chave) {
        int i = hash(chave);

        if (tabela[i] == null) return false;

        for (String nome : tabela[i]) {
            comparacoes++;
            if (nome.equals(chave)) return true;  // texto se compara com equals, nao com ==
        }
        return false;
    }

    // relatorio
    // Nome da classe filha.
    public String getNome() {
        return getClass().getSimpleName();
    }

    public int getQuantidade() {
        return quantidade;
    }

    public long getColisoes() {
        return colisoes;
    }

    public long getComparacoes() {
        return comparacoes;
    }

    public int getColisoesNaPosicao(int i) {
        return colisoesPorPosicao[i];
    }

    // Quantas chaves estao guardadas na posicao i (0 se o quarto nunca foi usado).
    public int getChavesNaPosicao(int i) {
        return tabela[i] == null ? 0 : tabela[i].size();
    }

    public int getPosicoesOcupadas() {
        int ocupadas = 0;
        for (int i = 0; i < CAPACIDADE; i++) {
            if (getChavesNaPosicao(i) > 0) ocupadas++;
        }
        return ocupadas;
    }

    public int getMenorLista() {
        int menor = getChavesNaPosicao(0);
        for (int i = 1; i < CAPACIDADE; i++) {
            menor = Math.min(menor, getChavesNaPosicao(i));
        }
        return menor;
    }

    public int getMaiorLista() {
        int maior = 0;
        for (int i = 0; i < CAPACIDADE; i++) {
            maior = Math.max(maior, getChavesNaPosicao(i));
        }
        return maior;
    }

    // Media de chaves por posicao, que tambem e o fator de carga da tabela.
    public double getMedia() {
        return (double) quantidade / CAPACIDADE;
    }

    // Desvio padrao: quanto as listas fogem da media. Quanto menor, mais uniforme.
    public double getDesvioPadrao() {
        double media = getMedia();
        double soma = 0;
        for (int i = 0; i < CAPACIDADE; i++) {
            double diferenca = getChavesNaPosicao(i) - media;
            soma += diferenca * diferenca;
        }
        return Math.sqrt(soma / CAPACIDADE);
    }
}
