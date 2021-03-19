package de.adito.aditoweb.nbm.tests.nbm.nodemodification;

import de.adito.aditoweb.nbm.tests.nbm.TestsFolderService;
import de.adito.notification.INotificationFacade;
import io.reactivex.rxjava3.functions.Consumer;
import org.netbeans.api.project.*;
import org.openide.filesystems.FileObject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Consumer that accepts a new name for a view folder
 *
 * @author s.seemann, 17.03.2021
 */
class TestsFolderMover implements Consumer<Optional<FileObject>>
{
  private AtomicReference<Project> projectDirRef;
  private AtomicReference<String> oldNameRef;

  @Override
  public void accept(Optional<FileObject> pNewData)
  {
    synchronized (TestsFolderMover.class)
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
      catch (IOException pE)
      {
        INotificationFacade.INSTANCE.error(pE);
      }
    }
  }
}