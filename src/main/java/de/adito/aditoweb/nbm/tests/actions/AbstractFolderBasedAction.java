package de.adito.aditoweb.nbm.tests.actions;

import com.google.common.annotations.VisibleForTesting;
import de.adito.actions.AbstractAsyncNodeAction;
import de.adito.aditoweb.nbm.nbide.nbaditointerface.common.IProjectQuery;
import de.adito.aditoweb.nbm.tests.api.ITestFileProvider;
import de.adito.aditoweb.nbm.tests.nbm.TestsFolderService;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.project.*;
import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

import java.io.File;
import java.util.*;

/**
 * Abstract Action for all actions, which work on files.
 *
 * @author s.seemann, 05.03.2021
 */
public abstract class AbstractFolderBasedAction extends AbstractAsyncNodeAction
{

  @Override
  protected boolean enable0(Node @NonNull [] activatedNodes)
  {
    // One Non-Null Node
    return Arrays.stream(activatedNodes).filter(Objects::nonNull).count() == 1;
  }

  @Override
  protected void performAction(Node[] activatedNodes)
  {
    NotifyDescriptor.InputLine desc = new NotifyDescriptor.InputLine(getInputLineTitle(), getName());
    Object result = DialogDisplayer.getDefault().notify(desc);
    String input = desc.getInputText().trim();
    if (result != NotifyDescriptor.OK_OPTION || input.isEmpty())
      return;

    //noinspection OptionalGetWithoutIsPresent Action would be disabled
    Node node = Arrays.stream(activatedNodes).filter(Objects::nonNull).findFirst().get(); // NOSONAR
    File parent = findFileOnFileSystem(node);
    if (parent != null)
      performAction0(node, parent, input);
  }

  /**
   * Performs the Action.
   *
   * @param pNode   current selected Node
   * @param pParent the Parent
   * @param pName   the name of a new file
   */
  protected abstract void performAction0(@NonNull Node pNode, @NonNull File pParent, @NonNull String pName);

  /**
   * @return the Label for the dialog with user-interaction
   */
  @NonNull
  protected abstract String getInputLineTitle();

  @Override
  public HelpCtx getHelpCtx()
  {
    return null;
  }

  /**
   * Search for a File in the Node
   *
   * @param pNode the node
   * @return a file or null, if nothing was found
   */
  @Nullable
  public File findFile(@NonNull Node pNode)
  {
    FileObject fo = pNode.getLookup().lookup(FileObject.class);
    if (fo != null)
      return FileUtil.toFile(fo);

    DataObject dataObject = pNode.getLookup().lookup(DataObject.class);
    if (dataObject != null)
      return FileUtil.toFile(dataObject.getPrimaryFile());

    return pNode.getLookup().lookup(File.class);
  }

  /**
   * Finds the corresponding file of the node on the filesystem.
   *
   * @param pNode the node
   * @return the actual file on the filesystem of the node.
   */
  @Nullable
  @VisibleForTesting
  File findFileOnFileSystem(@NonNull Node pNode)
  {
    File f = null;

    ITestFileProvider fileProvider = pNode.getLookup().lookup(ITestFileProvider.class);
    if (fileProvider != null)
    {
      FileObject folder = fileProvider.getFile();
      if (folder != null)
        f = FileUtil.toFile(folder);
    }

    if (f == null)
    {
      Project project = IProjectQuery.getInstance().findProjects(pNode, IProjectQuery.ReturnType.MULTIPLE_TO_NULL);
      f = findFile(pNode);

      if (project != null && f != null)
      {
        String name = f.getName();
        // only the name of the model without the extensions "aod"
        if (f.isFile())
          name = FilenameUtils.removeExtension(name);

        // get the root project. If we use modularization this will be different from the normal project
        Project rootProject = ProjectUtils.rootOf(project);
        f = rootProject.getLookup().lookup(TestsFolderService.class).getTestsFolderForModel(name);
      }
    }

    return f;
  }
}