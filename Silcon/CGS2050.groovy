import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.http.HttpRequest
import com.amazonaws.protocol.json.internal.JsonMarshaller
import multitec.swing.components.autocomplete.MNavigation
import multitec.swing.components.autocomplete.MNavigationController
import multitec.swing.core.MultitecRootPanel
import multitec.swing.core.utils.WindowUtils
import org.apache.axis2.i18n.Messages
import sam.dto.cgs.CGS2050CashbackDto
import sam.swing.VariaveisDaSessao
import sam.swing.tarefas.scf.SCF4001

import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.awt.Point;
import multitec.swing.components.textfields.MTextFieldBigDecimal;
import javax.swing.JPanel
import java.awt.event.ActionEvent
import java.awt.event.ActionListener;
import multitec.swing.components.spread.MSpread;
import multitec.swing.components.spread.MSpread
import multitec.swing.core.MultitecRootPanel
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import java.awt.event.ActionListener
import sam.dto.cgs.CGS2050DocumentoSCFDto
import multitec.swing.core.dialogs.Messages;
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import br.com.multitec.utils.UiSqlColumn;


public class Script extends sam.swing.ScriptBase{
    MTextFieldBigDecimal txtTotalDesconto = new MTextFieldBigDecimal();
    MTextFieldBigDecimal txtTotalMulta = new MTextFieldBigDecimal();
    MTextFieldBigDecimal txtTotalJuros = new MTextFieldBigDecimal();
    MTextFieldBigDecimal txtTotalEncargos = new MTextFieldBigDecimal();
    MTextFieldBigDecimal txtTotalGeral = new MTextFieldBigDecimal();
    JLabel lblTotalGeral = new JLabel();
    private boolean preenchendoSpread = false;
    public Runnable windowLoadOriginal;
    MultitecRootPanel tarefa;
    Boolean isAcessarTarefaSCF4001 = isAcessarTarefa("SCF4001");

    @Override
    public void execute(MultitecRootPanel tarefa) {
        this.tarefa = tarefa;
        reordenarColunas();
        adicionarEventoBtnAReceber();
        adicionarEventoBtnAPagar();
        adicionarEventoBtnRecebidos()
        adicionarEventoBtnPagos();
        adicionarEventoBtnPrevisaoAPagar();
        adicionarEventoBtnPrevisaoAReceber();
        alterarPosicoesComponentes();
        adicionarEventoSpreads();
        criarComponentes();
        adicionarLegendaCaschback();

        this.windowLoadOriginal = tarefa.windowLoad ;
        tarefa.windowLoad = {novoWindowLoad()};
    }
    private void adicionarLegendaCaschback(){
        JPanel pnlCashback = getComponente("pnlCashback");
        JLabel lblDuploCliqueCashback = new JLabel();
        lblDuploCliqueCashback.setText("<html><b>Duplo Clique Para Abrir Cashback Selecionado.</b><html>");
        lblDuploCliqueCashback.setHorizontalAlignment(2);
        lblDuploCliqueCashback.setBounds(500, 200, 355, 15);

        JLabel lblTotalizar = new JLabel();
        lblTotalizar.setText("<html><b>[T] Totalizar Cashback</b><html>")
        lblTotalizar.setHorizontalAlignment(2);
        lblTotalizar.setBounds(300, 200, 355, 15);

        pnlCashback.add(lblDuploCliqueCashback);
        pnlCashback.add(lblTotalizar);

    }
    private void reordenarColunas(){
        MSpread sprDocsFin = getComponente("sprDocsFin");
        sprDocsFin.getColumnIndex("documento") != -1 ? sprDocsFin.moveColumn(sprDocsFin.getColumnIndex("documento"), 0) : null;
        sprDocsFin.getColumnIndex("numero") != -1 ? sprDocsFin.moveColumn(sprDocsFin.getColumnIndex("numero"), 1) : null;
        sprDocsFin.getColumnIndex("parcela") != -1 ? sprDocsFin.moveColumn(sprDocsFin.getColumnIndex("parcela"), 2) : null;
        sprDocsFin.getColumnIndex("serie") != -1 ? sprDocsFin.moveColumn(sprDocsFin.getColumnIndex("serie"), 3) : null;
        sprDocsFin.getColumnIndex("vencimento") != -1 ? sprDocsFin.moveColumn(sprDocsFin.getColumnIndex("vencimento"), 4) : null;
        sprDocsFin.getColumnIndex("daa01json.dias") != -1 ? sprDocsFin.moveColumn(sprDocsFin.getColumnIndex("daa01json.dias"), 5) : null;
        sprDocsFin.getColumnIndex("valor") != -1 ? sprDocsFin.moveColumn(sprDocsFin.getColumnIndex("valor"), 6) : null;
        sprDocsFin.getColumnIndex("daa01json.juros") != -1 ? sprDocsFin.moveColumn(sprDocsFin.getColumnIndex("daa01json.juros"), 7) : null;
        sprDocsFin.getColumnIndex("data") != -1 ? sprDocsFin.moveColumn(sprDocsFin.getColumnIndex("data"), 8) : null;

//        sprDocsFin.getColumnIndex("daa01json.dias") != -1 ? sprDocsFin.moveColumn(sprDocsFin.getColumnIndex("daa01json.dias"), 7) : null;
//        sprDocsFin.getColumnIndex("daa01json.desconto") != -1 ? sprDocsFin.moveColumn(sprDocsFin.getColumnIndex("daa01json.desconto"), 8) : null;
//        sprDocsFin.getColumnIndex("daa01json.multa") != -1 ? sprDocsFin.moveColumn(sprDocsFin.getColumnIndex("daa01json.multa"), 9) : null;
//        sprDocsFin.getColumnIndex("daa01json.juros") != -1 ? sprDocsFin.moveColumn(sprDocsFin.getColumnIndex("daa01json.juros"), 10) : null;
//        sprDocsFin.getColumnIndex("daa01json.encargos") != -1 ? sprDocsFin.moveColumn(sprDocsFin.getColumnIndex("daa01json.encargos"), 11) : null;
    }
    private void adicionarEventoBtnAReceber(){
        JButton btnAReceber = getComponente("btnAReceber");
        btnAReceber.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                preenchendoSpread = true;
                zerarCamposCustomizados()
            }
        });
    }
    private void adicionarEventoBtnAPagar(){
        JButton btnAPagar = getComponente("btnAPagar");
        btnAPagar.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                preenchendoSpread = true;
                zerarCamposCustomizados()
            }
        });
    }
    private void adicionarEventoBtnRecebidos(){
        JButton btnRecebidos = getComponente("btnRecebidos");
        btnRecebidos.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                preenchendoSpread = true;
                zerarCamposCustomizados()
            }
        });
    }
    private void adicionarEventoBtnPagos(){
        JButton btnPagos = getComponente("btnPagos");
        btnPagos.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                preenchendoSpread = true;
                zerarCamposCustomizados()
            }
        });
    }
    private void adicionarEventoBtnPrevisaoAPagar(){
        JButton btnPrevisaoPagar = getComponente("btnPrevisaoPagar");
        btnPrevisaoPagar.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                preenchendoSpread = true;
                zerarCamposCustomizados()
            }
        });
    }
    private void adicionarEventoBtnPrevisaoAReceber(){
        JButton btnPrevisaoReceber = getComponente("btnPrevisaoReceber");
        btnPrevisaoReceber.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                preenchendoSpread = true;
                zerarCamposCustomizados()
            }
        });
    }
    private void alterarPosicoesComponentes(){
        JLabel lblTotalFinanceiro = getComponente("lblTotalFinanceiro");
        JLabel lblTotalValorFinanceiro = getComponente("lblTotalValorFinanceiro");
        MTextFieldBigDecimal txtTotalValorFinanceiro = getComponente("txtTotalValorFinanceiro");
        MTextFieldBigDecimal txtTotalValorPagoFinanceiro = getComponente("txtTotalValorPagoFinanceiro");
        JLabel lblTotalValorPagoFinanceiro = getComponente("lblTotalValorPagoFinanceiro");

        lblTotalFinanceiro.setBounds(new Rectangle(new Point(250, 536), lblTotalFinanceiro.getPreferredSize()));
        lblTotalValorFinanceiro.setBounds(300, 536, (lblTotalValorFinanceiro.getPreferredSize()).width as int, 14);
        txtTotalValorFinanceiro.setBounds(345, 525, 124, (txtTotalValorFinanceiro.getPreferredSize()).height as int);
        txtTotalValorPagoFinanceiro.setBounds(1200, 525, 124, (txtTotalValorPagoFinanceiro.getPreferredSize()).height as int);
        lblTotalValorPagoFinanceiro.setBounds(1140, 536, (lblTotalValorPagoFinanceiro.getPreferredSize()).width as int, 14);
        lblTotalFinanceiro.setVisible(false)
    }

    private void criarComponentes(){
        JPanel pnlDocsFinanceiros = getComponente("pnlDocsFinanceiros");

        // Lbl Descontos
        txtTotalDesconto.setBounds(470, 525, 100, (txtTotalDesconto.getPreferredSize()).height as int);
        txtTotalDesconto.setMaxValue(new BigDecimal("99999999999999.99"));
        txtTotalDesconto.setEditable(false);
        txtTotalDesconto.setEnabled(false);

        // Lbl Multa
        txtTotalMulta.setBounds(570, 525, 100, (txtTotalMulta.getPreferredSize()).height as int);
        txtTotalMulta.setMaxValue(new BigDecimal("99999999999999.99"));
        txtTotalMulta.setEditable(false);
        txtTotalMulta.setEnabled(false);

        // Lbl Juros
        txtTotalJuros.setBounds(670, 525, 100, (txtTotalJuros.getPreferredSize()).height as int);
        txtTotalJuros.setMaxValue(new BigDecimal("99999999999999.99"));
        txtTotalJuros.setEditable(false);
        txtTotalJuros.setEnabled(false);

        // Lbl Encargos
        txtTotalEncargos.setBounds(770, 525, 100, (txtTotalEncargos.getPreferredSize()).height as int);
        txtTotalEncargos.setMaxValue(new BigDecimal("99999999999999.99"));
        txtTotalEncargos.setEditable(false);
        txtTotalEncargos.setEnabled(false);

        // Lbl Txt Total Geral
        lblTotalGeral.setText("Total Geral");
        lblTotalGeral.setBounds(870, 536, (lblTotalGeral.getPreferredSize()).width as int, 14);

        // Txt Total Geral
        txtTotalGeral.setBounds(930, 525, 100, (txtTotalGeral.getPreferredSize()).height as int);
        txtTotalGeral.setMaxValue(new BigDecimal("99999999999999.99"));
        txtTotalGeral.setEditable(false);
        txtTotalGeral.setEnabled(false);

        pnlDocsFinanceiros.add(txtTotalDesconto);
        pnlDocsFinanceiros.add(txtTotalMulta);
        pnlDocsFinanceiros.add(txtTotalJuros);
        pnlDocsFinanceiros.add(txtTotalEncargos);
        pnlDocsFinanceiros.add(lblTotalGeral);
        pnlDocsFinanceiros.add(txtTotalGeral);
    }
    private void adicionarEventoSpreads(){
        adicionarEventoSpreadDocsFin();
        adicionarEventoSpreadCashback();
    }
    private void adicionarEventoSpreadDocsFin(){
        MSpread sprDocsFin = getComponente("sprDocsFin");

        sprDocsFin.getModel().addTableModelListener(e -> {

            if(e.getType() != TableModelEvent.UPDATE) return;

            // Não executa quando ordena a spread
            if(!preenchendoSpread) return;

            callback();
        });
    }
    private void adicionarEventoSpreadCashback(){
        try{
            MSpread sprCashbacks = getComponente("sprCashbacks");
            MNavigationController ctrAbe01 = getComponente("ctrAbe01");

            try {
                sprCashbacks.addKeyListener(new KeyAdapter() {
                    @Override
                    void keyPressed(KeyEvent e) {
                        if(e.getKeyCode() == 84 && sprCashbacks.getValue().size() > 0){
                            totalizarCashback();
                        }
                    }
                })
            } catch (Exception e) {
                throw new ValidacaoException("Falha ao totalizar cashback: " + e.getMessage());
            }


            try {
                sprCashbacks.addMouseListener(new MouseAdapter() {
                    @Override
                    void mouseClicked(MouseEvent e) {
                        if(e.getClickCount() == 2 && sprCashbacks.getSelectedRow() >= 0){
                            int row = sprCashbacks.getSelectedRow();
                            Long idCashback = buscarIdCashbackSelecionado(ctrAbe01.getValue().getAbe01id(), sprCashbacks.get(row));
                            if(idCashback == null) return;
                            if(!isAcessarTarefaSCF4001) throw new ValidacaoException("O usuário não tem permissão para acessar essa tarefa.")
                            abrirTarefaSCF4001(idCashback);
                        }
                    }
                })
            } catch (Exception e){
                throw new ValidacaoException(e.getMessage());
            }
        } catch (Exception e) {
            throw new ValidacaoException(e.getMessage())
        }
    }
    private void callback(){
        MSpread sprDocsFin = getComponente("sprDocsFin");
        int size = sprDocsFin.getValue().size();
        if(size > 0) somarCamposLivresSpread(sprDocsFin.getValue())
    }
    private void somarCamposLivresSpread(List<CGS2050DocumentoSCFDto> vlrSpread){

        LocalDate dtAtual = LocalDate.now();
        BigDecimal totDesconto = BigDecimal.ZERO;
        BigDecimal totMulta = BigDecimal.ZERO;
        BigDecimal totJuros = BigDecimal.ZERO;
        BigDecimal totEncargos = BigDecimal.ZERO;
        BigDecimal totDocs = BigDecimal.ZERO;

        for(int i = 0; i < vlrSpread.size(); i++ ){
            TableMap jsonDaa01 = vlrSpread.get(i).getDaa01json() != null ? vlrSpread.get(i).getDaa01json() : new TableMap();
            LocalDate dtVctoN =  vlrSpread.get(i).getVencimento();
            LocalDate dtPagamento = vlrSpread.get(i).getPagamento();
            Integer diasAtraso = dtPagamento == null ? ChronoUnit.DAYS.between(dtAtual, dtVctoN) : ChronoUnit.DAYS.between(dtPagamento, dtVctoN) ;
            diasAtraso < 0 ? jsonDaa01.put("juros", jsonDaa01.getBigDecimal_Zero("juros") * diasAtraso.abs()) : jsonDaa01.put("juros", BigDecimal.ZERO)
            totDesconto += jsonDaa01.getBigDecimal_Zero("desconto");
            totMulta += jsonDaa01.getBigDecimal_Zero("multa");
            totJuros += jsonDaa01.getBigDecimal_Zero("juros");
            totEncargos += jsonDaa01.getBigDecimal_Zero("encargos");
            totDocs += vlrSpread.get(i).getValor();

            jsonDaa01.put("dias", diasAtraso);
            vlrSpread.get(i).setDaa01json(jsonDaa01);
        }

        BigDecimal totGeral = totDocs + totMulta + totJuros + totEncargos - totDesconto.abs();

        txtTotalDesconto.setValue(totDesconto.abs().round(2));
        txtTotalMulta.setValue(totMulta.round(2));
        txtTotalJuros.setValue(totJuros.round(2));
        txtTotalEncargos.setValue(totEncargos.round(2));
        txtTotalGeral.setValue(totGeral.round(2));

        preenchendoSpread = false
    }
    private void zerarCamposCustomizados(){
        txtTotalDesconto.setValue(BigDecimal.ZERO)
        txtTotalMulta.setValue(BigDecimal.ZERO)
        txtTotalJuros.setValue(BigDecimal.ZERO)
        txtTotalEncargos.setValue(BigDecimal.ZERO)
        txtTotalGeral.setValue(BigDecimal.ZERO)
    }
    protected void novoWindowLoad() {
        this.windowLoadOriginal.run();

        def ctrAbe01 = getComponente("ctrAbe01");

        ctrAbe01.f4Columns = () -> {
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
    private void totalizarCashback(){
        MSpread sprCashbacks = getComponente("sprCashbacks");

        BigDecimal total = new BigDecimal(0);

        for(int i = 0; i < sprCashbacks.getValue().size(); i++){
            total = total.add(sprCashbacks.get(i).getSaldo())
        }

        Messages.create(tarefa.getWindow()).text("Soma dos saldos de cashback: " + total).success();
    }
    private Long buscarIdCashbackSelecionado(Long idEntidade, CGS2050CashbackDto cgs2050CashbackDto){
        String tipo = cgs2050CashbackDto.tipo;
        BigDecimal saldo = cgs2050CashbackDto.saldo;
        String nome = cgs2050CashbackDto.nome
        String codTipo = tipo.trim().split("-")[0]
        Integer propriedade = cgs2050CashbackDto.proprioTerceiro.toUpperCase() == "PRÓPRIO" ? 0 : 1;


        String sql = "SELECT dad01id " +
                    " FROM dad01 " +
                    " INNER JOIN abf30 ON abf30id = dad01tipo "+
                    " WHERE abf30codigo = '" + codTipo.trim() + "' "+
                    " AND dad01saldo = " + saldo +
                    " AND dad01nome = '" + nome + "' " +
                    " AND dad01ent = " + idEntidade +
                    " AND dad01prop = " + propriedade +
                    " LIMIT 1 ";

        TableMap tmCashback = executarConsulta(sql)[0];

        return tmCashback != null ? tmCashback.getLong("dad01id") : null;
    }
    private void abrirTarefaSCF4001(Long idCashback){
        try {
            SCF4001 scf4001 = new SCF4001();
            WindowUtils.createJDialog(scf4001.getWindow(), scf4001);
            scf4001.cancelar = () -> scf4001.getWindow().dispose();
            scf4001.exibirPanelListaCadastro = () -> scf4001.getWindow().dispose();
            scf4001.editar(idCashback);
            scf4001.getWindow().setVisible(true);
        } catch (Exception e){
            throw new ValidacaoException("Falha ao abrir tarefa SCF4001: " + e.getMessage());
        }

    }
    protected boolean isAcessarTarefa(String tarefa) {
        return (Boolean) HttpRequest.create().controllerEndPoint("cas0103").methodEndPoint("verificarSeUsuarioTemAcessoTarefa").param("aab10id", VariaveisDaSessao.getInstance().getAab10().getIdValue()).param("tarefa", tarefa).get().parseResponse(Boolean.class);
    }


}