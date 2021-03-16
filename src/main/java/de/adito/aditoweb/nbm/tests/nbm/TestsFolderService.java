package de.adito.aditoweb.nbm.tests.nbm;

import de.adito.observables.netbeans.FileObservable;
import de.adito.util.reactive.cache.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.*;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.*;
import org.netbeans.api.project.Project;
import org.openide.filesystems.*;
import org.openide.util.lookup.ServiceProvider;

import java.io.*;
import java.util.Optional;

/**
 * Contains all necessary information about the cypress-Folder in a single project
 *
 * @author s.seemann, 04.03.2021
 */
@ServiceProvider(service = TestsFolderService.class, path = "Projects/de-adito-project/Lookup")
public class TestsFolderService implements Disposable
{
  private static final String _CYPRESS = "cypress";
  private static final String _TEST_FOLDER = _CYPRESS + File.separator + "integration";
  private static final String _GLOBAL_TESTS = _TEST_FOLDER + File.separator + "globalTests";
  private static final String _SINGLE_TESTS = _TEST_FOLDER + File.separator + "singleTests";

  private Project project;

  private final ObservableCache cache = new ObservableCache();
  private final CompositeDisposable disposable = new CompositeDisposable();

  @NotNull
  public static TestsFolderService getInstance(@NotNull Project pProject)
  {
    TestsFolderService service = pProject.getLookup().lookup(TestsFolderService.class);
    if (service == null)
      throw new RuntimeException("Failed to retrieve tests service for project " + pProject + ". " +
                                     "Please reinstall your tests plugin to provide this service.");
    return service;
  }

  @SuppressWarnings("unused") // ServiceProvider
  public TestsFolderService()
  {
  }

  @SuppressWarnings("unused") // ServiceProvider with given Project
  public TestsFolderService(@NotNull Project pProject)
  {
    project = pProject;
    disposable.add(new ObservableCacheDisposable(cache));
  }

  @Override
  public void dispose()
  {
    if (!disposable.isDisposed())
      disposable.dispose();
  }

  @Override
  public boolean isDisposed()
  {
    return disposable.isDisposed();
  }

  /**
   * Observes the {@link TestsFolderService#_TEST_FOLDER} of this project
   *
   * @return Observable with the folder as content
   */
  @NotNull
  public Observable<Optional<FileObject>> observeTestsFolder()
  {
    return cache.calculate("testsFolder", () -> FileObservable.create(_getTestsFolderFile())
        .map(pFileOpt -> pFileOpt.map(FileUtil::toFileObject)));
  }

  /**
   * Observes the {@link TestsFolderService#_TEST_FOLDER} of a given model name.
   *
   * @param pModelName the model name
   * @return Observable with the folder as content. Triggers on Rename/Move etc.
   */
  @NotNull
  public Observable<Optional<FileObject>> observeTestsFolderForModel(@NotNull String pModelName)
  {
    return cache.calculate("testsFolderForModel_" + pModelName, () -> FileObservable.create(_getTestsFolderFileForModel(pModelName))
        .map(pFileOpt -> pFileOpt.map(FileUtil::toFileObject)));
  }

  /**
   * Returns the file for the location of the tests-folder of one model.
   *
   * @param pModelName name of the model
   * @return the file
   */
  @NotNull
  public File getTestsFolderForModel(@NotNull String pModelName)
  {
    return _getTestsFolderFileForModel(pModelName);
  }

  /**
   * Renames a Model.
   *
   * @param pOldName the old name
   * @param pNewName the new name or null, if the element should be deleted
   * @throws IOException if something goes wrong
   */
  public void renameModel(@NotNull String pOldName, @Nullable String pNewName) throws IOException
  {
    // nothing to change
    if (pOldName.equals(pNewName))
      return;

    File oldFolder = _getTestsFolderFileForModel(pOldName);

    // Old does not exist or is not modifiable
    if (!oldFolder.exists() || !oldFolder.canWrite() || !oldFolder.canRead())
      return;

    if (pNewName != null)
      FileUtils.moveDirectory(oldFolder, _getTestsFolderFileForModel(pNewName));
    else
      FileUtils.deleteDirectory(oldFolder);
  }

  @NotNull
  private File _getTestsFolderFileForModel(@NotNull String pModelName)
  {
    return new File(FileUtil.toFile(project.getProjectDirectory()), _SINGLE_TESTS + File.separator + pModelName);
  }

  @NotNull
  private File _getTestsFolderFile()
  {
    return new File(FileUtil.toFile(project.getProjectDirectory()), _TEST_FOLDER);
  }
}
