import br.com.multitec.utils.collections.TableMap
import multitec.swing.components.textfields.MTextFieldString
import multitec.swing.core.MultitecRootPanel

import javax.swing.JButton;

public class Script extends sam.swing.ScriptBase{
    @Override
    public void execute(MultitecRootPanel tarefa) {
        MTextFieldString txtDad01nome = getComponente("txtDad01nome");
        txtDad01nome.setEnabled(false);

        JButton btnGravar = getComponente("btnGravar");
        btnGravar.addActionListener(e -> btnGravarSelected());
    }
    private void btnGravarSelected(){
        alterarNomeCashback();
    }
    private void alterarNomeCashback(){
        MTextFieldString txtDad01nome = getComponente("txtDad01nome");
        Integer ultimaSeqCashback = buscarUltimaSequenciaCashback();
        txtDad01nome.setValue((ultimaSeqCashback + 1).toString());
    }
    private Integer buscarUltimaSequenciaCashback(){
        String sql = "SELECT COALESCE(MAX(dad01nome::int),0) AS ultimaseq FROM dad01 ";
        TableMap tmUltimaSequencia = executarConsulta(sql)[0];

        return tmUltimaSequencia.getInteger("ultimaseq");
    }
}