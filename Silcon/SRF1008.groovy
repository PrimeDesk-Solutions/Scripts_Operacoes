import multitec.swing.components.MComboBox
import multitec.swing.components.spread.MSpread
import multitec.swing.core.MultitecRootPanel
import sam.dto.srf.SRF1008DtoItem

import javax.swing.JButton
import javax.swing.JCheckBox;

public class Script extends sam.swing.ScriptBase{
    MultitecRootPanel panel;
    MComboBox<Integer> cmbTipoItens = new MComboBox<>();

    @Override
    public void execute(MultitecRootPanel tarefa) {
        this.panel = tarefa;

        definirValoresDefault()
        criarComboTipoItem();

    }
    private void definirValoresDefault(){
        MComboBox cmbFormulaImportar = getComponente("cmbFormulaImportar");
        JCheckBox chkAbrirSRF1001 = getComponente("chkAbrirSRF1001");

        chkAbrirSRF1001.setSelected(true);
        cmbFormulaImportar.setValue("Silcon.formulas.srf.ImportaXmlNFe_Entrada");
    }
    private void criarComboTipoItem(){
        cmbTipoItens.addItem(0, "0-Material");
        cmbTipoItens.addItem(1, "1-Produto");
        cmbTipoItens.addItem(2, "2-Merc");
        cmbTipoItens.addItem(3, "3-Serv");
        cmbTipoItens.setBounds(1200, 264, 100, 25);
        cmbTipoItens.addActionListener(e -> cmbTipoItensActionPerformed())

        panel.add(cmbTipoItens)
    }
    private void cmbTipoItensActionPerformed(){
        Integer tipoItem = cmbTipoItens.getValue();
        replicarTipoItem(tipoItem);
    }
    private void replicarTipoItem(Integer tipo){
        MSpread sprItens = getComponente("sprItens");
        List<SRF1008DtoItem> sprValue = sprItens.getValue();

        if(sprValue == null || sprValue.size() == 0) return;

        for(srf1008dto in sprValue){
            srf1008dto.abm01.setAbm01tipo(tipo);
        }

        sprItens.refreshAll()
    }
}