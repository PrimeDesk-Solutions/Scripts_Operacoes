import multitec.swing.components.MCheckBox
import multitec.swing.components.MPanelCheckMPMS
import multitec.swing.components.autocomplete.MNavigation
import multitec.swing.core.MultitecRootPanel

import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.MouseAdapter;

public class Script extends sam.swing.ScriptBase{
    @Override
    public void execute(MultitecRootPanel tarefa) {
        definirValoresDefault();
        adicionarEventoCodItemInicial();
    }
    private void definirValoresDefault(){
        MPanelCheckMPMS pnlChkMPMS = getComponente("pnlChkMPMS");
        MCheckBox chkItem = getComponente("chkItem");
        MNavigation nvgAbm20codigoSaida = getComponente("nvgAbm20codigoSaida");
        MNavigation nvgAbm20codigoEntrada = getComponente("nvgAbm20codigoEntrada");
        MNavigation nvgAah01codigo = getComponente("nvgAah01codigo");

        chkItem.setValue(1);
        pnlChkMPMS.getChkMaterial().setValue(1);
        pnlChkMPMS.getChkProduto().setValue(1);
        nvgAbm20codigoSaida.getNavigationController().setIdValue(129025);
        nvgAbm20codigoEntrada.getNavigationController().setIdValue(129024);
        nvgAah01codigo.getNavigationController().setIdValue(418441)
    }
    private void adicionarEventoCodItemInicial(){
        MNavigation nvgAbm01codigoInicial = getComponente("nvgAbm01codigoInicial");
        MNavigation nvgAbm01codigoFinal = getComponente("nvgAbm01codigoFinal");
        nvgAbm01codigoInicial.addFocusListener(new FocusAdapter() {
            @Override
            void focusLost(FocusEvent e) {
                if(nvgAbm01codigoInicial.getValue() != null){
                    Long idItemIni = nvgAbm01codigoInicial.getNavigationController().getValue().getAbm01id();
                    nvgAbm01codigoFinal.getNavigationController().setIdValue(idItemIni)
                }
            }
        })
    }
}