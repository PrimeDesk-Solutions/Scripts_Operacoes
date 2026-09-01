import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import multitec.swing.components.autocomplete.MNavigation
import multitec.swing.core.MultitecRootPanel;
import sam.swing.core.components.json.MJsonPanel
import javax.swing.JTabbedPane
import java.awt.Color
import multitec.swing.components.MComboBox



public class Script extends sam.swing.ScriptBase{
    @Override
    public void execute(MultitecRootPanel tarefa) {
    }

    @Override
    public void preSalvar(boolean salvo) {
        try{
            validarItem();
        }catch (Exception e){
            interromper(e.getMessage())
        }

    }
    private void validarItem(){
        JTabbedPane tabbedPane1 = getComponente("tabbedPane1");
        MJsonPanel pnlCamposLivresJSON = getComponente("pnlCamposLivresJSON");
        TableMap jsonAbm01 = pnlCamposLivresJSON.getValue();
        MNavigation nvgAbg01codigo = getComponente("nvgAbg01codigo");
        MComboBox cmbAbm01tipo = getComponente("cmbAbm01tipo");

        if(cmbAbm01tipo.getValue() != 1) return;

        if(jsonAbm01 == null || jsonAbm01.size() == 0 && !exibirQuestao("Item sem campo livres informado, deseja salvar mesmo assim?")){
            tabbedPane1.setSelectedIndex(3);
            throw new ValidacaoException("Preencha os campos livres antes de salvar!");
        };

        if(jsonAbm01 != null && jsonAbm01.size() > 0){
            if(jsonAbm01.getString("versao") == null || jsonAbm01.getString("versao").isEmpty()){
                boolean salvarRegistro = exibirQuestao("O campo 'Versão' não foi preechido. Deseja salvar mesmo assim?");
                if(!salvarRegistro){
                    tabbedPane1.setSelectedIndex(3);
                    throw new ValidacaoException("Preencha a versão do item antes de salvar!")
                }

            }
        }

        if(nvgAbg01codigo.getValue() == null){
            tabbedPane1.setSelectedIndex(2);
            nvgAbg01codigo.setBackground(new Color(255, 117, 117));
            throw new ValidacaoException("Necessário preencher o NCM do item antes de salvar!")
        }
    }

    @Override
    public void posSalvar(Long id) {
    }
}