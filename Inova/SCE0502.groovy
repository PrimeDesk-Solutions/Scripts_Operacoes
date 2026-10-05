import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import jdk.jshell.spi.ExecutionControlProvider
import multitec.swing.components.autocomplete.MNavigation
import multitec.swing.components.textfields.MTextFieldLocalDate
import multitec.swing.core.MultitecRootPanel

import java.awt.Color
import java.time.LocalDate


import javax.swing.JButton;

public class Script extends sam.swing.ScriptBase{
    @Override
    public void execute(MultitecRootPanel tarefa) {
        String user = obterUsuarioLogado().getAab10user();
        adicionarEventoBtnGravar();

        if(user == "PAOLA" || user == "MASTER2") valoresDefaultPCP();
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
    private void adicionarEventoBtnGravar(){
        JButton btnGravar = getComponente("btnGravar");

        btnGravar.addActionListener(e -> btnGravarPressed())
    }
    private void valoresDefaultPCP(){
        Long idPLF = buscarIDPLF("61");
        MNavigation nvgAbm20codigo = getComponente("nvgAbm20codigo");

        nvgAbm20codigo.getNavigationController().setIdValue(idPLF);
    }
    private Long buscarIDPLF(String codPLF){
        String sql = "SELECT abm20id FROM abm20 WHERE abm20codigo = '" + codPLF + "'";

        TableMap tmPLF = executarConsulta(sql)[0];

        return tmPLF != null ? tmPLF.getLong("abm20id") : null;
    }
}