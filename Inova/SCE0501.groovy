import br.com.multitec.utils.ValidacaoException
import jdk.jshell.spi.ExecutionControlProvider
import multitec.swing.components.textfields.MTextFieldLocalDate
import multitec.swing.core.MultitecRootPanel

import java.awt.Color
import java.time.LocalDate


import javax.swing.JButton;

public class Script extends sam.swing.ScriptBase{
    @Override
    public void execute(MultitecRootPanel tarefa) {
        JButton btnGravar = getComponente("btnGravar");

        btnGravar.addActionListener(e -> btnGravarPressed())
    }
    private void btnGravarPressed(){
        try{
            MTextFieldLocalDate txtBcc01data = getComponente("txtBcc01data");
            LocalDate dtLcto = txtBcc01data.getValue();
            LocalDate dataAtual = LocalDate.now();

            if(dtLcto.isAfter(dataAtual)){
                txtBcc01data.setBackground(new Color(255, 117, 117));
                throw new ValidacaoException("Não é permitido lançamentos com datas futuras.")
            }
        }catch (Exception e) {
            interromper(e.getMessage());
        }
    }
}