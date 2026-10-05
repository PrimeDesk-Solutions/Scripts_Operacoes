import multitec.swing.core.MultitecRootPanel;
import javax.swing.JButton;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.event.*;
import java.lang.Exception;
import multitec.swing.core.dialogs.ErrorDialog;
import javax.swing.*;
import sam.swing.tarefas.sce.SCE1503;
import multitec.swing.core.utils.WindowUtils;
import multitec.swing.components.textfields.MTextFieldInteger;

public class Script extends sam.swing.ScriptBase{
    private MultitecRootPanel tarefa;
    private MTextFieldInteger txtAbb01numPpInicial;

    @Override
    public void execute(MultitecRootPanel tarefa) {
        this.tarefa = tarefa;
        def tela = tarefa.getWindow()

        tela.setBounds((int) tela.getBounds().x, (int) tela.getBounds().y, (int) tela.getBounds().width, (int) tela.getBounds().height + 40);
        def btnDemanda = new JButton();
        btnDemanda.setText("Demanda de Estoque");
        tarefa.add(btnDemanda);
        btnDemanda.setBounds(850,55, 170, 30);

        btnDemanda.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectionButtonPressed();
            }
        })

    }

    private void selectionButtonPressed(){
        MTextFieldInteger txtAbb01num = getComponente("txtAbb01num");
        Integer numPlano = txtAbb01num.getValue()

        try{

            SCE1503 sce1503 = new SCE1503();
            WindowUtils.createJDialog(sce1503.getWindow(), sce1503);
            sce1503.chkNumeroPlanoProducao.setSelected(true);
            sce1503.rdoEstoqueInicialNaoConsiderar.setSelected(true);
            sce1503.txtAbb01numPpInicial.setValue(numPlano);
            sce1503.txtAbb01numPpFinal.setValue(numPlano);
            sce1503.rdoEstoqueInicialExibicao.setValue(2);
            sce1503.chkComprasSCV.setValue(1)
            sce1503.btnImportarPlanoProducao.doClick();
            sce1503.open.run();


        }catch (Exception err){
            ErrorDialog.defaultCatch(this.tarefa.getWindow(), err);
        }
    }

    @Override
    public void preSalvar(boolean salvo) {
    }

    @Override
    public void posSalvar(Long id) {
    }
}