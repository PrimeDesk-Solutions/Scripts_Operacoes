import multitec.swing.components.autocomplete.MNavigation
import multitec.swing.components.autocomplete.MNavigationController
import multitec.swing.core.MultitecRootPanel

import javax.swing.JTabbedPane;

public class Script extends sam.swing.ScriptBase{
    @Override
    public void execute(MultitecRootPanel tarefa) {
        JTabbedPane tabFiltros = getComponente("tabFiltros");
        tabFiltros.addChangeListener(e -> {
            int index = tabFiltros.getSelectedIndex();
            if(index == 2){ // Aba inserir item
                MNavigation nvgAam04codigo = getComponente("nvgAam04codigo");
                MNavigation nvgAbm15nome0 = getComponente("nvgAbm15nome0");
                nvgAam04codigo.getNavigationController().setIdValue(4224);
                nvgAbm15nome0.getNavigationController().setIdValue(128987);
            }
        })
    }
}