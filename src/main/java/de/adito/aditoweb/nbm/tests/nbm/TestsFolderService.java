package de.adito.aditoweb.nbm.tests.nbm;

import de.adito.observables.netbeans.*;
import de.adito.util.reactive.cache.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.*;
import lombok.NonNull;
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
  private static final String _TEST_FOLDER = _CYPRESS + File.separator + "e2e";
  private static final String _GLOBAL_TESTS = _TEST_FOLDER + File.separator + "globalTests";
  private static final String _SINGLE_TESTS = _TEST_FOLDER + File.separator + "singleTests";

  private Project project;

  private final ObservableCache cache = new ObservableCache();
  private final CompositeDisposable disposable = new CompositeDisposable();

  @NonNull
  public static TestsFolderService getInstance(@NonNull Project pProject)
  {
    TestsFolderService service = pProject.getLookup().lookup(TestsFolderService.class);
    if (service == null)
      throw new RuntimeException("Failed to retrieve tests service for project " + pProject + ". " +
                                     "Please reinstall your tests plugin to provide this service.");
    return service;
  }

  /**
   * Watches the instance for a specific project
   *
   * @param pProject Project
   * @return the observable to watch for instances
   */
  @NonNull
  public static Observable<Optional<TestsFolderService>> observe(@NonNull Project pProject)
  {
    return LookupResultObservable.create(pProject.getLookup(), TestsFolderService.class)
        .map(pServices -> pServices.stream().findFirst());
  }

  @SuppressWarnings("unused") // ServiceProvider
  public TestsFolderService()
  {
  }

  @SuppressWarnings("unused") // ServiceProvider with given Project
  public TestsFolderService(@NonNull Project pProject)
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

  @NonNull
  public Observable<Optional<FileObject>> observeCypressFolder()
  {
    return cache.calculate("testsFolder", () -> FileObservable.create(_getCypressFolderFile())
        .map(pFileOpt -> pFileOpt.map(FileUtil::toFileObject)));
  }

  /**
   * Observes the {@link TestsFolderService#_TEST_FOLDER} of this project
   *
   * @return Observable with the folder as content
   */
  @NonNull
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
  @NonNull
  public Observable<Optional<FileObject>> observeTestsFolderForModel(@NonNull String pModelName)
  {
    return cache.calculate("testsFolderForModel_" + pModelName, () -> FileObservable.create(_getTestsFolderFileForModel(pModelName))
        .map(pFileOpt -> pFileOpt.map(FileUtil::toFileObject)));
  }

  /**
   * Observes the {@link TestsFolderService#_GLOBAL_TESTS} folder
   *
   * @return Observable with the folder as content. Triggers on Rename/Move etc.
   */
  @NonNull
  public Observable<Optional<FileObject>> observeGlobalTestsFolder()
  {
    return cache.calculate("testsGlobalTestsFolder", () -> FileObservable.create(_getGlobalTestsFolder())
        .map(pFileOpt -> pFileOpt.map(FileUtil::toFileObject)));
  }

  /**
   * Returns the file for the location of the tests-folder of one model.
   *
   * @param pModelName name of the model
   * @return the file
   */
  @NonNull
  public File getTestsFolderForModel(@NonNull String pModelName)
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
  public void renameModel(@NonNull String pOldName, @Nullable String pNewName) throws IOException
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

  /**
   * Creates the global-tests folder in the project directory
   */
  public void createGlobalTestsFolder()
  {
    //noinspection ResultOfMethodCallIgnored
    _getGlobalTestsFolder().mkdirs();
  }

  /**
   * Creates the cypress folder in the project directory
   */
  public void createCypressFolder()
  {
    //noinspection ResultOfMethodCallIgnored
    _getCypressFolderFile().mkdirs();
  }


  @NonNull
  private File _getTestsFolderFileForModel(@NonNull String pModelName)
  {
    return new File(FileUtil.toFile(project.getProjectDirectory()), _SINGLE_TESTS + File.separator + pModelName);
  }

  @NonNull
  private File _getGlobalTestsFolder()
  {
    return new File(FileUtil.toFile(project.getProjectDirectory()), _GLOBAL_TESTS);
  }

  @NonNull
  private File _getTestsFolderFile()
  {
    return new File(FileUtil.toFile(project.getProjectDirectory()), _TEST_FOLDER);
  }

  @NonNull
  private File _getCypressFolderFile()
  {
    return new File(FileUtil.toFile(project.getProjectDirectory()), _CYPRESS);
  }
}
