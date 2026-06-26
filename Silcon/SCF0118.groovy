import multitec.swing.components.textfields.MTextArea
import multitec.swing.core.MultitecRootPanel;
import multitec.swing.components.textfields.MTextArea

import javax.swing.JButton


public class Script extends sam.swing.ScriptBase{
    @Override
    public void execute(MultitecRootPanel tarefa) {
        JButton btnGravar = getComponente("btnGravar");

        btnGravar.addActionListener(e -> btnGravarPressed());
    }
    private void btnGravarPressed(){
        MTextArea txtDaa01obs = getComponente("txtDaa01obs");
        if(exibirQuestao("Deseja verificar as naturezas antes de continuar?")) interromper("Verifique as naturezas.");

        if(txtDaa01obs.getValue() == null) interromper("Necessário preencher a observação do documento.")
    }
}