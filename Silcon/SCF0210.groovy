import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.http.HttpRequest
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import multitec.swing.components.autocomplete.MNavigation
import multitec.swing.core.MultitecRootPanel
import multitec.swing.core.dialogs.ErrorDialog
import multitec.swing.request.WorkerRunnable
import multitec.swing.request.WorkerSupplier
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.printing.PDFPageable
import sam.model.entities.ea.Eaa01
import sam.model.entities.da.Dad01
import sam.swing.tarefas.scv.SCV2002

import javax.print.DocFlavor
import javax.print.PrintService
import javax.print.PrintServiceLookup
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JOptionPane
import java.awt.print.PrinterJob;

public class Script extends sam.swing.ScriptBase{

    MultitecRootPanel tarefa;
    @Override
    public void execute(MultitecRootPanel tarefa) {
        this.tarefa = tarefa;

        adicionarBotaoImprimirCashback()
    }

    @Override
    public void preSalvar(boolean salvo) {
    }

    @Override
    public void posSalvar(Long id) {
    }
    private void adicionarBotaoImprimirCashback(){
        JButton btnImprimir = new JButton();

        btnImprimir.setBounds(621, 110, 243, 25);
        btnImprimir.setText("Imprimir Cashback");
        btnImprimir.addActionListener(e -> btnImprimirCashPressed())

        tarefa.add(btnImprimir);
    }
    private void btnImprimirCashPressed(){
        imprimirCashback();
    }
    private void imprimirCashback() {
        try {

            MNavigation nvgDad01nome = getComponente("nvgDad01nome");
            Dad01 dad01 = nvgDad01nome.getNavigationController().getValue();
            if(dad01 == null) interromper("Nenhum cashback informado para impressão.")


            Long idCashback = dad01.dad01id;

            WorkerSupplier.create(this.tarefa.getWindow(), {
                return buscarDadosImpressao(idCashback);
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
    private byte[] buscarDadosImpressao(Long idCashback) {
        String caminhoRelatorio = "Silcon.relatorios.scf.SCF_Cashback_Clientes";
        String json = "{\"nome\":\""+caminhoRelatorio+"\",\"filtros\":{\"id\":"+idCashback+"}}"

        ObjectMapper mapper = new ObjectMapper();
        JsonNode obj = mapper.readTree(json);
        return HttpRequest.create().controllerEndPoint("relatorio").methodEndPoint("gerarRelatorio").parseBody(obj).post().getResponseBody()
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
                    job.setJobName("Relatorio");
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
}