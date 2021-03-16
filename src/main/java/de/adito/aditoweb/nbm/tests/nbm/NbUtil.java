package de.adito.aditoweb.nbm.tests.nbm;

import org.jetbrains.annotations.NotNull;
import org.netbeans.api.actions.Openable;
import org.openide.filesystems.*;
import org.openide.util.Lookup;

import java.io.File;

/**
 * Utils for Netbeans
 *
 * @author s.seemann, 15.03.2021
 */
public class NbUtil
{

  private NbUtil()
  {
  }

  /**
   * Opens the file
   *
   * @param pFile the file, which should be opened
   */
  public static void open(@NotNull File pFile)
  {
    FileObject fo = FileUtil.toFileObject(pFile);
    open(fo);
  }

  /**
   * Opens the element
   *
   * @param pLookupProvider the element, which should be opened
   */
  public static void open(@NotNull Lookup.Provider pLookupProvider)
  {
    for (Openable openable : pLookupProvider.getLookup().lookupAll(Openable.class))
      openable.open();
  }
}
