/*
    1. Busca a lista de feriado na API BrasilAPI
    2. Preenche o repositorio de dados de feriado
 */

import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import com.fasterxml.jackson.core.type.TypeReference
import multitec.swing.components.spread.MSpread
import multitec.swing.components.textfields.MTextFieldString
import multitec.swing.core.MultitecRootPanel
import multitec.swing.request.WorkerRequest
import sam.model.entities.ab.Aba2001
import java.time.LocalDate


import javax.swing.JButton
import javax.swing.JPanel;

public class Script extends sam.swing.ScriptBase{
    MultitecRootPanel tarefa;
    @Override
    public void execute(MultitecRootPanel tarefa) {
        this.tarefa = tarefa;
        criarMenuCustomizado()
    }
    private void criarMenuCustomizado(){
        criarMenu("Customizado", "Ruptura", e -> preencherDadosRuptura(), null)
        criarMenu("Customizado", "Feriado", e -> preencherFeriados(), null)
    }
    private void preencherDadosRuptura(){
        try{
            throw new ValidacaoException("Processo indisponível")
            MTextFieldString txtAba20codigo = getComponente("txtAba20codigo");
            if(!"032".equals(txtAba20codigo.getValue().toString())) interromper("Funcionalidade apenas para o repositório de RUPTURA.");

            TableMap body = new TableMap();
            WorkerRequest.create(tarefa.getWindow())
                    .initialText("Buscando Dados Ruptura")
                    .dialogVisible(true)
                    .controllerEndPoint("servlet")
                    .methodEndPoint("run")
                    .param("name", "Atilatte.servlets.CGS_Compor_Rupturas_Repositorio")
                    .header("ignore-body-decrypt", "true")
                    .parseBody(body)
                    .success((response) -> {
                        List<TableMap> listRupturas = response.parseResponse(new TypeReference<List<TableMap>>(){});
                        List<TableMap> listNovosRegistros = new ArrayList<>();
                        if(listRupturas != null && listRupturas.size() > 0){
                            for(ruptura in listRupturas){
                                TableMap tmRuptura = new TableMap();

                                tmRuptura.put("data_corte", ruptura.getDate("dataped"));
                                tmRuptura.put("cod_item", ruptura.getString("abm01codigo"));
                                tmRuptura.put("descricao_item", ruptura.getString("naitem"));
                                tmRuptura.put("umu", ruptura.getString("umu"));
                                tmRuptura.put("qtd_pedido", ruptura.getBigDecimal_Zero("qtdpedido"));
                                tmRuptura.put("vlr_pedido", ruptura.getBigDecimal_Zero("valorpedido"));
                                tmRuptura.put("vlr_entregue", ruptura.getBigDecimal_Zero("valorentregue"));
                                tmRuptura.put("qtd_entregue", ruptura.getBigDecimal_Zero("qtdentregue"));
                                tmRuptura.put("saldo", ruptura.getBigDecimal_Zero("saldo"));
                                tmRuptura.put("cod_auxiliar", ruptura.getString("codaux"));
                                tmRuptura.put("usuario", obterUsuarioLogado().getAab10nome());
                                tmRuptura.put("data_registro", LocalDate.now().toString().replace("-", ""));

                                listNovosRegistros.add(tmRuptura);
                            }
                        }

                        if(listRupturas != null && listRupturas.size() > 0) preencherSpread(listNovosRegistros)
                    })
                    .post();

        }catch (Exception err){
            interromper("Erro ao preecher repositório: " + err)
        }

    }
    private boolean verificarRegistroExistente(TableMap registroNovo){
        MSpread sprAba2001s = getComponente("sprAba2001s");
        List<Aba2001> listRepositorio = sprAba2001s.getValue();

        if (listRepositorio != null && listRepositorio.size() > 0){
            for(Aba2001 aba2001 in listRepositorio){
                TableMap tmAba2001 = aba2001.aba2001json != null ? aba2001.aba2001json : new TableMap();
                if(registroNovo.getString("cod_auxiliar") == null) return false;
                if(tmAba2001.getString("cod_auxiliar") == registroNovo.getString("cod_auxiliar")) return true;

            }
        }
    }
    private void preencherFeriados(){
        MTextFieldString txtAba20codigo = getComponente("txtAba20codigo");
        MSpread sprAba2001s = getComponente("sprAba2001s");

        if(!"026".equals(txtAba20codigo.getValue().toString())) interromper("Funcionalidade apenas para o repositório de FERIADOS.");

        sprAba2001s.clear();
        sprAba2001s.refreshAll();

        try{
            TableMap body = new TableMap();
            List<TableMap> feriados = new ArrayList();
            WorkerRequest.create(tarefa.getWindow())
                    .initialText("Compondo lista de feriados")
                    .dialogVisible(true)
                    .controllerEndPoint("servlet")
                    .methodEndPoint("run")
                    .param("name", "Atilatte.servlets.CGS_Compor_Feriados_Repositorio")
                    .header("ignore-body-decrypt", "true")
                    .parseBody(body)
                    .success((response) -> {
                        List<TableMap> listFeriados = response.parseResponse(new TypeReference<List<TableMap>>(){});
                        List<TableMap> listRegistrosNovos = new ArrayList<>();

                        if(listFeriados != null && listFeriados.size() > 0){
                            for(feriado in listFeriados){
                                if(feriado.getString("type").toUpperCase() == "NATIONAL"){
                                    TableMap tmFeriado = new TableMap();
                                    String data = feriado.getString("date");
                                    String descrFeriado = feriado.getString("name");

                                    tmFeriado.put("data", data.replace("-",""));
                                    tmFeriado.put("feriado", descrFeriado);
                                    listRegistrosNovos.add(tmFeriado);
                                }
                            }
                            preencherSpread(listRegistrosNovos);
                        }
                    })
                    .post();

        }catch(Exception err){
            throw new ValidacaoException(err.getMessage());
        }
    }
    private void preencherSpread(List<TableMap> listNovosRegistros){
        MSpread sprAba2001s = getComponente("sprAba2001s");
        boolean registroJaExiste = false;
        for(tmRegistroNovo in listNovosRegistros){
            registroJaExiste = verificarRegistroExistente(tmRegistroNovo);
            if (registroJaExiste) continue;
            Aba2001 aba2001 = new Aba2001();
            aba2001.setAba2001json(tmRegistroNovo);
            sprAba2001s.addRow(aba2001);
            sprAba2001s.refreshAll();
        }



    }

    @Override
    public void preSalvar(boolean salvo) {
    }

    @Override
    public void posSalvar(Long id) {
    }
}