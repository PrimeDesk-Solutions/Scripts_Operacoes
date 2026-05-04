import br.com.multitec.utils.collections.TableMap
import multitec.swing.components.textfields.MTextFieldString
import multitec.swing.core.MultitecRootPanel;

public class Script extends sam.swing.ScriptBase{
    @Override
    public void execute(MultitecRootPanel tarefa) {
        MTextFieldString txtDad01nome = getComponente("txtDad01nome");
        txtDad01nome.setEnabled(false);
    }

    @Override
    public void preSalvar(boolean salvo) {
        MTextFieldString txtDad01nome = getComponente("txtDad01nome");
        Integer ultimaSeqCashback = buscarUltimaSequenciaCashback();
        txtDad01nome.setValue((ultimaSeqCashback + 1).toString())
    }
    private Integer buscarUltimaSequenciaCashback(){
        String sql = "SELECT COALESCE(MAX(dad01nome::int),0) AS ultimaseq FROM dad01 ";
        TableMap tmUltimaSequencia = executarConsulta(sql)[0];

        return tmUltimaSequencia.getInteger("ultimaseq");
    }

    @Override
    public void posSalvar(Long id) {
    }
}