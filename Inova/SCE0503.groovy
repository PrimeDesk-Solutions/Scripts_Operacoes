import multitec.swing.components.MCheckBox
import multitec.swing.components.MPanelCheckMPMS
import multitec.swing.components.autocomplete.MNavigation
import multitec.swing.core.MultitecRootPanel;

public class Script extends sam.swing.ScriptBase{
    @Override
    public void execute(MultitecRootPanel tarefa) {
        MPanelCheckMPMS pnlChkMPMS = getComponente("pnlChkMPMS");
        MCheckBox chkItem = getComponente("chkItem");
        MNavigation nvgAbm20codigoSaida = getComponente("nvgAbm20codigoSaida");
        MNavigation nvgAbm20codigoEntrada = getComponente("nvgAbm20codigoEntrada");

        chkItem.setValue(1);
        pnlChkMPMS.getChkMaterial().setValue(1);
        pnlChkMPMS.getChkProduto().setValue(1);

        nvgAbm20codigoSaida.getNavigationController().setIdValue(129025);
        nvgAbm20codigoEntrada.getNavigationController().setIdValue(129024);
    }
}