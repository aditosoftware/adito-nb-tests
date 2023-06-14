package de.adito.aditoweb.nbm.tests.api;

import de.adito.aditoweb.nbm.tests.internal.TestExecutorFacadeImpl;
import lombok.NonNull;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

import java.util.Collection;

/**
 * Facade for executing test files
 *
 * @author s.seemann, 17.03.2021
 */
public interface ITestExecutorFacade
{
  ITestExecutorFacade INSTANCE = new TestExecutorFacadeImpl();

  /**
   * Executes Tests.
   *
   * @param pProject current project
   * @param pFiles   the Files, which should be executed, no folders!
   */
  void executeTests(@NonNull Project pProject, @NonNull Collection<FileObject> pFiles);

  /**
   * Executes all tests, which are in the project found.
   *
   * @param pProject current project
   */
  void executeAllTests(@NonNull Project pProject);

  /**
   * Executes the cypress open command
   *
   * @param pProject current project
   */
  void executeCypressOpen(@NonNull Project pProject);
}
