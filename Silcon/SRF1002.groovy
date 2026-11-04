/*
    SRF1002 - DOCUMENTOS DE VENDAS (NOTAS)

    1. AO SAIR DO CAMPO PCD (nvgAbd01codigo), com a entidade preenchida:
       a) Mostra uma janela de "ATENÇÃO" com a observação de uso interno da entidade
          (abe02obsUsoInt), se houver.
       b) Verifica títulos vencidos (servlet Silcon.servlet.Buscar_Titulos_Vencidos_Entidade):
          - Se houver, pergunta se deseja continuar. Não -> cancela a operação.
          - Sim -> avisa para inserir uma observação, se necessário.
       c) Em seguida, verifica o limite de crédito (servlet Silcon.servlet.Verificar_Limite_Credito_Entidade):
          - Se excedido, pergunta se deseja continuar. Não -> cancela a operação.
          - Sim -> avisa para inserir uma observação, se necessário.
          - Regra de cálculo (feita no servlet):
              - Data limite de crédito vencida -> bloqueia.
              - Saldo devedor = Docs a Receber em aberto (DAA01: abb01quita = 0 AND daa01rp = 0)
                              + Docs Batch (EAA01: abb10tipoCod = 1 AND eaa01esMov = 1 AND eaa01clasDoc = 1
                                            AND eaa01cancData IS NULL AND eaa01iSCF = 2)
              - Saldo devedor > limite -> limite excedido.

    2. AO SAIR DO CAMPO TABELA DE PREÇO (nvgAbe40codigo), se houver itens:
       pergunta se deseja recalcular; se sim, zera o preço unitário dos itens e aciona
       o botão "Recalcular Documento".

    3. BOTÃO "IMPRIMIR" + CAMPO "VIAS" (1 a 9), no painel panel7 (a janela aumenta 40px de altura):
       - Exige que o documento esteja salvo.
       - Tipo de documento 04 ou 16 -> abre a tela de impressão de documentos (SRF1009) já
         preenchida (tipo, número, "somente com protocolo" se houver chave NF-e, e
         "reimpressão" se o documento já foi impresso).
       - Demais tipos -> gera o PDF pelo relatório do tipo de documento (aah01formRelDoc) e envia
         direto para a impressora com o número de vias informado (se houver mais de uma
         impressora, pede para escolher).
         Exceção: PCD 60026 usa o relatório Silcon.relatorios.srf.SRF_Impressao_Documento_Interno_S_Desc.

    4. BOTÃO "REPLICAR REP ITENS" (painel pnlDados2):
       copia o Representante 0 do documento (id, código e nome abreviado) para o campo livre
       (eaa0103json) de todos os itens. Exige o Representante 0 preenchido e itens na spread.

    5. LISTA DE BUSCA (F4) personalizada:
       - Entidade: Código, Nome, Endereço, Nome Abreviado, Nº Inscrição.
       - Item: Código, GTIN, Descrição, Fabricante.

    6. SPREAD DE ITENS: reordena as colunas (Descrição, UM, UM Coml, Qtde Coml, Unitário, Total,
       Total Doc, Total Financ, Entrega, NCM código e NCM descrição).

    7. Desativa o botão de incluir novo endereço de entrega da entidade.

    8. CANCELAR / ESC: pergunta "Deseja realmente sair?"; se não, mantém a tela aberta.

    9. AO SALVAR:
       - Antes: bloqueia o PCD 60000 para o usuário de id 1904684.
       - Depois: exibe a mensagem "Documento <número> salvo com sucesso." (fecha em 5 segundos).
 */

package scripts

import com.fasterxml.jackson.core.type.TypeReference
import multitec.swing.components.autocomplete.MNavigation
import multitec.swing.components.textfields.MTextArea
import multitec.swing.core.MultitecRootPanel
import multitec.swing.core.utils.WindowUtils
import sam.model.entities.ea.Eaa0103

import java.awt.event.FocusListener;
import javax.swing.SwingUtilities
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException
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
import multitec.swing.core.dialogs.ErrorDialog
import multitec.swing.core.dialogs.Messages
import multitec.swing.request.WorkerRunnable
import multitec.swing.request.WorkerSupplier
import sam.swing.ScriptBase
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import multitec.swing.core.MultitecRootPanel;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import multitec.swing.core.dialogs.Messages;
import multitec.swing.request.WorkerRequest;
import multitec.swing.request.WorkerRunnable;
import sam.swing.tarefas.srf.SRF1009;
import br.com.multitec.utils.UiSqlColumn;
import multitec.swing.components.textfields.MTextFieldInteger;
import multitec.swing.components.textfields.MTextFieldString;
import multitec.swing.core.dialogs.Messages;
import sam.swing.core.window.PanelListarCadastro;
import sam.swing.core.window.PanelCadastro;
import javax.swing.JLabel;




public class SRF1002 extends sam.swing.ScriptBase{
    def strTexto = "";
    String obs = "";
    MultitecRootPanel tarefa;
    public Runnable windowLoadOriginal;
    PanelListarCadastro listaDoCadastro;
    JSpinner txtVias = new JSpinner();

    @Override
    public void execute(MultitecRootPanel tarefa) {
        this.tarefa = tarefa;
        listaDoCadastro = ((PanelCadastro)tarefa).panelListarCadastro.get();
        alterarWindowLoad();
        adicionarEventoMenu();
        reordenarColunas();
        criarBotoes();
        adicionarEventosComponentes();
        criarCampoNumeroVias();
        desativarBotoes();
    }
    private alterarWindowLoad(){
        this.windowLoadOriginal = tarefa.windowLoad ;
        tarefa.windowLoad = {novoWindowLoad()};
    }
    private void adicionarEventoMenu(){
        tarefa.getWindow().getJMenuBar().getMnuArquivo().getMniCancelar().addActionListener(mnu -> this.adicionaEventoESC(mnu));
    }
    private void adicionarEventosComponentes(){
        adicionaEventoPCD();
        adicionarEventoTabelaPreco();
    }
    private void criarBotoes(){
        adicionaBotaoImprimirDanfe();
        adicionarBotaoReplicarRepresentantes();
    }
    private void adicionarBotaoReplicarRepresentantes(){
        JPanel pnlDados2 = getComponente("pnlDados2");
        JButton btnReplicarRep = new JButton();
        btnReplicarRep.setBounds(770, 284, 110, 25);
        btnReplicarRep.setText("Replicar Rep Itens");
        btnReplicarRep.addActionListener(e -> btnReplicarRepPressed());

        pnlDados2.add(btnReplicarRep)
    }
    private void btnReplicarRepPressed(){
        try{
            if(!exibirQuestao("Deseja replicar o representante para o itens do documento?")) return;
            MNavigation nvgAbe01codigoRep0 = getComponente("nvgAbe01codigoRep0");
            def rep0 = nvgAbe01codigoRep0.getNavigationController().getValue();
            if(rep0 == null) throw new ValidacaoException("Necessário preencher o representante 0 para replicar.");

            TableMap sprValue = new TableMap();
            sprValue.put("id_representante", rep0.getAbe01id());
            sprValue.put("cod_representante", rep0.getAbe01codigo());
            sprValue.put("nome_representante", rep0.getAbe01na());
            alterarCamposLivresItens(sprValue);
        } catch (Exception e){
            interromper(e.getMessage())
        }
    }
    private void alterarCamposLivresItens(TableMap tm){
        try {
            MSpread sprEaa0103s = getComponente("sprEaa0103s");
            if(sprEaa0103s.getValue().size() == 0) throw new ValidacaoException("Não há itens na spread.");

            for(Eaa0103 eaa0103 in sprEaa0103s.getValue()){
                TableMap jsonEaa0103 = eaa0103.eaa0103json != null ? eaa0103.eaa0103json : new TableMap();
                if(tm != null && tm.size() > 0){
                    jsonEaa0103.put("id_representante",tm.getLong("id_representante"));
                    jsonEaa0103.put("cod_representante",tm.getString("cod_representante"));
                    jsonEaa0103.put("nome_representante",tm.getString("nome_representante"));
                }

                eaa0103.setEaa0103json(jsonEaa0103);
            }

            sprEaa0103s.refreshAll();
            exibirInformacao("Itens atualizados com sucesso!")
        } catch (Exception e) {
            throw new ValidacaoException(e.getMessage())
        }
    }
    private void desativarBotoes(){
        JButton btnIncluirNovoEnderecoEntregaEntidade = getComponente("btnIncluirNovoEnderecoEntregaEntidade");

        desativarBotao(btnIncluirNovoEnderecoEntregaEntidade)
    }
    private void desativarBotao(JButton btn){
        btn.setEnabled(false);
    }
    private void adicionarEventoTabelaPreco(){
        MNavigation nvgAbe40codigo = getComponente("nvgAbe40codigo");
        nvgAbe40codigo.addFocusListener(new FocusListener() {
            @Override
            void focusGained(FocusEvent e) {}

            @Override
            void focusLost(FocusEvent e) {
                recalcularItens();
            }
        })
    }
    private void recalcularItens(){
        MSpread sprEaa0103s = getComponente("sprEaa0103s");
        java.util.List<Eaa0103> eaa0103s = sprEaa0103s.getValue();
        if(eaa0103s.size() == 0) return;
        if(exibirQuestao("Deseja recalcular os itens?")){
            JButton btnRecalcularDocumento = getComponente("btnRecalcularDocumento");

            for (eaa0103 in eaa0103s){
                eaa0103.eaa0103unit = new BigDecimal(0);
            }
            sprEaa0103s.refreshAll();
            btnRecalcularDocumento.doClick();
        }

    }
    protected void adicionaEventoESC(ActionEvent evt) {
        if(!exibirQuestao("Deseja realmente sair?")) interromper("Por favor salvar antes de SAIR.");
    }
    protected void novoWindowLoad(){
        this.windowLoadOriginal.run();

        def ctrAbb01ent = getComponente("ctrAbb01ent");
        def ctrEaa0103item = getComponente("ctrEaa0103item");

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

        ctrEaa0103item.f4Columns = () ->{
            java.util.List<UiSqlColumn> uiSqlColumn = new ArrayList<>();
            UiSqlColumn abm01codigo = new UiSqlColumn("abm01codigo", "abm01codigo", "Código", 20);
            UiSqlColumn abm01gtin = new UiSqlColumn("abm01gtin", "abm01gtin", "GTIN", 20);
            UiSqlColumn abm01descr = new UiSqlColumn("abm01descr", "abm01descr", "Descrição", 120);
            UiSqlColumn abm01complem = new UiSqlColumn("abm01complem", "abm01complem", "Fabricante", 255);
            uiSqlColumn.addAll(Arrays.asList(abm01codigo,abm01gtin, abm01descr, abm01complem));
            return uiSqlColumn;
        }

    }
    private void reordenarColunas(){
        MSpread sprEaa0103s = getComponente("sprEaa0103s")
        sprEaa0103s.getColumnIndex("eaa0103descr") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103descr"), 3) : null;
        sprEaa0103s.getColumnIndex("eaa0103umu.aam06codigo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103umu.aam06codigo"), 4) : null;
        sprEaa0103s.getColumnIndex("eaa0103umComl.aam06codigo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103umComl.aam06codigo"), 5) : null;
        sprEaa0103s.getColumnIndex("eaa0103qtComl") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103qtComl"), 6) : null;
        sprEaa0103s.getColumnIndex("eaa0103unit") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103unit"), 7) : null;
        sprEaa0103s.getColumnIndex("eaa0103total") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103total"), 8) : null;
        sprEaa0103s.getColumnIndex("eaa0103totDoc") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103totDoc"), 9) : null;
        sprEaa0103s.getColumnIndex("eaa0103totFinanc") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103totFinanc"), 10) : null;
        sprEaa0103s.getColumnIndex("eaa0103entrega") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103entrega"), 11) : null;
        sprEaa0103s.getColumnIndex("eaa0103ncm.abg01codigo") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103ncm.abg01codigo"), 12) : null;
        sprEaa0103s.getColumnIndex("eaa0103ncm.abg01descr") != -1 ? sprEaa0103s.moveColumn(sprEaa0103s.getColumnIndex("eaa0103ncm.abg01descr"), 13) : null;
    }
    private void adicionaEventoPCD(){
        MNavigation nvgAbd01codigo = getComponente("nvgAbd01codigo");

        nvgAbd01codigo.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {};

            public void focusLost(FocusEvent e) {
                MNavigation nvgAbe01codigo = getComponente("nvgAbe01codigo");
                String codEntidade = nvgAbe01codigo.getValue();

                if(codEntidade != null) {
                    Long idEntidade = buscarIdEntidade(codEntidade);
                    String msgEnt = buscarMensagemEntidade(idEntidade);

                    // Exibe caixa de dialogo na tela com a mensagem do cadastro da entidade
                    if (msgEnt != null && msgEnt != "") exibirTelaDeAtencaoComMensagemEntidade(msgEnt);

                    // Busca os titulos vencidos da entidade
                    buscarTitulosVencidosEntidade(idEntidade);

                }
            }
        });
    }
    private String buscarMensagemEntidade(Long idEntidade){
        String sql = "SELECT abe02obsUsoInt FROM abe02 WHERE abe02ent = " + idEntidade.toString();

        TableMap tmEntidade =  executarConsulta(sql)[0]

        return tmEntidade.getString("abe02obsUsoInt");
    }

    private void exibirTelaDeAtencaoComMensagemEntidade(String msg){

        String caminhoImagem = "H:/Sam4/imagens/atencao.png";

        // Caixa de Mensagem
        JDialog dialog = new JDialog((Frame) null, "Mensagem de Alerta", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        // Painel principal
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(8, 8, 8, 8)); // margem externa
        dialog.setContentPane(content);

        // --- Top: título centralizado em vermelho
        JLabel titulo = new JLabel("*** A T E N Ç Ã O ***", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 25));
        titulo.setForeground(Color.RED);

        // Painel para manter título com pequena margem
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titulo, BorderLayout.CENTER);
        content.add(topPanel, BorderLayout.NORTH);

        // --- Centro: área de texto não-editável dentro de um JScrollPane
        JTextArea txt = new JTextArea();
        txt.setText(msg); // texto inicial
        txt.setEditable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setFont(new Font("Arial", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JScrollPane scroll = new JScrollPane(txt);
        scroll.setPreferredSize(new Dimension(620, 260));
        content.add(scroll, BorderLayout.CENTER);

        // --- Sul: imagem + botão OK (imagem acima do botão)
        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Carrega imagem
//        String imagemPath = caminhoImagem;
//        JLabel imagemLabel = new JLabel();
//        imagemLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // centralizar horizontalmente
//        try {
//            BufferedImage img = ImageIO.read(new File(imagemPath));
//            // opcional: redimensionar mantendo proporção para largura fixa
//            int targetWidth = 220;
//            int w = img.getWidth();
//            int h = img.getHeight();
//            int targetHeight = (targetWidth * h) / w;
//            Image scaled = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
//            imagemLabel.setIcon(new ImageIcon(scaled));
//        } catch (IOException e) {
//            // se não encontrar, mostra texto substituto
//            imagemLabel.setText("Imagem não encontrada: " + imagemPath);
//            imagemLabel.setFont(new Font("Arial", Font.ITALIC, 12));
//        }
//        south.add(imagemLabel);
//        south.add(Box.createRigidArea(new Dimension(0, 8)));

        // Botão OK centralizado
        JButton btnOk = new JButton("OK");
        btnOk.setFont(new Font("Arial", Font.BOLD, 16));
        btnOk.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOk.setPreferredSize(new Dimension(120, 28));
        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        south.add(btnOk);

        content.add(south, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(null); // centraliza na tela
        dialog.setVisible(true);
    }
    private Long buscarIdEntidade(String codEntidade){
        String sql = "SELECT abe01id FROM abe01 WHERE abe01codigo = '" + codEntidade + "' AND abe01gc = 1075797" //+ idEmpresa.toString();
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
                    .param("name", "Silcon.servlet.Buscar_Titulos_Vencidos_Entidade")
                    .header("ignore-body-decrypt", "true")
                    .parseBody(body)
                    .success((response) -> {
                        Boolean contemTituloVencido = response.parseResponse(new TypeReference<Boolean>(){});
                        if(contemTituloVencido && !exibirQuestao("Constam títulos vencidos para esse cliente, necessário consultar financeiro. Deseja continuar?")){
                            throw new ValidacaoException("Operação Cancelada.")
                        }else{
                            if(contemTituloVencido) exibirInformacao("Insira uma observação, caso necessário.");
                            verificarLimiteDeCredito(idEntidade)
                        }
                    })
                    .post();
        }catch(Exception err){
            throw new ValidacaoException(err.getMessage());
        }
    }
    private void verificarLimiteDeCredito(Long idEntidade){
        TableMap body = new TableMap()
        body.put("abe01id",idEntidade)

        WorkerRequest.create(tarefa.getWindow())
                .initialText("Verificando Limite de Crédito")
                .dialogVisible(false)
                .controllerEndPoint("servlet")
                .methodEndPoint("run")
                .param("name", "Silcon.servlet.Verificar_Limite_Credito_Entidade")
                .header("ignore-body-decrypt", "true")
                .parseBody(body)
                .success((response) -> {
                    Boolean limiteCreditoExcedido = response.parseResponse(new TypeReference<Boolean>(){});
                    if(limiteCreditoExcedido && !exibirQuestao("Limite de crédito ultrapassado, necessario consultar o financeiro. Deseja continuar?")){
                        throw new ValidacaoException("Operação Cancelada.")
                    }else{
                        if(limiteCreditoExcedido) exibirInformacao("Insira uma observação, caso necessário.")
                    }
                })
                .post();
    }
    private void adicionaBotaoImprimirDanfe(){
        JPanel panel7 = getComponente("panel7");
        def tela = tarefa.getWindow();
        tela.setBounds((int) tela.getBounds().x, (int) tela.getBounds().y, (int) tela.getBounds().width, (int) tela.getBounds().height + 40);

        def btnImprimir = new JButton();
        btnImprimir.setText("Imprimir");

        // X    Y    W  H
        btnImprimir.setBounds(110, 100, 160, 25);
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
    private void criarCampoNumeroVias(){
        JPanel panel7 = getComponente("panel7");


        JLabel labelNumVias = new JLabel();
        labelNumVias.setText("Vias");
        labelNumVias.setBounds(10, 105, 64, 15);

        txtVias.setModel(new SpinnerNumberModel(1, 1, 9, 1));
        txtVias.setBounds(50, 100, 45, 25);

        panel7.add(labelNumVias);
        panel7.add(txtVias);
    }
    private void btnImprimirPressed() {
        try {
            TableMap documento = verificarDocumentoSalvo();
            MNavigation nvgAah01codigo = getComponente("nvgAah01codigo");
            String codTipoDoc = nvgAah01codigo.getValue();

            if(documento.isEmpty()) interromper("Necessário salvar o documento antes de imprimir.");

            Long idDocumento = documento.getLong("eaa01id");

            if(!codTipoDoc.equals("04") && !codTipoDoc.equals("16")){
                WorkerSupplier.create(this.tarefa.getWindow(), {
                    return buscarDadosImpressao(idDocumento, codTipoDoc);
                })
                        .initialText("Imprimindo Documento")
                        .dialogVisible(true)
                        .success({ bytes ->
                            enviarDadosParaImpressao(bytes);
                        })
                        .start();
            }else{
                abrirTelaImpressaoDocs(codTipoDoc, idDocumento);
            }
        } catch (Exception err) {
            ErrorDialog.defaultCatch(this.tarefa.getWindow(), err);
        }
    }
    private TableMap verificarDocumentoSalvo(){
        def txtAbb01num = getComponente("txtAbb01num");
        def nvgAah01codigo = getComponente("nvgAah01codigo");

        Integer numDoc = txtAbb01num.getValue();
        String tipoDoc = nvgAah01codigo.getValue();
        Long idEmpresa = obterEmpresaAtiva().getAac10id();

        return executarConsulta("SELECT eaa01id "+
                " FROM eaa01 "+
                "INNER JOIN abb01 on abb01id = eaa01central "+
                "INNER JOIN abd01 on abd01id = eaa01pcd "+
                "INNER JOIN aah01 ON aah01id = abb01tipo "+
                "WHERE abd01es = 1 AND abd01aplic = 1 "+
                "AND aah01codigo = '"+ tipoDoc + "' "+
                "AND abb01num = "+numDoc+ " "+
                "AND eaa01eg = "+idEmpresa);
    }
    private abrirTelaImpressaoDocs(String codTipoDoc, Long idDocumento){
        try{
            TableMap tmTipoDoc = buscarInformacoesTipoDoc(codTipoDoc);
            Long idTipoDoc = tmTipoDoc.getLong("aah01id");
            MTextFieldInteger txtAbb01num = getComponente("txtAbb01num");
            MTextFieldString txtEaa01nfeChave = getComponente("txtEaa01nfeChave");
            Integer numDoc = txtAbb01num.getValue();
            String protocolo = txtEaa01nfeChave.getValue();
            Boolean somenteComProtocolo = !(protocolo == null || protocolo.isEmpty());
            Integer statusImpressao = buscarStatusImpressao(idDocumento);
            Boolean rdoReimpressao = statusImpressao == 2;

            SRF1009 srf1009 = new SRF1009();
            WindowUtils.createJDialog(srf1009.getWindow(), srf1009);
            srf1009.cancelar = () -> srf1009.dispose();
            srf1009.chkDocumento.setValue(1);
            srf1009.ctrAah01.setIdValue(idTipoDoc);
            srf1009.txtNumInicialDoc.setValue(numDoc);
            srf1009.txtNumFinalDoc.setValue(numDoc);
            srf1009.chkSomenteDocumentoComProtocolo.setSelected(somenteComProtocolo);
            srf1009.rdoReimpressao.setSelected(rdoReimpressao);
            srf1009.btnMostrar.doClick()
            srf1009.getWindow().setVisible(true);
        }catch(Exception e) {
            throw new ValidacaoException("Falha ao abrir tarefa de imprimir documentos: " + e.getMessage());
        }
    }
    private byte[] buscarDadosImpressao(Long idDocumento, String codTipoDoc) {
        MNavigation nvgAbd01codigo = getComponente("nvgAbd01codigo");
        TableMap tmTipoDoc = buscarInformacoesTipoDoc(codTipoDoc);
        String caminhoRelatorio = nvgAbd01codigo.getValue() != "60026" ? tmTipoDoc.getString("aah01formRelDoc") : "Silcon.relatorios.srf.SRF_Impressao_Documento_Interno_S_Desc";
        if(caminhoRelatorio == null || caminhoRelatorio.isEmpty()) throw new ValidacaoException("Não foi encontrado relatório de impressão no tipo de documento " + codTipoDoc);

        String json = "{\"nome\":\""+caminhoRelatorio+"\",\"filtros\":{\"eaa01id\":"+idDocumento+"}}"

        ObjectMapper mapper = new ObjectMapper();
        JsonNode obj = mapper.readTree(json);
        return HttpRequest.create().controllerEndPoint("relatorio").methodEndPoint("gerarRelatorio").parseBody(obj).post().getResponseBody()
    }
    private TableMap buscarInformacoesTipoDoc(String codTipoDoc){
        String sql = "SELECT aah01id, aah01formRelDoc FROM aah01 WHERE aah01codigo = '" + codTipoDoc + "'";

        TableMap tmTipoDoc = executarConsulta(sql)[0];

        if(tmTipoDoc.size() == 0) throw new ValidacaoException("Não foi encontrado registros para o tipo de documento " + codTipoDoc);

        return tmTipoDoc;
    }
    private buscarStatusImpressao(Long idDocumento){
        String sql = "SELECT eaa01statusImpr FROM eaa01 WHERE eaa01id = " + idDocumento;
        TableMap tmDoc = executarConsulta(sql)[0];
        Integer statusImpressao = tmDoc.getInteger("eaa01statusImpr");

        return statusImpressao;
    }
    protected void enviarDadosParaImpressao(byte[] bytes) {
        try {
            Integer numVias = Integer.parseInt(txtVias.getValue().toString())
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
                    job.setCopies(numVias);
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
        MNavigation nvgAbd01codigo = getComponente("nvgAbd01codigo");
        String codPcd = nvgAbd01codigo.getValue();

        if(codPcd == "60000" && obterUsuarioLogado().getAab10id() == 1904684) interromper("O PCD utilizado está incorreto para esse usuário.")

    }

    @Override
    public void posSalvar(Long id) {
        def doc = executarConsulta(" SELECT abb01num FROM Eaa01 " +
                " INNER JOIN Abb01 ON abb01id = eaa01central " +
                " WHERE eaa01id = " + id + " " +
                obterWherePadrao("Eaa01"));

        if(doc != null && doc.size() > 0){
            def num = doc.get(0).getInteger("abb01num");
            Messages.create(listaDoCadastro.getWindow()).text("Documento " + num + " salvo com sucesso.").autoCloseAfter(5000).success();
        }
    }
}