import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.print.PrinterJob

import javax.print.DocFlavor
import javax.print.PrintService
import javax.print.PrintServiceLookup
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JOptionPane

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.printing.PDFPageable

import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.http.HttpRequest
import multitec.swing.components.spread.MSpread
import multitec.swing.core.MultitecRootPanel
import multitec.swing.core.dialogs.ErrorDialog
import multitec.swing.core.dialogs.Messages
import multitec.swing.request.WorkerRunnable
import multitec.swing.request.WorkerSupplier
import sam.swing.ScriptBase
import sam.swing.tarefas.spv.SPV1001
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

import sam.model.entities.ea.Eaa0103;
import sam.model.entities.ab.Abm01;

import java.util.Comparator;

class Script extends ScriptBase {

    @Override
    public void preSalvar(boolean salvo) {
        MSpread sprEaa0103s = getComponente("sprEaa0103s");
        boolean contemItem1 = false;
        boolean contemItem2 = false;

        for(Eaa0103 eaa0103 in sprEaa0103s.getValue()){
            Abm01 abm01 = eaa0103.eaa0103item;

            if(abm01.abm01codigo == "1001010010048") contemItem1 = true;
            if(abm01.abm01codigo == "1001010010049") contemItem2 = true;
        }

        if(contemItem1 && contemItem2){
            if(exibirQuestao("Deseja verificar o unitário dos itens 1001010010048 e 1001010010049?")){
                interromper("Verifique os unitários.");
            }
        }

        if(contemItem1){
            if(exibirQuestao("Deseja verificar o unitário do item 1001010010048 ?")){
                interromper("Verifique os unitários.");
            }
        }

        if(contemItem2){
            if(exibirQuestao("Deseja verificar o unitário do item 1001010010049 ?")){
                interromper("Verifique os unitários.");
            }
        }


    }

    private MultitecRootPanel tarefa;

    @Override
    public void execute(MultitecRootPanel tarefa) {
        this.tarefa = tarefa;
        criarMenu("Customizados", "Imprimir Documento", { selectionButtonImprimirPressed() }, null);


    }

    private void selectionButtonImprimirPressed() {
        try {

            def txtAbb01num = getComponente("txtAbb01num");
            def empresa = obterEmpresaAtiva()

            Integer numDoc = txtAbb01num.getValue();

            TableMap documentos = executarConsulta("select eaa01id "+
                    " from eaa01 "+
                    "inner join abb01 on abb01id = eaa01central "+
                    "inner join abd01 on abd01id = eaa01pcd "+
                    "where abd01es = 1 and abd01aplic = 1 "+
                    "and abb01num = "+numDoc+ " "+
                    "and eaa01eg = "+empresa.aac10id);
            if(documentos.isEmpty()){
                interromper("Documento não encontrado na central");
            }


            WorkerSupplier.create(this.tarefa.getWindow(), {
                return buscarDadosImpressao(numDoc);
            })
                    .initialText("Imprimindo DANFE")
                    .dialogVisible(true)
                    .success({ bytes ->
                        enviarDadosParaImpressao(bytes, numDoc);
                    })
                    .start();
        } catch (Exception err) {
            ErrorDialog.defaultCatch(this.tarefa.getWindow(), err);
        }
    }

    private byte[] buscarDadosImpressao(Integer numDoc) {
        Long idEmpresa = obterEmpresaAtiva().getAac10id();
        String json = "{\"nome\":\"MM.relatorios.srf.SRF_Danfe\",\"filtros\":{\"numeroInicial\":"+numDoc+",\"numeroFinal\":"+numDoc+"}}"

        ObjectMapper mapper = new ObjectMapper();
        JsonNode obj = mapper.readTree(json);
        return HttpRequest.create().controllerEndPoint("relatorio").methodEndPoint("gerarRelatorio").parseBody(obj).post().getResponseBody()
    }

    protected void enviarDadosParaImpressao(byte[] bytes, Integer numDoc) {
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
                    job.setJobName("DANFE " + numDoc);
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