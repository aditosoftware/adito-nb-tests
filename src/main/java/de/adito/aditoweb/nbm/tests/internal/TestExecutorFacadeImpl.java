package de.adito.aditoweb.nbm.tests.internal;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.javascript.node.*;
import de.adito.aditoweb.nbm.tests.api.ITestExecutorFacade;
import de.adito.notification.INotificationFacade;
import org.apache.commons.io.output.WriterOutputStream;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.windows.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ITestExecutorFacade}
 *
 * @author s.seemann, 17.03.2021
 */
public class TestExecutorFacadeImpl implements ITestExecutorFacade
{
  private final InputOutput output = IOProvider.getDefault().getIO("cypress", false);
  private final WriterOutputStream outputWriter;

  private INodeJSEnvironment nodeJsEnv;
  private INodeJSExecutor executor;

  public TestExecutorFacadeImpl()
  {
    outputWriter = new WriterOutputStream(this.output.getOut(), StandardCharsets.UTF_8, 128, true);
  }

  private void _setUp(@NotNull Project pProject)
  {
    executor = INodeJSExecutor.findInstance(pProject).orElse(null);
    INodeJSProvider provider = INodeJSProvider.findInstance(pProject).orElse(null);
    if (executor == null
        || provider == null
        || !provider.current().blockingFirst().isPresent())
      throw new NoNodeJSException();

    nodeJsEnv = provider.current().blockingFirst().get();

    try
    {
      output.getOut().reset();
    }
    catch (IOException pE)
    {
      INotificationFacade.INSTANCE.error(pE);
    }
    output.select();
    output.getOut().println(NbBundle.getMessage(TestExecutorFacadeImpl.class, "LBL_OUTPUT_STARTING"));
  }

  @Override
  public void executeTests(@NotNull Project pProject, @NotNull Collection<FileObject> pFiles)
  {
    _setUp(pProject);

    String specs = pFiles.stream()
        .map(FileUtil::toFile)
        .map(File::getAbsolutePath)
        .collect(Collectors.joining(","));

    String specQuoted = "\"" + specs + "\"";
    try
    {
      executor.executeAsync(nodeJsEnv, _getExecBase(), outputWriter, null,
                            null, "run", "--spec", specQuoted);
    }
    catch (IOException pE)
    {
      INotificationFacade.INSTANCE.error(pE);
    }
  }

  @Override
  public void executeAllTests(@NotNull Project pProject)
  {
    _setUp(pProject);

    try
    {
      executor.executeAsync(nodeJsEnv, _getExecBase(), outputWriter, null,
                            null, "run");
    }
    catch (IOException pE)
    {
      INotificationFacade.INSTANCE.error(pE);
    }
  }

  @NotNull
  private INodeJSExecBase _getExecBase()
  {
    if (BaseUtilities.isWindows())
      return INodeJSExecBase.binary("cypress.cmd");
    return INodeJSExecBase.binary("cypress");
  }
}
