import multitec.swing.components.autocomplete.MNavigation
import multitec.swing.core.MultitecRootPanel;

public class Script extends sam.swing.ScriptBase{
    @Override
    public void execute(MultitecRootPanel tarefa) {
        MNavigation nvgAbm20codigoSaidaAlternativo = getComponente("nvgAbm20codigoSaidaAlternativo");
        nvgAbm20codigoSaidaAlternativo.getNavigationController().setIdValue(4424661);
    }
}