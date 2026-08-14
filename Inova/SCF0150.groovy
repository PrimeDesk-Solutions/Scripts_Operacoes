import multitec.swing.core.MultitecRootPanel;
import multitec.swing.components.textfields.MTextFieldString
import javax.swing.JButton



public class Script extends sam.swing.ScriptBase{
    @Override
    public void execute(MultitecRootPanel tarefa) {
        JButton btnInserirLanc = getComponente("btnInserirLanc");
        JButton btnTransf = getComponente("btnTransf");
        JButton btnImportarDoc = getComponente("btnImportarDoc");

        btnInserirLanc.addActionListener(e -> verificarNomeMovimento());
        btnTransf.addActionListener(e -> verificarNomeMovimento());
        btnImportarDoc.addActionListener(e -> verificarNomeMovimento());
    }
    private void verificarNomeMovimento(){
        MTextFieldString txtDaa10nome = getComponente("txtDaa10nome");
        if(txtDaa10nome.getValue() == null) interromper("Necessário informar o nome do movimento.")
    }

    @Override
    public void preSalvar(boolean salvo) {
    }

    @Override
    public void posSalvar(Long id) {
    }
}