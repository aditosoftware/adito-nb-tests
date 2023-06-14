package de.adito.aditoweb.nbm.tests.nbm.node;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.common.IDisposerService;
import de.adito.aditoweb.nbm.tests.nbm.TestsFolderService;
import lombok.NonNull;
import org.jetbrains.annotations.*;
import org.netbeans.api.project.Project;
import org.openide.nodes.*;
import org.openide.util.*;
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
  private TestsFolderService folderService;

  @SuppressWarnings("unused") // ServiceProvider
  public RootNodeChildFactory()
  {
  }

  @SuppressWarnings("unused") // ServiceProvider
  public RootNodeChildFactory(@NonNull Project pProject)
  {
    // ins lookup werfen
    IDisposerService disposer = Lookup.getDefault().lookup(IDisposerService.class);
    if (disposer != null)
      disposer.register(pProject, TestsFolderService.observe(pProject)
          .subscribe(pServOpt -> {
            folderService = pServOpt.orElse(null);
            refresh(false);
          }));
  }

  @Nullable
  @Override
  protected Node createNodeForKey(@NonNull String key)
  {
    if (key.equals(_NODE_KEY) && folderService != null)
      return new EmptyFolderNode(folderService::createGlobalTestsFolder,
                                 NbBundle.getMessage(RootNodeChildFactory.class, "LBL_NODE_GLOBAL_TESTSFOLDER_NAME"),
                                 folderService.observeGlobalTestsFolder());
    return null;
  }

  @Override
  protected boolean createKeys(@NonNull List<String> toPopulate)
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