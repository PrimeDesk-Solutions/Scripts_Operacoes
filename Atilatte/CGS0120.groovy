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
import org.apache.poi.ss.usermodel.Table
import sam.model.entities.ab.Aba20
import sam.model.entities.ab.Aba2001
import sam.model.entities.da.Daa01

import javax.mail.Session
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.time.LocalDate


import javax.swing.JButton
import javax.swing.JPanel;

public class Script extends sam.swing.ScriptBase{
    MultitecRootPanel tarefa;
    @Override
    public void execute(MultitecRootPanel tarefa) {
        this.tarefa = tarefa;
        adicionarEventoSpread();
        criarMenuCustomizado();
    }
    private void criarMenuCustomizado(){
        criarMenu("Customizado", "Ruptura", e -> buscarDadosRuptura(), null)
        criarMenu("Customizado", "Feriado", e -> buscarFeriados(), null)
    }
    private void adicionarEventoSpread(){
        MSpread sprAba2001s = getComponente("sprAba2001s");
        MTextFieldString txtAba20codigo = getComponente("txtAba20codigo");
        String user = obterUsuarioLogado().getAab10user();

        sprAba2001s.addKeyListener(new KeyAdapter() {
            @Override
            void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == 116 || e.getKeyCode() == 118){
                    if(txtAba20codigo.getValue() != "032" || user == "MASTER2") return;
                    interromper("Para esse respositório não é permitido incluir ou excluir linhas da spread.");
                }

            }
        })
    }
    private void buscarDadosRuptura(){
        try{
            throw new ValidacaoException("Processo não disponivel");
            MTextFieldString txtAba20codigo = getComponente("txtAba20codigo");
            if(!"032".equals(txtAba20codigo.getValue().toString())) throw new ValidacaoException("Funcionalidade apenas para o repositório de RUPTURA.");

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

                        if(listRupturas != null && listRupturas.size() > 0) preencherSpreadRuptura(listNovosRegistros)
                    })
                    .post();

        }catch (Exception err){
            interromper("Falha ao buscar dados: " + err)
        }

    }
    private void preencherSpreadRuptura(List<TableMap> listNovosRegistros) {
        try{
            MSpread sprAba2001s = getComponente("sprAba2001s");
            List<TableMap> registrosAtualizarIncluir = new ArrayList<>();
            Aba20 aba20 = (Aba20) ((MultitecRootPanel) tarefa).registro;


            for (TableMap tmRegistroNovo in listNovosRegistros) {
                Aba2001 aba2001Existente = verificarRegistroExistente(tmRegistroNovo);

                if (aba2001Existente != null) {
                    TableMap body = new TableMap();
                    TableMap tmExistente = aba2001Existente.aba2001json != null ? aba2001Existente.aba2001json : new TableMap();
                    body.putAll(tmRegistroNovo);
                    body.put("aba2001id", aba2001Existente.aba2001id);
                    body.put("motivo", tmExistente.getInteger("motivo"));
                    body.put("sub_motivo", tmExistente.getInteger("sub_motivo"));
                    body.put("tipo", "atualizar");

                    registrosAtualizarIncluir.add(body);
                } else {
                    TableMap body = new TableMap();
                    body.putAll(tmRegistroNovo);
                    body.put("aba20id", aba20.aba20id);
                    body.put("tipo", "incluir");
                    body.put("motivo", 0);
                    body.put("sub_motivo", 0);

                    registrosAtualizarIncluir.add(body)
                }
            }

            if(registrosAtualizarIncluir != null && registrosAtualizarIncluir.size() > 0) atualizarOuIncluirRegistrosRuptura(registrosAtualizarIncluir);

        } catch (Exception e){
            interromper("Falha ao preecher spread: " + e.getMessage())
        }
    }
    private Aba2001 verificarRegistroExistente(TableMap registroNovo) {

        try{
            MSpread sprAba2001s = getComponente("sprAba2001s");

            List<Aba2001> listJaGravados = sprAba2001s.getValue();

            if (listJaGravados == null || listJaGravados.isEmpty()) {
                return null;
            }

            String codAuxiliarNovo = registroNovo.getString("cod_auxiliar");

            for (Aba2001 aba2001 in listJaGravados) {

                TableMap tmAba2001Gravado = aba2001.aba2001json;

                if (tmAba2001Gravado == null) {
                    continue;
                }

                String codAuxiliarGravado =
                        tmAba2001Gravado.getString("cod_auxiliar");

                if (codAuxiliarNovo == codAuxiliarGravado) {
                    return aba2001;
                }
            }

            return null;
        } catch (Exception e) {
            throw new ValidacaoException("Falha ao verificar registros: " + e.getMessage());
        }
    }
    private void atualizarOuIncluirRegistrosRuptura(List<TableMap> registrosAtualizarIncluir){
        try{
            WorkerRequest.create(tarefa.getWindow())
                    .initialText("Atualizando registros")
                    .dialogVisible(true)
                    .controllerEndPoint("servlet")
                    .methodEndPoint("run")
                    .param("name", "Atilatte.servlets.CGS_Atualizar_Incluir_Registro_Ruptura")
                    .header("ignore-body-decrypt", "true")
                    .parseBody(registrosAtualizarIncluir)
                    .success((response) -> {
                        tarefa.getWindow().getJMenuBar().getMnuArquivo().getMniAtualizar().doClick()
                    })
                    .post();
        } catch (Exception e){
            throw new ValidacaoException("Erro ao atualizar ou incluir registros: " + e.getMessage())
        }
    }
    private void buscarFeriados(){
        try{
            MTextFieldString txtAba20codigo = getComponente("txtAba20codigo");
            MSpread sprAba2001s = getComponente("sprAba2001s");

            if(!"026".equals(txtAba20codigo.getValue().toString()))
                throw new ValidacaoException("Funcionalidade apenas para o repositório de FERIADOS.")

            sprAba2001s.clear();
            sprAba2001s.refreshAll();

            TableMap body = new TableMap();
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
                                    preencherSpreadFeriados(tmFeriado);
                                }
                            }
                        }
                    })
                    .post();

        }catch(Exception err){
            interromper("Falha ao buscar dados: " + err.getMessage());
        }
    }
    private void preencherSpreadFeriados(TableMap tmNovoRegistro){
        MSpread sprAba2001s = getComponente("sprAba2001s");
        Aba2001 aba2001 = new Aba2001();
        aba2001.setAba2001json(tmNovoRegistro);
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