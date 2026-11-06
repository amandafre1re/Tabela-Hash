
public class TabelaHashPorLetras extends TabelaHash {

    @Override
    protected int hash(String chave) {
        return Math.floorMod(chave.length() - 1, tabela.length);
    }
}
