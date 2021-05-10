package de.adito.aditoweb.nbm.tests.actions.create;

import org.openide.awt.*;
import org.openide.util.*;
import org.openide.util.actions.Presenter;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Folder for Tests-Actions in Context Menu
 *
 * @author s.seemann, 05.03.2021
 */
@NbBundle.Messages("CTL_TestsAction=New")
@ActionID(category = "Tests", id = "de.adito.aditoweb.nbm.tests.actions.create.NewActionsContainer")
@ActionRegistration(displayName = "#CTL_TestsAction", lazy = false)
@ActionReference(path = "de/adito/aod/action/neonView", position = 170, separatorBefore = 160, separatorAfter = 180)
public class NewActionsContainer extends AbstractAction implements Presenter.Popup
{

  @Override
  public void actionPerformed(ActionEvent e)
  {
    throw new IllegalStateException("Not implemented");
  }

  @Override
  public JMenuItem getPopupPresenter()
  {
    JMenu main = new JMenu(Bundle.CTL_TestsAction());
    Utilities.actionsForPath("Plugins/Tests/Actions/New").forEach(pAction -> {
      if (pAction != null)
        main.add(pAction);
      else
        main.addSeparator();
    });
    return main;
  }
}