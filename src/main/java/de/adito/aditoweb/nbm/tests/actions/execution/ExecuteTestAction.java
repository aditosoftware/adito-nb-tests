package de.adito.aditoweb.nbm.tests.actions.execution;

import de.adito.actions.AbstractAsyncNodeAction;
import de.adito.aditoweb.nbm.nbide.nbaditointerface.common.IProjectQuery;
import de.adito.aditoweb.nbm.nbide.nbaditointerface.javascript.node.INodeJSProvider;
import de.adito.aditoweb.nbm.tests.api.*;
import org.jetbrains.annotations.*;
import org.netbeans.api.project.Project;
import org.openide.awt.*;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.*;

import java.util.*;
import java.util.stream.*;

/**
 * Executes a test
 *
 * @author s.seemann, 17.03.2021
 */
@NbBundle.Messages("CTL_ExecuteTestsAction=Execute Tests")
@ActionID(category = "Tests", id = "de.adito.aditoweb.nbm.tests.actions.execution.ExecuteTestAction")
@ActionRegistration(displayName = "#CTL_ExecuteTestsAction", lazy = false)
@ActionReferences({
                   @ActionReference(path = ITestsConstants.ACTIONS_PATH),
                   @ActionReference(path = "de/adito/aod/action/neonView", position = 210)
})
public class ExecuteTestAction extends AbstractAsyncNodeAction
{

  @Override
  protected boolean enable0(@NotNull Node[] activatedNodes)
  {
    if (activatedNodes.length > 0)
    {
      Project project = _findProject(activatedNodes);

      // NodeJS must be present for executing a test file
      return project != null && INodeJSProvider.findInstance(project).isPresent();
    }
    return false;
  }

  @Override
  protected void performAction(Node[] activatedNodes)
  {
    Project project = _findProject(activatedNodes);
    // Normally cannot be null, because the action would be disabled
    Objects.requireNonNull(project);


    Set<FileObject> fos = Arrays.stream(activatedNodes)
        .flatMap(this::_findFile)
        .collect(Collectors.toSet());

    ITestExecutorFacade.INSTANCE.executeTests(project, fos);
  }

  @Override
  public String getName()
  {
    return Bundle.CTL_ExecuteTestsAction();
  }

  @Override
  public HelpCtx getHelpCtx()
  {
    return null;
  }

  /**
   * Checks recursively , if the node contains a {@link ITestFileProvider}.
   *
   * @param pNode the node
   * @return a Stream of the founded files incl. the children
   */
  @NotNull
  private Stream<FileObject> _findFile(@NotNull Node pNode)
  {
    ITestFileProvider provider = pNode.getLookup().lookup(ITestFileProvider.class);
    if (provider != null)
    {
      FileObject file = provider.getFile();
      if (file != null)
      {
        if (file.isFolder())
          return Arrays.stream(pNode.getChildren().getNodes()).flatMap(this::_findFile);
        return Stream.of(file);
      }
    }
    return Stream.of();
  }

  /**
   * Looks for a project in the lookups of the node.
   *
   * @param pNodes the nodes
   * @return the project or null, if nothing is found
   */
  @Nullable
  private Project _findProject(@NotNull Node[] pNodes)
  {
    return Arrays.stream(pNodes)
        .map(pNode -> IProjectQuery.getInstance().findProjects(pNode, IProjectQuery.ReturnType.MULTIPLE_TO_NULL))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }
}
