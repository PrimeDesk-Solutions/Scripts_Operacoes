import multitec.swing.core.MultitecRootPanel;
import multitec.swing.components.MComboBox
import javax.swing.JCheckBox;



public class Script extends sam.swing.ScriptBase{
    @Override
    public void execute(MultitecRootPanel tarefa) {
        definirCamposDefault();
    }

    private void definirCamposDefault(){
        MComboBox cmbFormulaImportar = getComponente("cmbFormulaImportar");
        JCheckBox chkAbrirSRF1001 = getComponente("chkAbrirSRF1001");

        cmbFormulaImportar.setValue("Fast.formulas.srf.SRF_ImportarXMLEntrada");
        chkAbrirSRF1001.setSelected(true);
    }
}