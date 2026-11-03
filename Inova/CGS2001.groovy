import multitec.swing.components.spread.MSpread
import multitec.swing.core.MultitecRootPanel;

public class Script extends sam.swing.ScriptBase{
    @Override
    public void execute(MultitecRootPanel tarefa) {
        alterarPosicoesColunas();
    }

    private void alterarPosicoesColunas(){
        alterarPosicoesColunasEnderecos();
    }
    private void alterarPosicoesColunasEnderecos(){
        MSpread sprAbe0101s = getComponente("sprAbe0101s");
        sprAbe0101s.getColumnIndex("abe0101principal") != -1 ? sprAbe0101s.moveColumn(sprAbe0101s.getColumnIndex("abe0101principal"), 0) : null;
        sprAbe0101s.getColumnIndex("abe0101entrega") != -1 ? sprAbe0101s.moveColumn(sprAbe0101s.getColumnIndex("abe0101entrega"), 1) : null;
        sprAbe0101s.getColumnIndex("abe0101cobranca") != -1 ? sprAbe0101s.moveColumn(sprAbe0101s.getColumnIndex("abe0101cobranca"), 2) : null;
        sprAbe0101s.getColumnIndex("abe0101outras") != -1 ? sprAbe0101s.moveColumn(sprAbe0101s.getColumnIndex("abe0101outras"), 3) : null;
        sprAbe0101s.getColumnIndex("abe0101cep") != -1 ? sprAbe0101s.moveColumn(sprAbe0101s.getColumnIndex("abe0101cep"), 4) : null;
        sprAbe0101s.getColumnIndex("abe0101endereco") != -1 ? sprAbe0101s.moveColumn(sprAbe0101s.getColumnIndex("abe0101endereco"), 5) : null;
        sprAbe0101s.getColumnIndex("abe0101numero") != -1 ? sprAbe0101s.moveColumn(sprAbe0101s.getColumnIndex("abe0101numero"), 6) : null;
        sprAbe0101s.getColumnIndex("abe0101bairro") != -1 ? sprAbe0101s.moveColumn(sprAbe0101s.getColumnIndex("abe0101bairro"), 7) : null;
        sprAbe0101s.getColumnIndex("abe0101municipio.aag0201nome") != -1 ? sprAbe0101s.moveColumn(sprAbe0101s.getColumnIndex("abe0101municipio.aag0201nome"), 8) : null;
        sprAbe0101s.getColumnIndex("abe0101municipio.aag0201uf.aag02uf") != -1 ? sprAbe0101s.moveColumn(sprAbe0101s.getColumnIndex("abe0101municipio.aag0201uf.aag02uf"), 9) : null;
        sprAbe0101s.getColumnIndex("abe0101pais.aag01nome") != -1 ? sprAbe0101s.moveColumn(sprAbe0101s.getColumnIndex("abe0101pais.aag01nome"), 10) : null;
        sprAbe0101s.getColumnIndex("abe0101complem") != -1 ? sprAbe0101s.moveColumn(sprAbe0101s.getColumnIndex("abe0101complem"), 11) : null;
        sprAbe0101s.getColumnIndex("abe0101ddd1") != -1 ? sprAbe0101s.moveColumn(sprAbe0101s.getColumnIndex("abe0101ddd1"), 12) : null;
        sprAbe0101s.getColumnIndex("abe0101fone1") != -1 ? sprAbe0101s.moveColumn(sprAbe0101s.getColumnIndex("abe0101fone1"), 13) : null;
        sprAbe0101s.getColumnIndex("abe0101eMail") != -1 ? sprAbe0101s.moveColumn(sprAbe0101s.getColumnIndex("abe0101eMail"), 14) : null;

    }

    @Override
    public void preSalvar(boolean salvo) {
    }

    @Override
    public void posSalvar(Long id) {
    }
}