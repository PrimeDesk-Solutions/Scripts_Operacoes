import multitec.swing.components.MCheckBox
import multitec.swing.components.autocomplete.MNavigation
import multitec.swing.core.MultitecRootPanel;

public class Script extends sam.swing.ScriptBase{
    @Override
    public void execute(MultitecRootPanel tarefa) {
        String user = obterUsuarioLogado().getAab10user();
        if(user.toUpperCase() == "GISLAINE" || user.toUpperCase() == "LUCIANA") definirValoresDefault();
    }
    private void definirValoresDefault(){
        MNavigation nvgAbf01codigo = getComponente("nvgAbf01codigo");
        MCheckBox chkOperacao = getComponente("chkOperacao");
        MNavigation nvgAbf16codigoIni = getComponente("nvgAbf16codigoIni");
        MNavigation nvgAbf16codigoFim = getComponente("nvgAbf16codigoFim");

        nvgAbf01codigo.getNavigationController().setIdValue(59014730);
        chkOperacao.setValue(1);
        nvgAbf16codigoIni.getNavigationController().setIdValue(149010)
        nvgAbf16codigoFim.getNavigationController().setIdValue(149010)
    }
}