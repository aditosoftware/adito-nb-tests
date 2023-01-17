package de.adito.aditoweb.nbm.tests.nbm;

import org.mockito.Mockito;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Util class for testing the services.
 *
 * @author r.hartinger, 16.12.2022
 */
public class ServiceTestUtil
{
  /**
   * Creates a {@link Project} with a {@link Lookup} that returns a {@link TestsFolderService}.
   * This {@link Project} also have the following file structure and knows their project directory ({@code MyProject}).
   *
   * <pre>
   * MyProject
   * └───cypress
   *     └───e2e
   *         └───singleTests
   *             ├───CarEdit_view
   *             │       CarEdit.cy.ts
   *             │
   *             ├───CarMain_view
   *             └───CarPreview_view
   *                     CarPreviewNormal.cy.ts
   *                     CarPreviewSpecial.cy.ts
   * </pre>
   * This file structure can be found in the {@code resources} directory. If you modify the file structure, please adjust the graphic above.
   * <p>
   * On windows: go to the parent directory of {@code MyProject} and execute {@code tree /F MyProject} on the command line.
   *
   * @return the created project
   * @throws Exception Error converting the URL with the resources directory to URI or deleting the EMPTY file
   */
  static Project createProjectWithExampleFileStructure() throws Exception
  {
    String basePath = ServiceTestUtil.class.getPackageName().replace(".", "/") + "/MyProject";
    URL projectFolderUrl = ServiceTestUtil.class.getClassLoader().getResource(basePath);
    assertNotNull(projectFolderUrl, "url of project folder should exist");

    File projectFolder = new File(projectFolderUrl.toURI());

    // The empty file is just there because of the maven build. If this file would not be there, no folder would be copied.
    // we don't need this file for our test, so we delete it.
    URL emptyUrl = ServiceTestUtil.class.getClassLoader().getResource(basePath + "/cypress/e2e/singleTests/CarMain_view/EMPTY");
    //  We are entering the method multiple times in a test run. That means that the chances are high that the file is already deleted, and we don't need to delete it.
    if (emptyUrl != null)
      Files.delete(new File(emptyUrl.toURI()).toPath());


    Project project = Mockito.spy(Project.class);

    TestsFolderService testsFolderService = new TestsFolderService(project);

    Lookup lookup = Mockito.spy(Lookup.class);
    Mockito.doReturn(testsFolderService).when(lookup).lookup(TestsFolderService.class);

    Mockito.doReturn(lookup).when(project).getLookup();
    Mockito.doReturn(FileUtil.toFileObject(projectFolder)).when(project).getProjectDirectory();

    return project;
  }


}
