package de.adito.aditoweb.nbm.tests.nbm.node.modification;

import de.adito.aditoweb.nbm.tests.nbm.TestsFolderService;
import de.adito.notification.INotificationFacade;
import io.reactivex.rxjava3.functions.Consumer;
import org.netbeans.api.project.*;
import org.openide.filesystems.FileObject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.*;

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

      // Add a check for oldname == null, since the analytics plugin showed that oldname can be null at this point. This would basically mean a new file
      // is created -> this class only moves files, so log it and return
      if (oldName == null)
      {
        Logger.getLogger(TestsFolderMover.class.getName()).log(Level.INFO, () -> "UI Testing Plugin: Tried to create file " + newName + " in the " +
            "move listener since the given old name was null.");
        return;
      }

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