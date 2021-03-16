package de.adito.aditoweb.nbm.tests.nbm;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.nodes.INodeModificationSupport;
import de.adito.aditoweb.nbm.tests.actions.*;
import de.adito.observables.netbeans.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.functions.Consumer;
import org.jetbrains.annotations.*;
import org.netbeans.api.project.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.lookup.*;

import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


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
    return new _NeonViewNode(pNode);
  }

  private static class _NeonViewNode extends FilterNode implements Disposable
  {
    private final CompositeDisposable disposable = new CompositeDisposable();

    public _NeonViewNode(Node pOriginal)
    {
      super(new AbstractNode(Children.LEAF), null,
            new ProxyLookup(new AbstractLookup(new InstanceContent()), Lookups.exclude(pOriginal.getLookup(), Node.class)));

      disposable.add(_watchTestsFolderNode(pOriginal).subscribe(pFolderNodeOpt -> FilterNode.Children.MUTEX.postWriteRequest(() -> {
        changeOriginal(new _FolderNode(pFolderNodeOpt.orElseGet(() -> new AbstractNode(Children.LEAF))), true);
        changeOriginal(pOriginal, false);
      })));

      // Tests-Folder-Mover
      disposable.add(_watchViewAODFile(pOriginal)
                         .debounce(500, TimeUnit.MILLISECONDS)
                         .subscribe(new _TestsFolderMover()));
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
    private Observable<Optional<Node>> _watchTestsFolderNode(@NotNull Node pViewNode)
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
          .distinctUntilChanged()

          // FileObject to Node
          .switchMap(pFileOpt -> pFileOpt
              .map(this::_getNode)
              .map(pNode -> NodeObservable.create(pNode)
                  .map(Optional::of))
              .orElseGet(() -> Observable.just(Optional.empty())));
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
  }

  /**
   * Node for test folders
   */
  private static class _FolderNode extends FilterNode
  {
    public _FolderNode(@NotNull Node pOriginal)
    {
      super(pOriginal, new _Children(pOriginal), new ProxyLookup(Lookups.fixed((AbstractFolderBasedAction.IFolderProvider)
                                                                                   () -> pOriginal.getLookup().lookup(FileObject.class)),
                                                                 pOriginal.getLookup()));
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
            return new _FolderNode(pNode);
        }
        return super.copyNode(pNode);
      }
    }
  }

  /**
   * Consumer that accepts a new name for a view folder
   */
  private static class _TestsFolderMover implements Consumer<Optional<FileObject>>
  {
    private AtomicReference<Project> projectDirRef;
    private AtomicReference<String> oldNameRef;

    @Override
    public void accept(Optional<FileObject> pNewData)
    {
      synchronized (_TestsFolderMover.class)
      {
        if (oldNameRef == null || projectDirRef == null) // initial auf alle werte warten
        {
          projectDirRef = pNewData
              .map(FileOwnerQuery::getOwner)
              .map(AtomicReference::new)
              .orElse(null);
          oldNameRef = pNewData
              .map(FileObject::getName)
              .map(AtomicReference::new)
              .orElse(null);
          return;
        }

        Project project = projectDirRef.get();
        String newName = pNewData.map(FileObject::getName).orElse(null);
        String oldName = oldNameRef.getAndSet(newName);

        // Nothing to move
        if (Objects.equals(oldName, newName))
          return;

        try
        {
          TestsFolderService.getInstance(project).renameModel(oldName, newName);
        }
        catch (IOException e)
        {
          e.printStackTrace(); // TODO errorhandling
        }
      }
    }
  }
}
