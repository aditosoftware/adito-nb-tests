package de.adito.aditoweb.nbm.tests.nbm.node.modification;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.nodes.INodeModificationSupport;
import lombok.NonNull;
import org.jetbrains.annotations.*;
import org.netbeans.api.project.*;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;

import java.util.Map;


/**
 * Modifies the neonView-Nodes to display the contents of the cypress-Folder directly under its view.
 * <p>
 * The position of this modification needs to be rather high, in order to be applied after other modifications
 * and therefore adding the children at the latest possible moment.
 *
 * @author s.seemann, 04.03.2021
 */
@ServiceProvider(service = INodeModificationSupport.class, path = "Projects/de-adito-project/Nodes/neonView", position = 10_000)
public class ViewNodeModificationSupport implements INodeModificationSupport
{
  @Override
  public boolean canModify(@NonNull Node pNode)
  {
    return true;
  }

  @Nullable
  @Override
  public Node modify(@NonNull Node pNode, @NonNull Map<Object, Object> pAttributes)
  {
    Object sourceProject = pAttributes.get("sourceProject");

    // easiest solution: the root project will be in the attributes
    if (sourceProject instanceof Project)
      return new NeonViewNode(pNode, (Project) sourceProject);

    // if it is not there, try to get the project from the node lookup and then get the root project
    Project projectFromLookup = pNode.getLookup().lookup(Project.class);
    if (projectFromLookup != null)
      return new NeonViewNode(pNode, ProjectUtils.rootOf(projectFromLookup));

    //if we don't find any project, just return the current node
    return pNode;
  }
}
