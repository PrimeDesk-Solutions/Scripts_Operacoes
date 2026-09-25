/*
    1. Altera a posição das colunas da spread dos itens
    2. Insere botão para imprimir documentos
 */
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.http.HttpRequest
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import multitec.swing.components.spread.MSpread
import multitec.swing.core.MultitecRootPanel
import multitec.swing.core.dialogs.ErrorDialog
import multitec.swing.request.WorkerRunnable
import multitec.swing.request.WorkerSupplier
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.printing.PDFPageable
import sam.model.entities.ea.Eaa01
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

public class Script extends sam.swing.ScriptBase{
    MultitecRootPanel tarefa;

    @Override
    public void execute(MultitecRootPanel tarefa) {
        this.tarefa = tarefa;
        adicionaBotaoImprimirDocumento();
        adicionarEventoPCD();
        reordenarColunasPorUsuario();
        criarBotao("Ordenar Campos", {reordenarColunasPorUsuario()})
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
    }

    @Override
    public void posSalvar(Long id) {
    }
}