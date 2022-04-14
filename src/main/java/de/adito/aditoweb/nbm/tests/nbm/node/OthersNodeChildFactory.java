package de.adito.aditoweb.nbm.tests.nbm.node;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.common.IDisposerService;
import de.adito.aditoweb.nbm.tests.nbm.TestsFolderService;
import org.jetbrains.annotations.*;
import org.netbeans.api.project.Project;
import org.openide.nodes.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import java.util.List;

/**
 * Adds the cypress-node to the node "others" of the project tree
 *
 * @author s.seemann, 19.04.2021
 */
@ServiceProvider(service = ChildFactory.class, path = "Projects/de-adito-project/Nodes/other/children", position = 100)
public class OthersNodeChildFactory extends ChildFactory<String>
{
  private static final String _NODE_KEY = "cypressOthersNode";

  private TestsFolderService folderService;

  @SuppressWarnings("unused") // ServiceProvider
  public OthersNodeChildFactory()
  {
  }

  @SuppressWarnings("unused") // ServiceProvider
  public OthersNodeChildFactory(@NotNull Project pProject)
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
  protected Node createNodeForKey(@NotNull String key)
  {
    if (key.equals(_NODE_KEY) && folderService != null)
      return new EmptyFolderNode(folderService::createCypressFolder, "cypress", folderService.observeCypressFolder());
    return null;
  }

  @Override
  protected boolean createKeys(@NotNull List<String> toPopulate)
  {
    toPopulate.add(_NODE_KEY);
    return true;
  }
}
