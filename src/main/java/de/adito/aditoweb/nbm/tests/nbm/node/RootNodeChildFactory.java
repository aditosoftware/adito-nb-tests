package de.adito.aditoweb.nbm.tests.nbm.node;

import de.adito.aditoweb.nbm.tests.nbm.TestsFolderService;
import org.jetbrains.annotations.*;
import org.netbeans.api.project.Project;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adds a global-tests-node to the project tree.
 *
 * @author s.seemann, 19.04.2021
 */
@ServiceProvider(service = ChildFactory.class, path = "Projects/de-adito-project/Nodes/children", position = 1000)
public class RootNodeChildFactory extends ChildFactory<String>
{
  private static final String _NODE_KEY = "cypressGlobalTestsNode";
  private static final String _PROCESS = "PROCESS_EDITOR";

  private Project project;

  @SuppressWarnings("unused") // ServiceProvider
  public RootNodeChildFactory()
  {
  }

  @SuppressWarnings("unused") // ServiceProvider
  public RootNodeChildFactory(@NotNull Project pProject)
  {
    project = pProject;
  }

  @Nullable
  @Override
  protected Node createNodeForKey(@NotNull String key)
  {
    if (key.equals(_NODE_KEY))
    {
      TestsFolderService service = TestsFolderService.getInstance(project);
      return new EmptyFolderNode(service::createGlobalTestsFolder, NbBundle.getMessage(RootNodeChildFactory.class, "LBL_NODE_GLOBAL_TESTSFOLDER_NAME"),
                                 service.observeGlobalTestsFolder());
    }
    return null;
  }

  @Override
  protected boolean createKeys(@NotNull List<String> toPopulate)
  {
    //noinspection unchecked,rawtypes
    List<String> collect = (List<String>) ((List) toPopulate).stream().map(Object::toString).collect(Collectors.toList());
    int index = collect.indexOf(_PROCESS);
    if (index == -1)
      toPopulate.add(_NODE_KEY);
    else
      toPopulate.add(index + 1, _NODE_KEY);
    return true;
  }
}