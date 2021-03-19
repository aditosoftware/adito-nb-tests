package de.adito.aditoweb.nbm.tests.internal;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.javascript.node.*;
import de.adito.aditoweb.nbm.tests.api.ITestExecutorFacade;
import de.adito.notification.INotificationFacade;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.openide.filesystems.*;
import org.openide.windows.*;

import java.io.*;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ITestExecutorFacade}
 *
 * @author s.seemann, 17.03.2021
 */
public class TestExecutorFacadeImpl implements ITestExecutorFacade
{
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private final InputOutput output = IOProvider.getDefault().getIO("cypress", false);

  private INodeJSEnvironment nodeJsEnv;
  private INodeJSExecutor executor;

  private void _setUp(@NotNull Project pProject)
  {
    executor = INodeJSExecutor.findInstance(pProject).orElse(null);
    INodeJSProvider provider = INodeJSProvider.findInstance(pProject).orElse(null);
    if (executor == null
        || provider == null
        || !provider.current().blockingFirst().isPresent())
      throw new NoNodeJSException();

    nodeJsEnv = provider.current().blockingFirst().get();
  }

  private void _log(@NotNull String pMsg)
  {
    output.select();
    output.getOut().print(pMsg);
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
    executorService.execute(() -> {
      try
      {
        String result = executor.executeSync(nodeJsEnv, INodeJSExecBase.binary("cypress.cmd"), -1, "run", "--spec", specQuoted);
        _log(result);
      }
      catch (IOException | InterruptedException pE)
      {
        INotificationFacade.INSTANCE.error(pE);
      }
    });
  }

  @Override
  public void executeAllTests(@NotNull Project pProject)
  {
    _setUp(pProject);
    executorService.execute(() -> {
      try
      {
        String result = executor.executeSync(nodeJsEnv, INodeJSExecBase.binary("cypress.cmd"), -1, "run");
        _log(result);
      }
      catch (IOException | InterruptedException pE)
      {
        INotificationFacade.INSTANCE.error(pE);
      }
    });
  }
}
