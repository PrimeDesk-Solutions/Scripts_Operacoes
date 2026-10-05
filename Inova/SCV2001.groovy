/*
    1. Altera a posição das colunas da spread dos itens
    2. Insere botão para imprimir documentos
 */

import br.com.multitec.utils.UiSqlColumn
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.http.HttpRequest
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import multitec.swing.components.MCheckBox
import multitec.swing.components.spread.MSpread
import multitec.swing.core.MultitecRootPanel
import multitec.swing.core.dialogs.ErrorDialog
import multitec.swing.core.dialogs.Messages
import multitec.swing.request.WorkerRunnable
import multitec.swing.request.WorkerSupplier
import org.apache.commons.collections.functors.ExceptionPredicate
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.printing.PDFPageable
import sam.model.entities.ab.Abm01
import sam.model.entities.da.Daa01
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103
import sam.model.entities.ea.Eaa0107
import sam.swing.core.window.PanelCadastro
import sam.swing.core.window.PanelListarCadastro
import sam.swing.tarefas.scv.SCV2001
import sam.swing.tarefas.scv.SCV2002
import sam.swing.tarefas.srf.SRF1001
import javax.print.DocFlavor
import javax.print.PrintService
import javax.print.PrintServiceLookup
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JOptionPane
import javax.swing.JPanel;
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.print.PrinterJob
import javax.swing.*;
import multitec.swing.components.autocomplete.MNavigation
import java.nio.file.Files
import java.nio.file.Paths
import java.awt.Desktop
import multitec.swing.request.WorkerRunnable
import multitec.swing.request.WorkerSupplier
import java.time.LocalDate
import java.time.LocalTime

public class Script extends sam.swing.ScriptBase{
    MultitecRootPanel tarefa;
    public Runnable windowLoadOriginal;
    PanelListarCadastro listaDoCadastro;
    Integer countInconsistencia = 0;

    @Override
    public void execute(MultitecRootPanel tarefa) {
        this.tarefa = tarefa;
        listaDoCadastro = ((PanelCadastro)tarefa).panelListarCadastro.get();
        this.windowLoadOriginal = tarefa.windowLoad ;
        tarefa.windowLoad = {novoWindowLoad()};

        adicionaBotaoImprimirDocumento();
        adicionarEventoPCD();
        reordenarColunasPorUsuario();
        criarBotao("Ordenar Campos", {reordenarColunasPorUsuario()})
    }
    protected void novoWindowLoad(){
        this.windowLoadOriginal.run();

        def ctrAbb01ent = getComponente("ctrAbb01ent");

        ctrAbb01ent.f4Columns = () -> {
            java.util.List<UiSqlColumn> uiSqlColumn = new ArrayList<>();
            UiSqlColumn abe01codigo = new UiSqlColumn("abe01codigo", "abe01codigo", "Código", 10);
            UiSqlColumn abe01nome = new UiSqlColumn("abe01nome", "abe01nome", "Nome", 60);
            UiSqlColumn abe01complem = new UiSqlColumn("abe01complem", "abe01complem", "Endereço", 60);
            UiSqlColumn abe01na = new UiSqlColumn("abe01na", "abe01na", "Nome Abreviado", 40);
            UiSqlColumn abe01ni = new UiSqlColumn("abe01ni", "abe01ni", "Número da Inscrição", 60);
            uiSqlColumn.addAll(Arrays.asList(abe01codigo, abe01nome, abe01complem, abe01na, abe01ni));
            return uiSqlColumn;
        };
    }
    private void adicionarEventoPCD(){
        MNavigation nvgAbd01codigo = getComponente("nvgAbd01codigo");
        String user = obterUsuarioLogado().getAab10user().toUpperCase();

        nvgAbd01codigo.addFocusListener(new FocusListener() {
            @Override
            void focusGained(FocusEvent e) {

            }

            @Override
            void focusLost(FocusEvent e) {
                if(nvgAbd01codigo.getValue() != null){
                    if(user == "DAIANA" || user == "MASTER2") reordenarColunasCompra();
                }
            }
        })
    }
    private void reordenarColunasPorUsuario(){
        String user = obterUsuarioLogado().getAab10user().toUpperCase();
        if(user == "DAIANA" || user == "MASTER2") reordenarColunasCompra();
    }

    private void reordenarColunasCompra(){
        MSpread sprEaa0103s = getComponente("sprEaa0103s")

        sprEaa0103s.getColumnIndex("eaa0103umComl.aam06codigo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103umComl.aam06codigo"), 1) : null;
        sprEaa0103s.getColumnIndex("eaa0103item.abm01tipo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103item.abm01tipo"), 2) : null;
        sprEaa0103s.getColumnIndex("eaa0103item.abm01codigo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103item.abm01codigo"), 3) : null;
        sprEaa0103s.getColumnIndex("eaa0103complem") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103complem"), 4) : null;
        sprEaa0103s.getColumnIndex("eaa0103qtComl") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103qtComl"), 5) : null;
        sprEaa0103s.getColumnIndex("eaa0103unit") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103unit"), 6) : null;
        sprEaa0103s.getColumnIndex("eaa0103total") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103total"), 7) : null;
        sprEaa0103s.getColumnIndex("eaa0103totDoc") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103totDoc"), 8) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.frete_dest") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.frete_dest"), 9) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.aliq_ipi") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.aliq_ipi"), 10) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.aliq_icms") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.aliq_icms"), 11) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.imposto_importacao") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.imposto_importacao"), 12) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.cotacao_dolar") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.cotacao_dolar"), 13) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.unit_convertido") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.unit_convertido"), 14) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.total_convertido") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.total_convertido"), 15) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.frete_dolar") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.frete_dolar"), 16) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.vl_tx_financ") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.vl_tx_financ"), 17) : null;
        sprEaa0103s.getColumnIndex("eaa0103ncm.abg01codigo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103ncm.abg01codigo"), 18) : null;
        sprEaa0103s.getColumnIndex("eaa0103ncm.abg01descr") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103ncm.abg01descr"), 19) : null;
        sprEaa0103s.getColumnIndex("eaa0103item.abm01reduzido") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103item.abm01reduzido"), 20) : null;
    }

    private void adicionaBotaoImprimirDocumento(){
        JPanel panel7 = getComponente("panel7");
        def tela = tarefa.getWindow();
        tela.setBounds((int) tela.getBounds().x, (int) tela.getBounds().y, (int) tela.getBounds().width, (int) tela.getBounds().height + 40);

        def btnImprimir = new JButton();
        btnImprimir.setText("Visualizar");

        // X    Y    W  H
        btnImprimir.setBounds(110, 100, 160, 20);
        panel7.add(btnImprimir);

        panel7.setLayout(null);

        panel7.revalidate();
        panel7.repaint();

        btnImprimir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnImprimirPressed();
            }
        });
    }
    private void btnImprimirPressed() {
        try {
            salvarPDF();
        } catch (Exception err) {
            ErrorDialog.defaultCatch(this.tarefa.getWindow(), err);
        }
    }
    void salvarPDF() {
        try {
            def txtAbb01num = getComponente("txtAbb01num");
            Integer numDoc = txtAbb01num.getValue()
            def empresa = obterEmpresaAtiva().aac10na
            Eaa01 eaa01 = (Eaa01)  ((SCV2001) tarefa).registro;
            MNavigation nvgAah01codigo = getComponente("nvgAah01codigo");
            String codTipoDoc = nvgAah01codigo.getValue();

            if(eaa01 == null || eaa01.eaa01id == null) interromper("Antes de visualizar é necessário salvar o documento.");

            Long idDocumento = eaa01.eaa01id;

            byte[] pdfBytes = buscarDadosRelatorio(idDocumento, codTipoDoc)

            String caminhoArquivo = System.getProperty("user.home") + "/Downloads/"+empresa+"-"+numDoc+".pdf"

            Files.write(Paths.get(caminhoArquivo), pdfBytes)
            File pdfFile = new File(caminhoArquivo);

            abrirPastaArquivo(pdfFile)

        } catch (IOException e) {
            e.printStackTrace()
        }
    }

    private byte[] buscarDadosRelatorio(Long idDocumento, String codTipoDoc) {
        String caminhoRelatorio = buscarCaminhoRelatorio(codTipoDoc);
        String json = "{\"nome\":\""+caminhoRelatorio+"\",\"filtros\":{\"eaa01id\":"+idDocumento+"}}"

        ObjectMapper mapper = new ObjectMapper();
        JsonNode obj = mapper.readTree(json);
        def result =  HttpRequest.create().controllerEndPoint("relatorio").methodEndPoint("gerarRelatorio").parseBody(obj).post().getResponseBody()

        return result
    }

    private String buscarCaminhoRelatorio(String codTipoDoc){
        String sql = "SELECT aah01formRelDoc FROM aah01 WHERE aah01codigo = '" + codTipoDoc + "'";

        TableMap tmTipoDoc = executarConsulta(sql)[0];

        if(tmTipoDoc == null || tmTipoDoc.size() == 0) throw new ValidacaoException("Não foi encontrado relatório de impressão no tipo de documento " + codTipoDoc + ".");

        return tmTipoDoc.getString("aah01formRelDoc");
    }

    private static void abrirPastaArquivo(File pdfFile) {
        try {
            Desktop.getDesktop().open(pdfFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void preSalvar(boolean salvo) {
        Eaa01 eaa01 = (Eaa01) ((MultitecRootPanel) tarefa).registro;

        verificarItensDoc(eaa01);
    }

    private void verificarItensDoc(Eaa01 eaa01){
        try {
            MNavigation nvgAbd01codigo = getComponente("nvgAbd01codigo");
            String codPCD = nvgAbd01codigo.getValue();
            Integer abd01aplic = buscarAplicacaoPCD(codPCD);
            MSpread sprEaa0103s = getComponente("sprEaa0103s");

            for(Eaa0103 eaa0103 in sprEaa0103s.getValue()){
                Abm01 abm01 = eaa0103.eaa0103item;
                Long idItem = abm01.abm01id;
                if(abd01aplic == 0){
                    TableMap jsonAbm0101 = buscarCamposLivresItem(idItem);
                    if (jsonAbm0101.getTableMap("abm0101json").getBigDecimal_Zero("preco_max_real") > 0 && eaa01.eaa01moeda == null) {
                        if (eaa0103.eaa0103unit > jsonAbm0101.getTableMap("abm0101json").getBigDecimal_Zero("preco_max_real")) {
                            String msg = "O unitário do item " + abm01.abm01codigo + " " + abm01.abm01descr + " excedeu o limite de preço unitário real permitido que é: " + jsonAbm0101.getTableMap("abm0101json").getBigDecimal_Zero("preco_max_real");
                            gravarInconsitencia(eaa01, msg, "Script Unit. Real", 1);
                        }
                    }

                    if (eaa01.eaa01moeda != null && eaa01.eaa01moeda.aag10codigo == "01") {
                        if (jsonAbm0101.getTableMap("abm0101json").getBigDecimal_Zero("preco_max_dolar") > 0 && eaa0103.eaa0103unit > jsonAbm0101.getTableMap("abm0101json").getBigDecimal_Zero("preco_max_dolar")) {
                            String msg = "O unitario do item " + abm01.abm01codigo + " " + abm01.abm01descr + " excedeu o limite de preço unitario dolar permitido que é: " + jsonAbm0101.getTableMap("abm0101json").getBigDecimal_Zero("preco_max_dolar");
                            gravarInconsitencia(eaa01, msg, "Script Unit Dólar", 1);
                        }
                    }
                    if (eaa0103.eaa0103qtUso.compareTo(jsonAbm0101.getTableMap("abm0101json").getBigDecimal_Zero("lote_min")) < 0) {
                        String msg = "O item " + abm01.abm01codigo + " " + abm01.abm01descr + " não atingiu o lote mínimo de compra."
                        gravarInconsitencia(eaa01, msg, "Script Lote Min", 1);
                    }

                    BigDecimal saldoAtual = buscarSaldoAtualItem(abm01.abm01id);
                    BigDecimal qtdPedido = eaa0103.eaa0103qtComl;
                    BigDecimal estqMax = jsonAbm0101.getBigDecimal_Zero("abm0101estMax");
                    BigDecimal saldoLiquido = (saldoAtual + qtdPedido) - estqMax;
                    if (estqMax != 0 && saldoAtual.compareTo(estqMax) > 0) {
                        String msg = "Item - " + eaa0103.eaa0103seq + " " + abm01.abm01codigo + "\n";
                        msg += " A quantidade solicitada + Saldo do estoque não pode ser maior que o estoque máximo\n";
                        msg += " Estoque Máximo: " + estqMax + "\n";
                        msg += " Qtd Solicitada: " + qtdPedido + "\n";
                        msg += " Saldo Estoque: " + saldoAtual + "\n";
                        msg += " Total Excedido: " + saldoLiquido + "\n";

                        gravarInconsitencia(eaa01, msg, "Pré-Gravar Est. Max", 1);
                    }
                }
            }
        } catch (Exception e){
            interromper("Falha ao verificar inconsistências do documento: " + e.getMessage())
        }
    }

    private Integer buscarAplicacaoPCD(String codPCD){
        try{
            String sql = "SELECT abd01aplic FROM abd01 WHERE abd01codigo = '" + codPCD + "'";

            TableMap tmPCD = executarConsulta(sql)[0];

            return tmPCD.getInteger("abd01aplic");
        } catch (Exception e) {
            throw new ValidacaoException("Falha ao buscar informações do PCD: " + e.getMessage() )
        }
    }

    private TableMap buscarCamposLivresItem(Long idItem){
        try {
            TableMap jsonAbm0101 = new TableMap();

            String sql = "SELECT abm0101json, abm0101estMax FROM abm0101 WHERE abm0101item = " + idItem + " AND abm0101empresa = " + obterEmpresaAtiva().getAac10id();

            TableMap jsonItem = executarConsulta(sql)[0];

            jsonAbm0101.putAll(jsonItem);

            return jsonAbm0101;
        } catch (Exception e){
            throw new ValidacaoException(e.getMessage())
        }
    }

    private void gravarInconsitencia(Eaa01 eaa01, String msg, String identificador, Integer bloquear) {
        try {
            MSpread sprEaa0107s = getComponente("sprEaa0107s");

            verificarInconsistenciaJaGravada(identificador);

            Eaa0107 eaa0107 = criarInconsistencia(msg, identificador);

            sprEaa0107s.addRow(eaa0107);
            sprEaa0107s.refreshAll();
            countInconsistencia = sprEaa0107s.getValue().size();
            bloquearDesbloquearDocumento(eaa01, bloquear);
        } catch (Exception e) {
            throw new ValidacaoException(e.getMessage())
        }

    }
    private void verificarInconsistenciaJaGravada(String identificador){
        try{
            MSpread sprEaa0107s = getComponente("sprEaa0107s");

            if (sprEaa0107s.getValue().size() > 0) {
                Integer linha = 0
                for(int i = 0; i < sprEaa0107s.getValue().size(); i++ ){
                    if (sprEaa0107s.get(i).eaa0107ident.contains(identificador) && sprEaa0107s.get(i).eaa0107justificativa == null) {
                        sprEaa0107s.removeRow(i);
                    }
                }
            }
        } catch (Exception e){
            throw new ValidacaoException(e.getMessage())
        }
    }

    private Eaa0107 criarInconsistencia(String msg, String identificador){
        Eaa0107 eaa0107 = new Eaa0107();
        eaa0107.eaa0107msg = msg;
        eaa0107.eaa0107user = obterUsuarioLogado();
        eaa0107.eaa0107data = null
        eaa0107.eaa0107hora = null
        eaa0107.eaa0107ident = identificador;

        return eaa0107;
    }

    private BigDecimal buscarSaldoAtualItem(Long idItem){
       try {
           String sql = "SELECT COALESCE(SUM(bcc02qt), 0.00) AS saldo FROM bcc02 WHERE bcc02item = " + idItem;

           TableMap tmSaldo = executarConsulta(sql)[0];

           return tmSaldo.getBigDecimal_Zero("saldo")
       } catch (Exception e) {
           throw new ValidacaoException("Falha ao buscar saldo do item " + e.getMessage());
       }

    }

    private void bloquearDesbloquearDocumento(Eaa01 eaa01, Integer bloquear){
        MCheckBox chkEaa01bloqueado = getComponente("chkEaa01bloqueado");
        chkEaa01bloqueado.setValue(bloquear);
    }

    private void verificarInconsistenciasDocumento(Long id){
        try {
            Integer numDocSalvo = buscarNumDocSalvo(id);
            String mensagem = countInconsistencia > 0 ? "Documento " + numDocSalvo + " salvo com inconsistência." : "Documento " + numDocSalvo + " salvo com sucesso.";;
            exibirMensagemAoGravar(mensagem);
        } catch (Exception e) {
            interromper("Falha ao verificar inconsistências documento: " + e.getMessage())
        }
    }

    private Integer buscarNumDocSalvo(Long idDoc){
        try {
            TableMap doc = executarConsulta(" SELECT abb01num FROM Eaa01 " +
                                            " INNER JOIN Abb01 ON abb01id = eaa01central " +
                                            " WHERE eaa01id = " + idDoc + " " +
                                            obterWherePadrao("Eaa01"));

            return doc.getInteger("abb01num");
        } catch (Exception e) {
            throw new ValidacaoException("Não foi possível buscar o número do documento salvo: " + e.getMessage());
        }
    }
    private void exibirMensagemAoGravar(String mensagem){
        try {
            Messages.create(listaDoCadastro.getWindow()).text(mensagem).autoCloseAfter(5000).success();
        } catch (Exception e) {
            throw new ValidacaoException("Erro ao exibir mensagem de gravação: " + e.getMessage());
        }
    }

    @Override
    public void posSalvar(Long id) {
        verificarInconsistenciasDocumento(id);
    }
}
