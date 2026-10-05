import br.com.multitec.utils.ValidacaoException
import multitec.swing.components.MRadioButton
import multitec.swing.core.MultitecRootPanel

import javax.swing.JButton;

public class Script extends sam.swing.ScriptBase{
    @Override
    public void execute(MultitecRootPanel tarefa) {
        JButton btnGravar = getComponente("btnGravar");

        btnGravar.addActionListener(e -> btnGravarPressed())
    }
    private void btnGravarPressed(){
        try {
            MRadioButton rdoAprovar = getComponente("rdoAprovar");
            MRadioButton rdoDesaprovar = getComponente("rdoDesaprovar");
            String user = obterUsuarioLogado().getAab10user();

            if(rdoAprovar.isSelected() && user != "CAROL" && user != "GUILHERME"){
                throw new ValidacaoException("O usuário logado não tem permissão para aprovar documentos");
            }

            if(rdoDesaprovar.isSelected() && user != "DAIANA"){
                throw new ValidacaoException("O usuário logago não tem permissão para desaprovar documentos.")
            }
        } catch (Exception e){
            interromper(e.getMessage())
        }
    }
}