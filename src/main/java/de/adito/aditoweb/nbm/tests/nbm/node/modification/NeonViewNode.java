package de.adito.aditoweb.nbm.tests.nbm.node.modification;

import com.google.common.annotations.VisibleForTesting;
import de.adito.aditoweb.nbm.tests.api.*;
import de.adito.aditoweb.nbm.tests.nbm.TestsFolderService;
import de.adito.nbm.project.ProjectTabUtil;
import de.adito.observables.netbeans.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.*;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.*;
import org.netbeans.api.project.Project;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
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
  private List<String[]> expanded = null;
  private final RequestProcessor rp = new RequestProcessor("tNeonViewNodeProcessor");

  private final Project project;

  /**
   * Constructor.
   *
   * @param pOriginal the original node that should be modified
   * @param pProject  the project the node belongs to
   */
  public NeonViewNode(@NotNull Node pOriginal, @NotNull Project pProject)
  {
    super(new AbstractNode(Children.LEAF), null,
          new ProxyLookup(new AbstractLookup(new InstanceContent()),
                          Lookups.exclude(pOriginal.getLookup(), Node.class),
                          Lookups.fixed(new FileProvider(pOriginal, pProject))));

    project = pProject;

    disposable.add(watchTestsFolder(pOriginal).subscribe(pFileObject -> rp.post(() -> {
      if (pFileObject.isPresent())
      {
        Node foNode = getNode(pFileObject.get());
        FolderNode node = new FolderNode(foNode != null ? foNode : new AbstractNode(Children.LEAF));
        changeOriginal(node, true);
        changeOriginal(pOriginal, false);

        if (foNode != null)
          node.changeOriginal(foNode);

        if (expanded != null)
        {
          ProjectTabUtil.setExpandedNodes(expanded);
          expanded = null;
        }
      }
      else
        changeOriginal(pOriginal, true);
    })));

    // Tests-Folder-Mover
    disposable.add(watchViewAODFile(pOriginal)
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
  private Observable<Optional<FileObject>> watchTestsFolder(@NotNull Node pViewNode)
  {
    return watchViewAODFile(pViewNode)

        // Watch View-Folder in corresponding tests folder
        .switchMap(pViewOpt -> pViewOpt
            .map(pView -> TestsFolderService.observe(project)
                .switchMap(pTestFolderOpt -> pTestFolderOpt
                    .map(pService -> pService.observeTestsFolderForModel(pView.getName()))
                    .orElse(Observable.just(Optional.empty()))))
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
  private Observable<Optional<FileObject>> watchViewAODFile(@NotNull Node pViewNode)
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
  private Node getNode(@NotNull FileObject pFileObject)
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

  @AllArgsConstructor
  @VisibleForTesting
  static class FileProvider implements ITestFileProvider
  {

    @NotNull
    private final Node original;
    @NotNull
    private final Project project;

    @Nullable
    @Override
    public FileObject getFile()
    {
      return FileUtil.toFileObject(TestsFolderService.getInstance(project).getTestsFolderForModel(original.getName()));
    }
  }
}
