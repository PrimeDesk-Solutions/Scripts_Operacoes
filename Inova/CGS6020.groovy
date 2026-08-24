import br.com.multitec.utils.collections.TableMap
import multitec.swing.core.MultitecRootPanel
import sam.model.entities.ab.Abp20

public class Script extends sam.swing.ScriptBase{
    MultitecRootPanel tarefa;
    @Override
    public void execute(MultitecRootPanel tarefa) {
        this.tarefa = tarefa;
        Abp20 abp20 = (Abp20) ((MultitecRootPanel) tarefa).registro;

    }

    private void verificarComposicao(Abp20 abp20, Integer acao){
        if(abp20.abp20id == null) return;

        String sql = "SELECT bab01id FROM bab01 WHERE bab01comp = " + abp20.abp20id + " AND bab01status = 1 LIMIT 1";

        TableMap tmOrdem = executarConsulta(sql)[0];

        if(tmOrdem != null && tmOrdem.size() > 0){
            switch (acao){
                case 0:
                    exibirAtencao("ATENÇÃO: Existe documento de produção em aberto utilizando esta fórmula.");
                    break;
                default:
                    interromper("Existe documento de produção em aberto utilizando esta fórmula.");
                    break;
            }
        }
    }

    @Override
    public void preSalvar(boolean salvo) {
        Abp20 abp20 = (Abp20) ((MultitecRootPanel) tarefa).registro;
        verificarComposicao(abp20, 1);
    }

    @Override
    public void posSalvar(Long id) {
    }


}