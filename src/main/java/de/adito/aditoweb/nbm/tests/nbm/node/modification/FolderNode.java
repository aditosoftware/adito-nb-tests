package de.adito.aditoweb.nbm.tests.nbm.node.modification;

import de.adito.aditoweb.nbm.tests.actions.create.NewActionsContainer;
import de.adito.aditoweb.nbm.tests.api.ITestFileProvider;
import org.jetbrains.annotations.NotNull;
import org.openide.filesystems.FileObject;
import org.openide.nodes.*;
import org.openide.util.lookup.*;

import javax.swing.*;
import java.util.*;

/**
 * Node for test folders
 *
 * @author s.seemann, 22.04.2021
 */
public class FolderNode extends FilterNode
{
  public FolderNode(@NotNull Node pOriginal)
  {
    super(pOriginal, new _Children(pOriginal), new ProxyLookup(Lookups.fixed((ITestFileProvider)
                                                                                 () -> pOriginal.getLookup().lookup(FileObject.class)),
                                                               pOriginal.getLookup()));
  }

  public void changeOriginal(@NotNull Node pNode)
  {
    if (!pNode.equals(getOriginal()))
      changeOriginal(pNode, true);
  }

  @Override
  public Action[] getActions(boolean pContext)
  {
    Action[] actionArr = super.getActions(pContext);
    List<Action> actions = actionArr == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(actionArr));

    // Replace "New"-Action with our own folder, if available
    boolean wasRemoved = actions.removeIf(pAction -> pAction != null &&
        pAction.getClass().getName().startsWith("org.netbeans.modules.project.ui.actions.NewFile"));
    if (wasRemoved)
      actions.add(0, new NewActionsContainer());

    NeonViewNode.addExecuteActions(actions);

    return actions.toArray(new Action[0]);
  }

  /**
   * Children-Impl
   */
  private static class _Children extends FilterNode.Children
  {
    public _Children(@NotNull Node pOriginal)
    {
      super(pOriginal);
    }

    @Override
    public Node[] getNodes(boolean optimalResult)
    {
      return super.getNodes(optimalResult);
    }

    @Override
    protected Node copyNode(@NotNull Node pNode)
    {
      FileObject fo = pNode.getLookup().lookup(FileObject.class);
      if (fo != null)
      {
        if (fo.isFolder())
          return new FolderNode(pNode);
        else
          return new TestFileNode(pNode);
      }

      return super.copyNode(pNode);
    }
  }
}
