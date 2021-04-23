package de.adito.aditoweb.nbm.tests.nbm.node.modification;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.nodes.INodeModificationSupport;
import org.jetbrains.annotations.*;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;

import java.util.Map;


/**
 * Modifies the neonView-Nodes to display the contents of the cypress-Folder directly under its view
 *
 * @author s.seemann, 04.03.2021
 */
@ServiceProvider(service = INodeModificationSupport.class, path = "Projects/de-adito-project/Nodes/neonView", position = 100)
public class ViewNodeModificationSupport implements INodeModificationSupport
{
  @Override
  public boolean canModify(@NotNull Node pNode)
  {
    return true;
  }

  @Nullable
  @Override
  public Node modify(@NotNull Node pNode, @NotNull Map<Object, Object> pAttributes)
  {
    return new NeonViewNode(pNode);
  }
}
