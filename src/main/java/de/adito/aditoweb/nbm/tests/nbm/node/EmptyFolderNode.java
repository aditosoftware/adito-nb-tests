package de.adito.aditoweb.nbm.tests.nbm.node;

import com.google.common.base.Strings;
import de.adito.aditoweb.nbm.tests.nbm.node.modification.FolderNode;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.*;
import org.jetbrains.annotations.NotNull;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * Node for a folder, which does not have to be existing. If it doesn't exist, an empty node is shown with a action which creates the folder.
 *
 * @author s.seemann, 22.04.2021
 */
class EmptyFolderNode extends FilterNode implements Disposable
{
  private static final Color _DEFAULTVALUE_COLOR = UIManager.getColor("PropSheet.disabledForeground");
  private static final String _DEFAULTVALUE_COLOR_STRING = _DEFAULTVALUE_COLOR == null ? null :
      Strings.padStart(Integer.toHexString(_DEFAULTVALUE_COLOR.getRGB()), 8, '0').substring(2);

  private final CompositeDisposable disposable = new CompositeDisposable();
  private final Runnable createFolderRunnable;
  private final String displayName;

  /**
   * Creates a node
   *
   * @param pCreateFolderRunnable the runnable, which creates the folder
   * @param pDisplayName          display Name
   * @param pFileObservable       Observable for the fileobject
   */
  public EmptyFolderNode(@NotNull Runnable pCreateFolderRunnable, @NotNull String pDisplayName, @NotNull Observable<Optional<FileObject>> pFileObservable)
  {
    super(AbstractNode.EMPTY);
    createFolderRunnable = pCreateFolderRunnable;
    displayName = pDisplayName;
    disposable.add(pFileObservable
                       .subscribe(pFoOpt -> {
                                    if (pFoOpt.isPresent())
                                    {
                                      try
                                      {
                                        Node delegate = DataObject.find(pFoOpt.get()).getNodeDelegate();
                                        Children.MUTEX.postWriteRequest(() -> changeOriginal(new FolderNode(delegate), true));
                                      }
                                      catch (DataObjectNotFoundException pE)
                                      {
                                        // do nothing
                                      }
                                    }
                                  }
                       ));
  }

  @Override
  public String getHtmlDisplayName()
  {
    if (!Children.LEAF.equals(getChildren()) || _DEFAULTVALUE_COLOR_STRING == null)
      return displayName;
    return "<html><font color=\"" + _DEFAULTVALUE_COLOR_STRING + "\">" + displayName + "</font></html>";
  }

  @Override
  public Action[] getActions(boolean context)
  {
    if (getOriginal().equals(AbstractNode.EMPTY))
      return new Action[]{new _CreateFolderAction(createFolderRunnable)};

    return super.getActions(context);
  }

  @Override
  public void dispose()
  {
    disposable.dispose();
  }

  @Override
  public boolean isDisposed()
  {
    return disposable.isDisposed();
  }

  private static class _CreateFolderAction extends AbstractAction
  {
    private final Runnable createFolderRunnable;

    public _CreateFolderAction(@NotNull Runnable pCreateFolderRunnable)
    {
      createFolderRunnable = pCreateFolderRunnable;
      putValue(Action.NAME, NbBundle.getMessage(EmptyFolderNode.class, "LBL_CREATE_FOLDER"));
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      createFolderRunnable.run();
    }
  }
}
