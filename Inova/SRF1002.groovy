/*
    1. Altera a posição das colunas da spread dos itens
    2. Insere botão para imprimir documentos
 */
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.http.HttpRequest
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import multitec.swing.components.spread.MSpread
import multitec.swing.core.MultitecRootPanel
import multitec.swing.core.dialogs.ErrorDialog
import multitec.swing.request.WorkerRequest
import multitec.swing.request.WorkerRunnable
import multitec.swing.request.WorkerSupplier
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.printing.PDFPageable
import sam.model.entities.ea.Eaa01
import sam.swing.tarefas.scv.SCV2001
import sam.swing.tarefas.srf.SRF1001
import sam.swing.tarefas.srf.SRF1002
import multitec.swing.components.textfields.MTextArea


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
import groovy.swing.SwingBuilder







public class Script extends sam.swing.ScriptBase{
    MultitecRootPanel tarefa;

    @Override
    public void execute(MultitecRootPanel tarefa) {
        this.tarefa = tarefa;
        reordenarColunas();
        adicionaBotaoImprimirDocumento();
        adicionarEventosEntidades();
    }
    private void reordenarColunas(){
        MSpread sprEaa0103s = getComponente("sprEaa0103s")

        sprEaa0103s.getColumnIndex("eaa0103descr") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103descr"), 3) : null;
    }
    private adicionarEventosEntidades(){
        MNavigation nvgAbd01codigo = getComponente("nvgAbd01codigo");

        nvgAbd01codigo.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {};

            public void focusLost(FocusEvent e) {
                if(nvgAbd01codigo.getValue() == null) return;
                MNavigation nvgAbe01codigo = getComponente("nvgAbe01codigo");
                String codEntidade = nvgAbe01codigo.getValue();

                if(codEntidade == null) return;
                Long idEntidade = buscarIdEntidade(codEntidade);
                buscarTitulosVencidosEntidade(idEntidade);

            }
        });
    }
    private Long buscarIdEntidade(String codEntidade){
        String sql = "SELECT abe01id FROM abe01 WHERE abe01codigo = '" + codEntidade + "'" //+ idEmpresa.toString();
        TableMap tmEntidade = executarConsulta(sql)[0];
        Long idEntidade = tmEntidade.getLong("abe01id");

        return idEntidade;
    }
    private void buscarTitulosVencidosEntidade(Long idEntidade){
        try{
            TableMap body = new TableMap()
            body.put("abe01id",idEntidade)
            WorkerRequest.create(tarefa.getWindow())
                    .initialText("Buscando Limite de Crédito")
                    .dialogVisible(false)
                    .controllerEndPoint("servlet")
                    .methodEndPoint("run")
                    .param("name", "Inova.servlet.Buscar_Titulos_Vencidos_Entidade")
                    .header("ignore-body-decrypt", "true")
                    .parseBody(body)
                    .success((response) -> {
                        Boolean contemTituloVencido = response.parseResponse(new TypeReference<Boolean>(){});
                        if(!contemTituloVencido) return;
                        if(contemTituloVencido && !exibirQuestao("Constam títulos vencidos para esse cliente, necessário consultar financeiro. Deseja continuar?")){
                            throw new ValidacaoException("Operação Cancelada.")
                        }else {
                            def swing = new groovy.swing.SwingBuilder();
                            MTextArea txtEaa01obsUsoInt = getComponente("txtEaa01obsUsoInt");
                            String strTexto = txtEaa01obsUsoInt.getValue();
                            swing.edt {
                                dialog(title:"Observação de Aprovação", size:[500,250], defaultCloseOperation:javax.swing.JFrame.DISPOSE_ON_CLOSE, show:true, modal:true, locationRelativeTo:null) {
                                    borderLayout()
                                    lblTexto = label(text:"Consta títulos vencidos. Informe motivo da autorização.", constraints: java.awt.BorderLayout.NORTH)
                                    scrollPane(){
                                        txtArea = textArea(text:"", constraints:java.awt.BorderLayout.CENTER, rows:50, columns:65)
                                    }
                                    button(text:'Ok', actionPerformed: {txtEaa01obsUsoInt.setValue("Controle: " + txtArea.text); dispose()}, constraints:java.awt.BorderLayout.SOUTH);
                                }
                            }
                        }
                    })
                    .post();
        }catch(Exception err){
            throw new ValidacaoException(err.getMessage());
        }
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
            Eaa01 eaa01 = (Eaa01)  ((SRF1002) tarefa).registro;
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