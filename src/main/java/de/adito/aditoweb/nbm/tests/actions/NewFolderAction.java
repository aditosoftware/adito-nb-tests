package de.adito.aditoweb.nbm.tests.actions;

import org.jetbrains.annotations.NotNull;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.io.*;
import java.nio.file.Files;

/**
 * Creates a new Folder.
 *
 * @author s.seemann, 05.03.2021
 */
@NbBundle.Messages("CTL_NewFolder=New Folder")
@ActionID(category = "Tests", id = "de.adito.aditoweb.nbm.tests.actions.NewFolderAction")
@ActionRegistration(displayName = "#CTL_NewFolder", lazy = false)
@ActionReference(path = "Plugins/Tests/Actions/New", position = 1000)
public class NewFolderAction extends AbstractFolderBasedAction
{
  @Override
  protected void performAction0(@NotNull Node pNode, @NotNull File pParent, @NotNull String pName)
  {
    try
    {
      Files.createDirectories(new File(pParent, pName).toPath());
    }
    catch (IOException pE)
    {
      pE.printStackTrace(); // TODO errorhandling
    }
  }

  @NbBundle.Messages("LBL_NewFolder=Folder Name")
  @Override
  @NotNull
  protected String getInputLineTitle()
  {
    return Bundle.LBL_NewFolder();
  }

  @Override
  public String getName()
  {
    return Bundle.CTL_NewFolder();
  }
}
