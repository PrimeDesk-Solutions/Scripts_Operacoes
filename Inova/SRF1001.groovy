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






public class Script extends sam.swing.ScriptBase{
    MultitecRootPanel tarefa;
    private JPanel tabFiscal = new JPanel();
    private JScrollPane scrTabFiscal;
    private MSpread<TableMap> sprFiscal;
    private Map<Long, TableMap> mapFiscal = new HashMap<Long, TableMap>();

    @Override
    public void execute(MultitecRootPanel tarefa) {
        this.tarefa = tarefa;
        reordenarColunas();
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
    private void reordenarColunas(){
        MSpread sprEaa0103s = getComponente("sprEaa0103s")

        sprEaa0103s.getColumnIndex("eaa0103descr") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103descr"), 3) : null;
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

    @Override
    public void preSalvar(boolean salvo) {
    }

    @Override
    public void posSalvar(Long id) {
    }
}