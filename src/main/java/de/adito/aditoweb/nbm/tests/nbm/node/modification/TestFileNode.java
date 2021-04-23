package de.adito.aditoweb.nbm.tests.nbm.node.modification;

import de.adito.aditoweb.nbm.tests.api.ITestFileProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.*;
import org.openide.util.lookup.*;

import javax.swing.*;
import java.util.*;

/**
 * Node for test files
 */
class TestFileNode extends FilterNode
{

  public TestFileNode(Node pOriginal)
  {
    super(pOriginal, null, new ProxyLookup(Lookups.fixed((ITestFileProvider)
                                                             () -> pOriginal.getLookup().lookup(FileObject.class)),
                                           pOriginal.getLookup()));
  }

  @Override
  public Action[] getActions(boolean pContext)
  {
    Action[] actionArr = super.getActions(pContext);
    List<Action> actions = actionArr == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(actionArr));

    NeonViewNode.addExecuteActions(actions);

    return actions.toArray(new Action[0]);
  }
}
