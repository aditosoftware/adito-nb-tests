package de.adito.aditoweb.nbm.tests.actions.create;

import com.google.common.base.Charsets;
import de.adito.aditoweb.nbm.tests.nbm.NbUtil;
import de.adito.notification.INotificationFacade;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.io.*;
import java.util.Objects;

/**
 * Creates a new test file with a template
 *
 * @author s.seemann, 17.12.2021
 */
@NbBundle.Messages("CTL_NewFileWithTemplate=Create Cypress File with Template")
@ActionID(category = "Tests", id = "de.adito.aditoweb.nbm.tests.actions.create.NewTestFileWithTemplateAction")
@ActionRegistration(displayName = "#CTL_NewFileWithTemplate", lazy = false)
@ActionReference(path = "Plugins/Tests/Actions/New", position = 210)
public class NewTestFileWithTemplateAction extends NewTestFileAction
{
  private static final String _TEMPLATE;

  static
  {
    try
    {
      _TEMPLATE = IOUtils.toString(Objects.requireNonNull(NewTestFileWithTemplateAction.class.getResource("template.ts")), Charsets.UTF_8);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void performAction0(@NotNull Node pNode, @NotNull File pParent, @NotNull String pName)
  {
    File target = createTarget(pParent, pName);

    if (target != null)
    {
      try (FileOutputStream fos = new FileOutputStream(target))
      {
        IOUtils.write(_TEMPLATE, fos, Charsets.UTF_8);
      }
      catch (IOException pE)
      {
        INotificationFacade.INSTANCE.error(pE);
      }
      NbUtil.open(target);
    }
  }


  @Override
  public String getName()
  {
    return Bundle.CTL_NewFileWithTemplate();
  }
}
