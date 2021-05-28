package de.adito.aditoweb.nbm.tests.nbm.node.modification;

import de.adito.aditoweb.nbm.tests.api.*;
import de.adito.aditoweb.nbm.tests.nbm.TestsFolderService;
import de.adito.nbm.project.ProjectTabUtil;
import de.adito.observables.netbeans.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.*;
import org.jetbrains.annotations.*;
import org.netbeans.api.project.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.Utilities;
import org.openide.util.lookup.*;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Modification of the Neon-View-Node and its children
 *
 * @author s.seemann, 17.03.2021
 */
class NeonViewNode extends FilterNode implements Disposable
{
  private final CompositeDisposable disposable = new CompositeDisposable();
  private Boolean changeOriginal = null;
  private FolderNode node = null;
  private List<String[]> expanded = null;

  public NeonViewNode(Node pOriginal)
  {
    super(new AbstractNode(Children.LEAF), null,
          new ProxyLookup(new AbstractLookup(new InstanceContent()),
                          Lookups.exclude(pOriginal.getLookup(), Node.class),
                          Lookups.fixed(new _FileProvider(pOriginal))));

    disposable.add(_watchTestsFolder(pOriginal).subscribe(pFileObject -> FilterNode.Children.MUTEX.postWriteRequest(() -> {
      if (pFileObject.isPresent())
      {
        // change original only once
        if (Boolean.TRUE.equals(changeOriginal))
        {
          Node foNode = _getNode(pFileObject.get());
          // Create node only once
          if (node == null)
          {
            node = new FolderNode(foNode != null ? foNode : new AbstractNode(Children.LEAF));
            changeOriginal(node, true);
            changeOriginal(pOriginal, false);

          }
          else
          {
            if (foNode != null)
              node.changeOriginal(foNode);
            if (expanded != null)
            {
              ProjectTabUtil.setExpandedNodes(expanded);
              expanded = null;
            }
          }
          changeOriginal = false;
        }
      }
      // if fileObject isn't present, on the next event the original must be changed
      else if (Boolean.FALSE.equals(changeOriginal))
        changeOriginal = true;

      // Change only once the original of the view node
      if (changeOriginal == null)
      {
        changeOriginal(pOriginal, false);
        changeOriginal = true;
      }
    })));

    // Tests-Folder-Mover
    disposable.add(_watchViewAODFile(pOriginal)
                       .debounce(500, TimeUnit.MILLISECONDS)
                       .subscribe(new TestsFolderMover()));
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

  /**
   * Creates a new observable to watch the corresponding tests folder instance
   *
   * @param pViewNode Node of the view
   * @return the tests folder observable
   */
  @NotNull
  private Observable<Optional<FileObject>> _watchTestsFolder(@NotNull Node pViewNode)
  {
    return _watchViewAODFile(pViewNode)

        // Watch View-Folder in corresponding tests folder
        .switchMap(pViewOpt -> pViewOpt
            .map(pView -> {
              Project project = FileOwnerQuery.getOwner(pView);
              if (project != null)
                return TestsFolderService.getInstance(project).observeTestsFolderForModel(pView.getName());
              return null;
            })
            .orElseGet(() -> Observable.just(Optional.empty())))

        // only changes
        .distinctUntilChanged();
  }

  /**
   * Creates a new observable that observes the neonView aod file
   *
   * @param pViewNode Node
   * @return Name-Observable
   */
  @NotNull
  private Observable<Optional<FileObject>> _watchViewAODFile(@NotNull Node pViewNode)
  {
    return LookupResultObservable.create(pViewNode.getLookup(), DataObject.class)

        // Find FileObject to our DataObject
        .map(pDataObjects -> pDataObjects.stream()
            .map(DataObject::getPrimaryFile)
            .filter(Objects::nonNull)
            .findFirst())

        // Watch FileObject (rename, delete, etc) for AOD File
        .flatMap(pFileObjOpt -> pFileObjOpt
            .map(pFileObj -> FileObjectObservable.create(pFileObj)
                .map(Optional::of))
            .orElseGet(() -> Observable.just(Optional.empty())));
  }

  /**
   * Generates the node from a fileobject
   *
   * @param pFileObject FileObject to get the node from
   * @return the node, or null if it cannot be read
   */
  @Nullable
  private Node _getNode(@NotNull FileObject pFileObject)
  {
    try
    {
      DataObject dataObject = DataObject.find(pFileObject);
      if (dataObject != null)
      {
        return dataObject.getNodeDelegate();
      }
    }
    catch (DataObjectNotFoundException e)
    {
      // nothing
    }

    return null;
  }

  /**
   * Adds the execution-actions to the list. If the paste-action is present, the execute-actions are inserted after this one.
   *
   * @param pActions list of actions
   */
  public static void addExecuteActions(@NotNull List<Action> pActions)
  {
    boolean wasInserted = false;

    for (int i = 0; i < pActions.size(); i++)
    {
      Action action = pActions.get(i);
      if (action != null
          && action.getClass().getName().startsWith("org.openide.actions.PasteAction"))
      {
        List<? extends Action> executeActions = Utilities.actionsForPath(ITestsConstants.ACTIONS_PATH);
        executeActions.add(0, null);
        pActions.addAll(i + 1, executeActions);
        wasInserted = true;
        break;
      }
    }

    if (!wasInserted)
      pActions.addAll(Utilities.actionsForPath(ITestsConstants.ACTIONS_PATH));
  }

  private static class _FileProvider implements ITestFileProvider
  {
    private final Node original;

    _FileProvider(Node pOriginal)
    {

      original = pOriginal;
    }

    @Nullable
    @Override
    public FileObject getFile()
    {
      Project project = original.getLookup().lookup(Project.class);
      if (project != null)
        return FileUtil.toFileObject(TestsFolderService.getInstance(project).getTestsFolderForModel(original.getName()));

      return null;
    }
  }
}
