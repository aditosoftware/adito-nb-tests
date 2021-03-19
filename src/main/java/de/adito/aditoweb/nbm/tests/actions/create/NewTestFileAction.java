package de.adito.aditoweb.nbm.tests.actions.create;

import de.adito.aditoweb.nbm.tests.actions.*;
import de.adito.aditoweb.nbm.tests.nbm.*;
import org.jetbrains.annotations.NotNull;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.io.*;

/**
 * Creates a new test file
 *
 * @author s.seemann, 05.03.2021
 */
@NbBundle.Messages("CTL_NewFile=New Test-File")
@ActionID(category = "Tests", id = "de.adito.aditoweb.nbm.tests.actions.create.NewTestFileAction")
@ActionRegistration(displayName = "#CTL_NewFile", lazy = false)
@ActionReference(path = "Plugins/Tests/Actions/New", position = 200, separatorAfter = 250)
public class NewTestFileAction extends AbstractFolderBasedAction
{
  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Override
  protected void performAction0(@NotNull Node pNode, @NotNull File pParent, @NotNull String pName)
  {
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
        pE.printStackTrace(); // TODO errorhandling
      }
    }

    NbUtil.open(target);
  }

  @NbBundle.Messages("LBL_NewTestFile=File Name")
  @Override
  @NotNull
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
