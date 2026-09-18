import multitec.swing.components.spread.MSpread
import multitec.swing.core.MultitecRootPanel
import sam.model.entities.ab.Abb01;
import br.com.multitec.utils.collections.TableMap

import javax.swing.JButton
import javax.swing.SwingUtilities
import javax.swing.Timer
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent


public class Script extends sam.swing.ScriptBase{
    MultitecRootPanel tarefa
    Thread threadDocumentos;

    @Override
    public void execute(MultitecRootPanel tarefa) {
        this.tarefa = tarefa;
        iniciarProcesso();
        //adicionarEventoBtnEnviar();
        onClosed()
    }
    private void iniciarProcesso(){
        Timer timerInicial = new Timer(300, null);

        timerInicial.addActionListener(e -> {
            excluirDocumentos();
            timerInicial.stop();

            iniciarThread();
        })

        timerInicial.setRepeats(false);
        timerInicial.start();
    }
    private void adicionarEventoBtnEnviar(){
        JButton btnEnviar = getComponente("btnEnviar");
        btnEnviar.addActionListener(e -> btnEnviarPressed())
    }
    private void btnEnviarPressed() {
        excluirDocumentos();
    }
    private void iniciarThread(){
        threadDocumentos = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()){
                try{
                    threadDocumentos.sleep(3000L);
                    excluirDocumentos();

                }catch (InterruptedException e){
                    threadDocumentos.interrupt();
                    break;
                }
            }
        });

        threadDocumentos.setDaemon(true);
        threadDocumentos.start();
    }
    private void excluirDocumentos(){
        MSpread sprDocs = getComponente("sprDocs");
        Long idUser = obterUsuarioLogado().getAab10id();

        if(sprDocs.getValue().size() == 0) return;


        for(int i = 0; i < sprDocs.getValue().size(); i++){
            Long idDoc = sprDocs.getValueAt(i, "abb01id")
            TableMap tmUserDoc = executarConsulta("SELECT abb01operUser AS user FROM abb01 WHERE abb01id = " + idDoc)[0];
            Long idUserDoc = tmUserDoc.getLong("user")

            if(idUser != idUserDoc) sprDocs.removeRow(i);
        }

        sprDocs.refreshAll();
    }
    private void onClosed(){
        this.tarefa.getWindow().addWindowListener(new WindowAdapter() {
            @Override
            void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                threadDocumentos.interrupt();
                //exibirInformacao("Thread encerrada.")
            }

            @Override
            void windowClosing(WindowEvent e) {
                super.windowClosing(e)
                threadDocumentos.interrupt();
            }
        });
    }
}