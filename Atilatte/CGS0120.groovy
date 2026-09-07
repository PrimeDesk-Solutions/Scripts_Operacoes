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
        adicionarBotaoPreencherFeriados();
        criarMenuCustomizado()
    }
    private void criarMenuCustomizado(){
        criarMenu("Customizado", "Ruptura", e -> preencherDadosRuptura(), null)
        criarMenu("Customizado", "Estoque", e -> preencherDadosEstoque(), null)
    }
    private void preencherDadosRuptura(){
        try{
            MTextFieldString txtAba20codigo = getComponente("txtAba20codigo");
            if(!"032".equals(txtAba20codigo.getValue().toString())) interromper("Funcionalidade apenas para o repositório de RUPTURA.");

            TableMap body = new TableMap();
            List<TableMap> feriados = new ArrayList();
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

                                preencherSpread(tmRuptura);
                            }
                        }
                    })
                    .post();

        }catch (Exception err){
            interromper("Erro ao preecher repositório: " + err)
        }

    }
    private void preencherDadosEstoque(){
        MTextFieldString txtAba20codigo = getComponente("txtAba20codigo");
        if(!"033".equals(txtAba20codigo.getValue().toString())) interromper("Funcionalidade apenas para o repositório de ESTOQUE.");
    }
    private void adicionarBotaoPreencherFeriados(){
        JPanel panel2 = getComponente("panel2");

        JButton btnPreencherFeriados = new JButton();
        btnPreencherFeriados.setText("Preencher Repositorio");
        btnPreencherFeriados.setBounds(280,15,154, 30);
        btnPreencherFeriados.addActionListener(e -> btnPreencherPressed())

        panel2.add(btnPreencherFeriados);
    }
    private void btnPreencherPressed(){
        MTextFieldString txtAba20codigo = getComponente("txtAba20codigo");
        MSpread sprAba2001s = getComponente("sprAba2001s");

        sprAba2001s.clear();
        sprAba2001s.refreshAll();

        if(!"026".equals(txtAba20codigo.getValue().toString())) interromper("Funcionalidade apenas para o repositório de FERIADOS.");

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

                        if(listFeriados != null && listFeriados.size() > 0){
                            for(feriado in listFeriados){
                                if(feriado.getString("type").toUpperCase() == "NATIONAL"){
                                    TableMap tmFeriado = new TableMap();
                                    String data = feriado.getString("date");
                                    String descrFeriado = feriado.getString("name");

                                    tmFeriado.put("data", data.replace("-",""));
                                    tmFeriado.put("feriado", descrFeriado);

                                    preencherSpread(tmFeriado);
                                }
                            }
                        }
                    })
                    .post();

        }catch(Exception err){
            throw new ValidacaoException(err.getMessage());
        }
    }
    private void preencherSpread(TableMap tm){
        MSpread sprAba2001s = getComponente("sprAba2001s");

        Aba2001 aba2001 = new Aba2001();
        aba2001.setAba2001json(tm);
        sprAba2001s.addRow(aba2001);
        sprAba2001s.refreshAll();
    }

    @Override
    public void preSalvar(boolean salvo) {
    }

    @Override
    public void posSalvar(Long id) {
    }
}