/*
    1. Altera a posição das colunas da spread dos itens
    2. Insere botão para imprimir documentos
 */
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.http.HttpRequest
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import multitec.swing.components.autocomplete.MNavigationController
import multitec.swing.components.spread.MSpread
import multitec.swing.components.spread.columns.MSpreadColumnBigDecimal
import multitec.swing.components.spread.columns.MSpreadColumnString
import multitec.swing.core.MultitecRootPanel
import multitec.swing.core.dialogs.ErrorDialog
import multitec.swing.request.WorkerRunnable
import multitec.swing.request.WorkerSupplier
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.printing.PDFPageable
import sam.model.entities.ea.Eaa01
import sam.swing.tarefas.scv.SCV2001
import sam.swing.tarefas.srf.SRF1001
import sam.model.entities.ea.Eaa0103
import javax.print.DocFlavor
import javax.print.PrintService
import javax.print.PrintServiceLookup
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.table.TableColumn;
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import java.awt.print.PrinterJob
import javax.swing.*;
import multitec.swing.components.autocomplete.MNavigation
import sam.model.entities.ea.Eaa0103;
import sam.model.entities.ea.Eaa01034;

public class Script extends sam.swing.ScriptBase{
    MultitecRootPanel tarefa;
    private JPanel tabFiscal = new JPanel();
    private JScrollPane scrTabFiscal;
    private MSpread<TableMap> sprFiscal;
    private Map<Long, TableMap> mapFiscal = new HashMap<Long, TableMap>();

    @Override
    public void execute(MultitecRootPanel tarefa) {
        this.tarefa = tarefa;
        reordenarColunasPorUsuario();
        criarBotao("Ordenar Campos", {reordenarColunasPorUsuario()})
        criarBotao("Replicar Dados Importação", {replicarDadosImportacao()})
        adicionaBotaoImprimirDocumento();
        criarSpreadETab(tarefa);
    }
    private void criarSpreadETab (MultitecRootPanel tarefa){

        JTabbedPane tabPedido = getComponente("tabPedido");

        tabPedido.addChangeListener(e -> {
            int selectedIndex = tabPedido.getSelectedIndex();
            if (selectedIndex == 8) {
                calcularAbaFiscal();
            }
        });

        tarefa.remove(tabPedido);
        tarefa.add(tabPedido)

        scrTabFiscal = new JScrollPane();
        sprFiscal = new MSpread();
        sprFiscal.setCanInclude(false);
        sprFiscal.setCanExclude(false);
        scrTabFiscal.setViewportView(sprFiscal);

        scrTabFiscal.setBounds(3, 3, 1332, 270);

        sprFiscal.clear();
        sprFiscal.addColumn((TableColumn)(new MSpreadColumnString("cfop")).text("CFOP").widthByCharCount(4).editable(false));
        sprFiscal.addColumn((TableColumn)(new MSpreadColumnBigDecimal("eaa0103total")).text("Total Doc").maxFractionDigits(6).widthByCharCount(15).editable(false));
        sprFiscal.addColumn((TableColumn)(new MSpreadColumnBigDecimal("bc_icms")).text("BC ICMS").maxFractionDigits(6).widthByCharCount(15).editable(false));
        sprFiscal.addColumn((TableColumn)(new MSpreadColumnBigDecimal("aliq_icms")).text("% ICMS").maxFractionDigits(6).widthByCharCount(15).editable(false));
        sprFiscal.addColumn((TableColumn)(new MSpreadColumnBigDecimal("icms")).text("ICMS").maxFractionDigits(6).widthByCharCount(15).editable(false));
        sprFiscal.addColumn((TableColumn)(new MSpreadColumnBigDecimal("icms_outras")).text("ICMS Outras").maxFractionDigits(6).widthByCharCount(15).editable(false));
        sprFiscal.addColumn((TableColumn)(new MSpreadColumnBigDecimal("icms_isento")).text("ICMS Isento").maxFractionDigits(6).widthByCharCount(15).editable(false));

        tabFiscal.add(scrTabFiscal);
        tabFiscal.setLayout(null);
        tabPedido.addTab("Fiscal", tabFiscal);

        //Seta o <ENTER> quando escolhido o PCD
        MNavigationController ctrEaa01pcd = getComponente("ctrEaa01pcd");
        ctrEaa01pcd.show = ctrEaa01pcd.show.andThen(abf01 -> {
            if(ctrEaa01pcd.getValue() != null){
                tarefa.eventosAbd01codigo();
            }
        });

    }
    private void calcularAbaFiscal(){
        try{
            Map<String, TableMap> mapAjustes = new HashMap<String, TableMap>();
            MSpread sprEaa0103s = getComponente("sprEaa0103s");
            def vlrIcms = BigDecimal.ZERO;
            def vlrBcIcms = BigDecimal.ZERO;
            def vlrIcmsOutras = BigDecimal.ZERO;
            def vlrIcmsIsento = BigDecimal.ZERO;
            def vlrTotDoc = BigDecimal.ZERO;
            TableMap valores = new TableMap();

            for(eaa0103 in sprEaa0103s.getValue()){
                String cfop = eaa0103.eaa0103cfop != null ? eaa0103.eaa0103cfop.aaj15codigo : null;
                TableMap tmJson = eaa0103.eaa0103json;
                vlrIcms = tmJson.getBigDecimal_Zero("icms");
                vlrBcIcms = tmJson.getBigDecimal_Zero("bc_icms");
                vlrIcmsOutras = tmJson.getBigDecimal_Zero("icms_outras");
                vlrIcmsIsento = tmJson.getBigDecimal_Zero("icms_isento");
                vlrTotDoc = eaa0103.eaa0103total;
                valores = mapAjustes.get(cfop) != null ? mapAjustes.get(cfop) : new TableMap();
                valores.put("icms", valores.getBigDecimal_Zero("icms") != BigDecimal.ZERO ? vlrIcms + valores.getBigDecimal_Zero("icms") : vlrIcms);
                valores.put("bc_icms", valores.getBigDecimal_Zero("bc_icms") != BigDecimal.ZERO ? vlrBcIcms + valores.getBigDecimal_Zero("bc_icms") : vlrBcIcms);
                valores.put("icms_outras", valores.getBigDecimal_Zero("icms_outras") != BigDecimal.ZERO ? vlrIcmsOutras + valores.getBigDecimal_Zero("icms_outras") : vlrIcmsOutras);
                valores.put("icms_isento", valores.getBigDecimal_Zero("icms_isento") != BigDecimal.ZERO ? vlrIcmsIsento + valores.getBigDecimal_Zero("icms_isento") : vlrIcmsIsento);
                valores.put("aliq_icms", tmJson.getBigDecimal_Zero("aliq_icms"));
                valores.put("eaa0103total", valores.getBigDecimal_Zero("eaa0103total") != BigDecimal.ZERO ? vlrTotDoc + valores.getBigDecimal_Zero("eaa0103total") : vlrTotDoc);

                mapAjustes.put(cfop, valores);
            }

            sprFiscal.clear();
            int i = 0;
            if(mapAjustes != null && mapAjustes.size() > 0) {
                for (String key : mapAjustes.keySet()) {
                    valores = mapAjustes.get(key);
                    sprFiscal.addRow();
                    sprFiscal.setValueAt(key, i, "cfop");
                    sprFiscal.setValueAt(valores.getBigDecimal_Zero("icms"), i, "icms");
                    sprFiscal.setValueAt(valores.getBigDecimal_Zero("bc_icms"), i, "bc_icms");
                    sprFiscal.setValueAt(valores.getBigDecimal_Zero("icms_outras"), i, "icms_outras");
                    sprFiscal.setValueAt(valores.getBigDecimal_Zero("icms_isento"), i, "icms_isento");
                    sprFiscal.setValueAt(valores.getBigDecimal_Zero("aliq_icms"), i, "aliq_icms");
                    sprFiscal.setValueAt(valores.getBigDecimal_Zero("eaa0103total"), i, "eaa0103total");
                    i++
                }
            }
        }catch(Exception ex){
            interromper("Erro: " + ex)
        }
    }
    private void reordenarColunasPorUsuario(){
        String user = obterUsuarioLogado().getAab10user().toUpperCase();
        if(user == "AMANDA" || user == "MASTER2") reordenarColunasFaturamento();
    }

    private void reordenarColunasFaturamento(){
        MSpread sprEaa0103s = getComponente("sprEaa0103s")

        sprEaa0103s.getColumnIndex("eaa0103seq") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103seq"), 0) : null;
        sprEaa0103s.getColumnIndex("eaa0103item.abm01tipo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103item.abm01tipo"), 1) : null;
        sprEaa0103s.getColumnIndex("eaa0103item.abm01codigo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103item.abm01codigo"), 2) : null;
        sprEaa0103s.getColumnIndex("eaa0103descr") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103descr"), 3) : null;
        sprEaa0103s.getColumnIndex("colBtnUD") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("colBtnUD"), 4) : null;
        sprEaa0103s.getColumnIndex("eaa0103umu.aam06codigo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103umu.aam06codigo"), 5) : null;
        sprEaa0103s.getColumnIndex("eaa0103umComl.aam06codigo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103umComl.aam06codigo"), 6) : null;
        sprEaa0103s.getColumnIndex("eaa0103qtUso") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103qtUso"), 7) : null;
        sprEaa0103s.getColumnIndex("eaa0103qtComl") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103qtComl"), 8) : null;
        sprEaa0103s.getColumnIndex("eaa0103unit") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103unit"), 9) : null;
        sprEaa0103s.getColumnIndex("eaa0103total") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103total"), 10) : null;
        sprEaa0103s.getColumnIndex("eaa0103totDoc") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103totDoc"), 11) : null;
        sprEaa0103s.getColumnIndex("eaa0103totFinanc") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103totFinanc"), 12) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.frete_dest") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.frete_dest"), 13) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.outras_despesas") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.outras_despesas"), 14) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.bc_ipi") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.bc_ipi"), 15) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.aliq_ipi") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.aliq_ipi"), 16) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.ipi") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.ipi"), 17) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.bc_icms") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.bc_icms"), 18) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.aliq_icms") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.aliq_icms"), 19) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.icms") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.icms"), 20) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.aliq_pis") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.aliq_pis"), 21) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.bc_pis") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.bc_pis"), 22) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.pis") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.pis"), 23) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.bc_cofins") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.bc_cofins"), 24) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.aliq_cofins") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.aliq_cofins"), 25) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.cofins") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.cofins"), 26) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.ibs_uf_aliq") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.ibs_uf_aliq"), 27) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.cbs_ibs_bc") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.cbs_ibs_bc"), 28) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.vlr_ibs") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.vlr_ibs"), 29) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.vlr_ibsuf") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.vlr_ibsuf"), 30) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.vlr_cbs") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.vlr_cbs"), 31) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.cbs_aliq") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.cbs_aliq"), 32) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.aliq_importacao") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.aliq_importacao"), 33) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.bc_importacao") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.bc_importacao"), 34) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.imposto_importacao") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.imposto_importacao"), 35) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.siscomex_valor") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.siscomex_valor"), 36) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.valor_aduaneiro") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.valor_aduaneiro"), 37) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.peso_liquido") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.peso_liquido"), 38) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.peso_bruto") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.peso_bruto"), 39) : null;
        sprEaa0103s.getColumnIndex("eaa0103json.volumes") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103json.volumes"), 40) : null;
        sprEaa0103s.getColumnIndex("eaa0103cfop.aaj15codigo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103cfop.aaj15codigo"), 41) : null;
        sprEaa0103s.getColumnIndex("eaa0103cfop.aaj15descr") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103cfop.aaj15descr"), 42) : null;
        sprEaa0103s.getColumnIndex("eaa0103ncm.abg01codigo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103ncm.abg01codigo"), 43) : null;
        sprEaa0103s.getColumnIndex("eaa0103ncm.abg01descr") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103ncm.abg01descr"), 44) : null;
        sprEaa0103s.getColumnIndex("eaa0103cstIcms.aaj10codigo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103cstIcms.aaj10codigo"), 45) : null;
        sprEaa0103s.getColumnIndex("eaa0103cstIcms.aaj10descr") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103cstIcms.aaj10descr"), 46) : null;
        sprEaa0103s.getColumnIndex("eaa0103cstIpi.aaj11codigo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103cstIpi.aaj11codigo"), 47) : null;
        sprEaa0103s.getColumnIndex("eaa0103cstIpi.aaj11descr") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103cstIpi.aaj11descr"), 48) : null;
        sprEaa0103s.getColumnIndex("eaa0103cstPis.aaj12codigo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103cstPis.aaj12codigo"), 49) : null;
        sprEaa0103s.getColumnIndex("eaa0103cstPis.aaj12descr") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103cstPis.aaj12descr"), 50) : null;
        sprEaa0103s.getColumnIndex("eaa0103cstCofins.aaj13codigo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103cstCofins.aaj13codigo"), 51) : null;
        sprEaa0103s.getColumnIndex("eaa0103cstCofins.aaj13descr") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103cstCofins.aaj13descr"), 52) : null;
        sprEaa0103s.getColumnIndex("eaa0103clasTribCbsIbs.aaj07codigo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103clasTribCbsIbs.aaj07codigo"), 53) : null;
        sprEaa0103s.getColumnIndex("eaa0103clasTribCbsIbs.aaj07descr") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103clasTribCbsIbs.aaj07descr"), 54) : null;
        sprEaa0103s.getColumnIndex("eaa0103cstCbsIbs.aaj09codigo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103cstCbsIbs.aaj09codigo"), 55) : null;
        sprEaa0103s.getColumnIndex("eaa0103cstCbsIbs.aaj09descr") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103cstCbsIbs.aaj09descr"), 56) : null;
        sprEaa0103s.getColumnIndex("eaa0103codBenef") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103codBenef"), 57) : null;
        sprEaa0103s.getColumnIndex("eaa0103dtEntrega") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103dtEntrega"), 58) : null;
        sprEaa0103s.getColumnIndex("eaa0103pcNum") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103pcNum"), 59) : null;
        sprEaa0103s.getColumnIndex("eaa0103item.abm01reduzido") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103item.abm01reduzido"), 60) : null;
    }

    private void adicionaBotaoImprimirDocumento(){
        JPanel panel7 = getComponente("panel7");
        def tela = tarefa.getWindow();
        tela.setBounds((int) tela.getBounds().x, (int) tela.getBounds().y, (int) tela.getBounds().width, (int) tela.getBounds().height + 40);

        def btnImprimir = new JButton();
        btnImprimir.setText("Imprimir");

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
            Eaa01 eaa01 = (Eaa01)  ((SRF1001) tarefa).registro;
            MNavigation nvgAah01codigo = getComponente("nvgAah01codigo");
            String codTipoDoc = nvgAah01codigo.getValue();

            if(eaa01 == null || eaa01.eaa01id == null) interromper("Antes de imprimir é necessário salvar o documento.");

            Long idDocumento = eaa01.eaa01id;

            WorkerSupplier.create(this.tarefa.getWindow(), {
                return buscarDadosImpressao(idDocumento, codTipoDoc);
            })
                    .initialText("Imprimindo Documento")
                    .dialogVisible(true)
                    .success({ bytes ->
                        enviarDadosParaImpressao(bytes);
                    })
                    .start();
        } catch (Exception err) {
            ErrorDialog.defaultCatch(this.tarefa.getWindow(), err);
        }
    }
    private byte[] buscarDadosImpressao(Long idDocumento, String codTipoDoc) {
        String caminhoRelatorio = buscarCaminhoRelatorio(codTipoDoc);
        String json = "{\"nome\":\""+caminhoRelatorio+"\",\"filtros\":{\"eaa01id\":"+idDocumento+"}}"

        ObjectMapper mapper = new ObjectMapper();
        JsonNode obj = mapper.readTree(json);
        return HttpRequest.create().controllerEndPoint("relatorio").methodEndPoint("gerarRelatorio").parseBody(obj).post().getResponseBody()
    }
    private String buscarCaminhoRelatorio(String codTipoDoc){
        String sql = "SELECT aah01formRelDoc FROM aah01 WHERE aah01codigo = '" + codTipoDoc + "'";

        TableMap tmTipoDoc = executarConsulta(sql)[0];

        if(tmTipoDoc == null || tmTipoDoc.size() == 0) throw new ValidacaoException("Não foi encontrado relatório de impressão no tipo de documento " + codTipoDoc + ".");

        return tmTipoDoc.getString("aah01formRelDoc");
    }

    protected void enviarDadosParaImpressao(byte[] bytes) {
        try {
            if(bytes == null || bytes.length == 0) {
                interromper("Não foi encontrado o relatório ou parametrizações para a impressão.");
            }

            PrintService myService = escolherImpressora();

            WorkerRunnable load = WorkerRunnable.create(this.tarefa.getWindow());
            load.dialogVisible(true);
            load.initialText("Enviando Documento para impressão");
            load.runnable({
                try {
                    PDDocument document = PDDocument.load(bytes);
                    PrinterJob job = PrinterJob.getPrinterJob();
                    job.setPageable(new PDFPageable(document));
                    job.setPrintService(myService);
                    job.setCopies(1);
                    job.setJobName("Documento");
                    job.print();
                    document.close();
                }catch (Exception err) {
                    interromper("Erro ao imprimir Documento. Verifique a impressora utilizada.");
                }
            });
            load.start();

        }catch (Exception err) {
            ErrorDialog.defaultCatch(this.tarefa.getWindow(), err, "Erro ao enviar dados para impressão.");
        }
    }

    protected PrintService escolherImpressora() {
        PrintService myService = null;

        PrintService[] ps = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
        if (ps.length == 0) {
            throw new ValidacaoException("Não foram encontradas impressoras.");
        }else {
            String nomeImpressoraComum = null;

            if(ps.length == 1) {
                nomeImpressoraComum = ps[0].getName();
            }else {
                JComboBox<String> jcb = new JComboBox<>();

                for (PrintService printService : ps) {
                    jcb.addItem(printService.getName());
                }

                JOptionPane.showMessageDialog(null, jcb, "Selecione a impressora", JOptionPane.QUESTION_MESSAGE);

                if (jcb.getSelectedItem() == null) {
                    throw new ValidacaoException("Nenhuma impressora selecionada.");
                }

                nomeImpressoraComum = (String)jcb.getSelectedItem();
            }

            for (PrintService printService : ps) {
                if (printService.getName().equalsIgnoreCase(nomeImpressoraComum)) {
                    myService = printService;
                    break;
                }
            }

            if (myService == null) {
                throw new ValidacaoException("Nenhuma impressora selecionada.");
            }
        }

        return myService;
    }

    private void replicarDadosImportacao() {
        try {
            def sprEaa0103s = getComponente("sprEaa0103s");

            if(sprEaa0103s.getRowCount() == 0) return;

            int row = sprEaa0103s.getSelectedRow();
            if(row < 0) return;

            Eaa0103 eaa0103 = sprEaa0103s.get(row);
            if(eaa0103.eaa01034s == null || eaa0103.eaa01034s.size() == 0) return;

            for(int i = 0; i < sprEaa0103s.getRowCount(); i++) {
                if(i == row) continue;

                List<Eaa01034> eaa01034sNovos = new ArrayList<>();

                for(Eaa01034 eaa01034 : eaa0103.eaa01034s) {
                    Eaa01034 eaa01034Novo = new Eaa01034();

                    eaa01034Novo.eaa01034num = eaa01034.eaa01034num;
                    eaa01034Novo.eaa01034drawback = eaa01034.eaa01034drawback;
                    eaa01034Novo.eaa01034dtReg = eaa01034.eaa01034dtReg;
                    eaa01034Novo.eaa01034local = eaa01034.eaa01034local;
                    eaa01034Novo.eaa01034ufLocal = eaa01034.eaa01034ufLocal;
                    eaa01034Novo.eaa01034dtDesemb = eaa01034.eaa01034dtDesemb;
                    eaa01034Novo.eaa01034codExp = eaa01034.eaa01034codExp;
                    eaa01034Novo.eaa01034viaTransp = eaa01034.eaa01034viaTransp;
                    eaa01034Novo.eaa01034afrmm = eaa01034.eaa01034afrmm;
                    eaa01034Novo.eaa01034formaImp = eaa01034.eaa01034formaImp;
                    eaa01034Novo.eaa01034cnpjAdq = eaa01034.eaa01034cnpjAdq;
                    eaa01034Novo.eaa01034ufAdq = eaa01034.eaa01034ufAdq;
                    eaa01034Novo.eaa01034decSimp = eaa01034.eaa01034decSimp;
                    eaa01034Novo.eaa01034total = eaa01034.eaa01034total;
                    eaa01034Novo.eaa01034servExt = eaa01034.eaa01034servExt;
                    eaa01034Novo.eaa01034bcPis = eaa01034.eaa01034bcPis;
                    eaa01034Novo.eaa01034pis = eaa01034.eaa01034pis;
                    eaa01034Novo.eaa01034pgtoPis = eaa01034.eaa01034pgtoPis;
                    eaa01034Novo.eaa01034bcCofins = eaa01034.eaa01034bcCofins;
                    eaa01034Novo.eaa01034cofins = eaa01034.eaa01034cofins;
                    eaa01034Novo.eaa01034pgtoCofins = eaa01034.eaa01034pgtoCofins;

                    eaa01034sNovos.add(eaa01034Novo);
                }

                sprEaa0103s.get(i).setEaa01034s(eaa01034sNovos);
            }

        }catch(Exception err){
            mostrarErros(err);
        }
    }

    @Override
    public void preSalvar(boolean salvo) {
    }

    @Override
    public void posSalvar(Long id) {
    }
}