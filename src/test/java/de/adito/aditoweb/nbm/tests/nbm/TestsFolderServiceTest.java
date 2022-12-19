package de.adito.aditoweb.nbm.tests.nbm;

import org.jetbrains.annotations.*;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;

import java.io.File;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link TestsFolderService}.
 *
 * @author r.hartinger, 16.12.2022
 */
class TestsFolderServiceTest
{

  /**
   * Tests the method {@link TestsFolderService#getInstance(Project)}.
   */
  @Nested
  class GetInstance
  {
    /**
     * Tests that the method will throw a {@link RuntimeException}, if no {@link TestsFolderService} can be found in the {@link Lookup}.
     */
    @Test
    void shouldFailGettingInstance()
    {
      Project project = Mockito.spy(Project.class);

      RuntimeException actual = assertThrows(RuntimeException.class, () -> baseGetInstance(project, null));
      assertEquals("Failed to retrieve tests service for project " + project + ". " +
                       "Please reinstall your tests plugin to provide this service.", actual.getMessage());
    }

    /**
     * Tests that an instance will be returned.
     */
    @Test
    void shouldGetInstance()
    {
      Project project = Mockito.spy(Project.class);

      TestsFolderService testsFolderService = new TestsFolderService(project);

      assertNotNull(baseGetInstance(project, testsFolderService));
    }

    /**
     * Basic method for testing {@link TestsFolderService#getInstance(Project)}.
     * The given project will be added a lookup.
     *
     * @param project            the project that will be given to the method
     * @param testsFolderService the service that the {@link Lookup} of the project should return
     * @return the result of the method call
     */
    @NotNull
    private TestsFolderService baseGetInstance(@NotNull Project project, @Nullable TestsFolderService testsFolderService)
    {
      Lookup lookup = Mockito.spy(Lookup.class);
      Mockito.doReturn(testsFolderService).when(lookup).lookup(TestsFolderService.class);

      Mockito.doReturn(lookup).when(project).getLookup();


      return TestsFolderService.getInstance(project);
    }
  }


  /**
   * Tests the method {@link TestsFolderService#getTestsFolderForModel(String)}.
   */
  @Nested
  class GetTestsFolderForModel
  {

    /**
     * Getting the file object for the model {@code CarFilter_view} and tests that the file does not exist.
     *
     * @throws Exception Error converting the URL with the resources directory to URI or deleting the EMPTY file
     */
    @Test
    void shouldGetTestsFolderForCarFilterView() throws Exception
    {
      baseGetTestsFolderForModel(false, Paths.get("MyProject", "cypress", "e2e", "singleTests", "CarFilter_view"), "CarFilter_view");
    }

    /**
     * Getting the file object for the model {@code CarPreview_view} and tests that the file does exist.
     *
     * @throws Exception Error converting the URL with the resources directory to URI or deleting the EMPTY file
     */
    @Test
    void shouldGetTestsFolderForCarPreviewView() throws Exception
    {
      baseGetTestsFolderForModel(true, Paths.get("MyProject", "cypress", "e2e", "singleTests", "CarPreview_view"), "CarPreview_view");
    }

    /**
     * Getting the file object for the model {@code CarPreview_view} and tests that the file does exist.
     *
     * @throws Exception Error converting the URL with the resources directory to URI or deleting the EMPTY file
     */
    @Test
    void shouldGetTestsFolderForCarMainView() throws Exception
    {
      baseGetTestsFolderForModel(true, Paths.get("MyProject", "cypress", "e2e", "singleTests", "CarMain_view"), "CarMain_view");
    }

    /**
     * Getting the file object for the model {@code CarEdit_view} and tests that the file not exist.
     *
     * @throws Exception Error converting the URL with the resources directory to URI or deleting the EMPTY file
     */
    @Test
    void shouldGetTestsFolderForCarEditView() throws Exception
    {
      baseGetTestsFolderForModel(true, Paths.get("MyProject", "cypress", "e2e", "singleTests", "CarEdit_view"), "CarEdit_view");
    }

    /**
     * Basic method for testing {@link TestsFolderService#getTestsFolderForModel(String)}.
     *
     * @param shouldExist   indicator if the file should exist or not
     * @param endOfFilePath the expected end part of the {@link File#getAbsolutePath()}.
     * @param modelName     the name of the file that should be found
     * @throws Exception Error converting the URL with the resources directory to URI or deleting the EMPTY file
     */
    private void baseGetTestsFolderForModel(boolean shouldExist, @NotNull Path endOfFilePath, @NotNull String modelName) throws Exception
    {
      TestsFolderService testsFolderService = TestsFolderService.getInstance(ServiceTestUtil.createProjectWithExampleFileStructure());

      File file = testsFolderService.getTestsFolderForModel(modelName);
      
      assertAll(() -> assertTrue(file.getAbsolutePath().endsWith(endOfFilePath.toString()), "file should end with a specific path: expected " + endOfFilePath + " actual " + file.getAbsolutePath()),
                () -> assertFalse(file.isFile(), "file should not be a file"),
                () -> assertEquals(shouldExist, file.isDirectory(), "file should be a directory, if it exists"),
                () -> assertEquals(shouldExist, file.exists(), "should file exist"));
    }
  }

}
