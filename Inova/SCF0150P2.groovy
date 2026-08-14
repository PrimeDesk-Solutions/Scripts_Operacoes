import multitec.swing.core.MultitecRootPanel
import sam.model.entities.aa.Aac10;
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import br.com.multitec.utils.collections.TableMap;
import multitec.swing.components.MRadioButton
import multitec.swing.components.MCheckBox;
import multitec.swing.components.autocomplete.MNavigation





public class Script extends sam.swing.ScriptBase{
    @Override
    public void execute(MultitecRootPanel tarefa) {
        MRadioButton rdoReceber = getComponente("rdoReceber");
        MRadioButton rdoPagar = getComponente("rdoPagar");
        MCheckBox chkConsiderarBaixa = getComponente("chkConsiderarBaixa");
        MNavigation nvgAbf20codigo = getComponente("nvgAbf20codigo");
        Aac10 empresaAtiva = obterEmpresaAtiva();
        Long idPlf = buscarIdPLF("400", empresaAtiva);

        nvgAbf20codigo.getNavigationController().setIdValue(idPlf);

        rdoReceber.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                idPlf = buscarIdPLF('100',empresaAtiva)
                nvgAbf20codigo.getNavigationController().setIdValue(idPlf);
            }
        });

        rdoPagar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                idPlf = buscarIdPLF('400',empresaAtiva)
                nvgAbf20codigo.getNavigationController().setIdValue(idPlf);
            }
        });

        chkConsiderarBaixa.setValue(1);
    }

    private Long buscarIdPLF(String codPLF, def empresaAtiva){

        def idEmpresa = empresaAtiva.getAac10id();


        TableMap tmPlf = executarConsulta("SELECT abf20id FROM abf20 WHERE abf20codigo = '" + codPLF + "' and abf20gc = " + idEmpresa.toString());

        if(tmPlf.size() == 0) interromper("PLF '" + codPLF + "' não foi encontrado na empresa ativa")

        return tmPlf.getLong("abf20id")
    }
}