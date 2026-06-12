import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import com.microsoft.schemas.office.office.STInsetMode
import multitec.swing.components.autocomplete.MNavigation
import multitec.swing.core.MultitecRootPanel
import org.apache.poi.ss.usermodel.Table
import sam.swing.core.components.json.MJsonPanel
import multitec.swing.components.textfields.MTextFieldBigDecimal;
import multitec.swing.components.textfields.MTextFieldInteger;
import multitec.swing.components.textfields.MTextFieldLocalDate;
import multitec.swing.components.textfields.MTextFieldString;

import javax.swing.JButton;

public class Script extends sam.swing.ScriptBase {
    @Override
    public void execute(MultitecRootPanel tarefa) {
        JButton btnGravar = getComponente("btnGravar");
        btnGravar.addActionListener(e -> btnGravarPressed());
    }

    private void btnGravarPressed() {
        try {
            Long idDocumento = buscarIdDocumento();

            String sql = "SELECT CAST(daa01json ->> 'user_baixa' AS INTEGER) AS userbaixa FROM daa01 WHERE daa01id = " + idDocumento;

            TableMap tmUserBaixa = executarConsulta(sql)[0] == null ? new TableMap() : executarConsulta(sql)[0];
            Long idUserBaixa = tmUserBaixa.getLong("userbaixa");

            String nomeUsuarioBaixa = buscarNomeUsuarioBaixa(idUserBaixa);

            if(idUserBaixa == null) return;
            if (idUserBaixa != obterUsuarioLogado().getAab10id() && !obterUsuarioLogado().aab10custom) throw new ValidacaoException("O usuário logado não pode estornar esse documento, solicitar ao usuario " + nomeUsuarioBaixa + " para efeturar o estorno.")

        } catch (Exception e) {
            interromper(e.getMessage())
        }
    }

    private Long buscarIdDocumento() {
        MNavigation nvgAbb10codigo = getComponente("nvgAbb10codigo");
        MNavigation nvgAbe01codigo = getComponente("nvgAbe01codigo");
        MNavigation nvgAah01codigo = getComponente("nvgAah01codigo");
        MTextFieldInteger txtAbb01num = getComponente("txtAbb01num");
        MTextFieldString txtAbb01serie = getComponente("txtAbb01serie");
        MTextFieldLocalDate txtAbb01data = getComponente("txtAbb01data");
        MTextFieldBigDecimal txtAbb01valor = getComponente("txtAbb01valor");
        MTextFieldString txtAbb01parcela = getComponente("txtAbb01parcela");
        MTextFieldInteger txtAbb01quita = getComponente("txtAbb01quita");

        String idOper = nvgAbb10codigo.getNavigationController().getValue() != null ? nvgAbb10codigo.getNavigationController().getValue().getAbb10id() : "";
        String idEntidade = nvgAbe01codigo.getNavigationController().getValue() != null ? nvgAbe01codigo.getNavigationController().getValue().getAbe01id() : "";
        String idTipoDoc = nvgAah01codigo.getNavigationController().getValue() != null ? nvgAah01codigo.getNavigationController().getValue().getAah01id() : "";
        String numDoc = txtAbb01num.getValue();
        String serie = txtAbb01serie.getValue() != null ? txtAbb01serie.getValue() : "";
        String data = txtAbb01data.getValue();
        String valor = txtAbb01valor.getValue();
        String parcela = txtAbb01parcela.getValue();
        String quita = txtAbb01quita.getValue();

        String whereSerie = txtAbb01serie.getValue() != null ? " AND abb01serie = '" + serie + "' " : "";

        String sql = "SELECT daa01id " +
                " FROM daa01 " +
                " INNER JOIN abb01 ON daa01central = abb01id " +
                " WHERE abb01operCod = " + idOper +
                " AND abb01tipo = " + idTipoDoc +
                " AND abb01num = " + numDoc +
                " AND abb01ent = " + idEntidade +
                " AND abb01data = '" + data + "' " +
                " AND abb01valor = " + valor +
                " AND abb01parcela = '" + parcela + "' " +
                whereSerie +
                " AND abb01quita = " + quita;

        TableMap tmIdDoc = executarConsulta(sql)[0];

        if(tmIdDoc == null) throw new ValidacaoException("Script: Não foi encontrado documento com os filtros informados.")

        return tmIdDoc.getLong("daa01id");
    }
    private String buscarNomeUsuarioBaixa(Long idUserBaixa){
        String sql = "SELECT aab10user FROM aab10 WHERE aab10id = " + idUserBaixa;

        return executarConsulta(sql)[0].getString("aab10user");
    }
}