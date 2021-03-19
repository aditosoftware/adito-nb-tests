package de.adito.aditoweb.nbm.tests.api;

import org.jetbrains.annotations.Nullable;
import org.openide.filesystems.FileObject;

/**
 * Interface to provide a single folder for selection in the activated nodes
 */
public interface ITestFileProvider
{
  /**
   * @return the file object or null, if it could not be found
   */
  @Nullable
  FileObject getFile();
}
