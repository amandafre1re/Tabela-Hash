
public class TabelaHashPolinomial extends TabelaHash {

    @Override
    protected int hash(String chave) {
        int h = 0;
        for (int i = 0; i < chave.length(); i++) {
            h = 31 * h + chave.charAt(i);
        }
        // Em nomes compridos, h estoura o limite do int e pode ficar negativo.
        return Math.floorMod(h, tabela.length);
    }
}
