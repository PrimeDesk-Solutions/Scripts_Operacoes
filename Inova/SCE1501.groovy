import multitec.swing.components.MCheckBox
import multitec.swing.core.MultitecRootPanel;
import multitec.swing.components.MPanelCheckMPMS;


public class Script extends sam.swing.ScriptBase{
    @Override
    public void execute(MultitecRootPanel tarefa) {
        MPanelCheckMPMS pnlChkMPMS = getComponente("pnlChkMPMS");
        MCheckBox chkItem = getComponente("chkItem");
        chkItem.setValue(1);
        pnlChkMPMS.getChkMaterial().setValue(1);
        pnlChkMPMS.getChkProduto().setValue(1);
    }
}