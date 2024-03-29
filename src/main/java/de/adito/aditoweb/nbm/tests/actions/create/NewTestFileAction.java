package de.adito.aditoweb.nbm.tests.actions.create;

import de.adito.aditoweb.nbm.tests.actions.AbstractFolderBasedAction;
import de.adito.aditoweb.nbm.tests.nbm.NbUtil;
import de.adito.notification.INotificationFacade;
import lombok.NonNull;
import org.jetbrains.annotations.*;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.io.*;

/**
 * Creates a new test file
 *
 * @author s.seemann, 05.03.2021
 */
@NbBundle.Messages("CTL_NewFile=Create Empty Cypress File")
@ActionID(category = "Tests", id = "de.adito.aditoweb.nbm.tests.actions.create.NewTestFileAction")
@ActionRegistration(displayName = "#CTL_NewFile", lazy = false)
@ActionReference(path = "Plugins/Tests/Actions/New", position = 200, separatorAfter = 250)
public class NewTestFileAction extends AbstractFolderBasedAction
{
  @Override
  protected void performAction0(@NonNull Node pNode, @NonNull File pParent, @NonNull String pName)
  {
    File target = createTarget(pParent, pName);
    if (target != null)
      NbUtil.open(target);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Nullable
  protected File createTarget(@NonNull File pParent, @NonNull String pName)
  {
    if (!pName.endsWith(".cy.ts"))
      pName += ".cy.ts";

    File target = new File(pParent, pName);
    if (!target.exists())
    {
      try
      {
        target.getParentFile().mkdirs();
        target.createNewFile();
      }
      catch (IOException pE)
      {
        INotificationFacade.INSTANCE.error(pE);
        return null;
      }
    }
    return target;
  }

  @NbBundle.Messages("LBL_NewTestFile=File Name")
  @Override
  @NonNull
  protected String getInputLineTitle()
  {
    return Bundle.LBL_NewTestFile();
  }

  @Override
  public String getName()
  {
    return Bundle.CTL_NewFile();
  }
}
