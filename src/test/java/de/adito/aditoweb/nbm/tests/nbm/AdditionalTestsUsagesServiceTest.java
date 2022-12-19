package de.adito.aditoweb.nbm.tests.nbm;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.netbeans.api.project.Project;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link AdditionalTestsUsagesService}.
 *
 * @author r.hartinger, 16.12.2022
 */
class AdditionalTestsUsagesServiceTest
{

  /**
   * Tests the method {@link AdditionalTestsUsagesService#findAdditionalUsages(Project, String)}.
   */
  @Nested
  class FindAdditionalUsages
  {
    /**
     * Tests that two files are found if {@code CarPreview_view} is queried.
     *
     * @throws Exception Error converting the URL with the resources directory to URI or deleting the EMPTY file
     */
    @Test
    void shouldFindTwoUsages() throws Exception
    {
      baseFindAdditionalUsages(new ArrayList<>(List.of("CarPreviewNormal.cy.ts", "CarPreviewSpecial.cy.ts")), "CarPreview_view");
    }

    /**
     * Tests that one file are found if {@code CarEdit_view} is queried.
     *
     * @throws Exception Error converting the URL with the resources directory to URI or deleting the EMPTY file
     */
    @Test
    void shouldFindOneUsage() throws Exception
    {
      baseFindAdditionalUsages(new ArrayList<>(List.of("CarEdit.cy.ts")), "CarEdit_view");
    }

    /**
     * Tests that no files are found if {@code CarMain_view} is queried. This folder exists.
     *
     * @throws Exception Error converting the URL with the resources directory to URI or deleting the EMPTY file
     */
    @Test
    void shouldFindNoUsageWithExistingFolder() throws Exception
    {
      baseFindAdditionalUsages(new ArrayList<>(), "CarMain_view");
    }

    /**
     * Tests that no files are found if {@code CarFilter_view} is queried. This folder does not exist.
     *
     * @throws Exception Error converting the URL with the resources directory to URI or deleting the EMPTY file
     */
    @Test
    void shouldFindNoUsageWithNonExistingFolder() throws Exception
    {
      baseFindAdditionalUsages(new ArrayList<>(), "CarFilter_view");
    }

    /**
     * Basic method for testing {@link AdditionalTestsUsagesService#findAdditionalUsages(Project, String)}.
     *
     * @param expectedFilenames the list of expected filenames that the method should give
     * @param nameOfProperty    the name of the property that is given to the method
     * @throws Exception Error converting the URL with the resources directory to URI or deleting the EMPTY file
     */
    private void baseFindAdditionalUsages(@NotNull List<String> expectedFilenames, @NotNull String nameOfProperty) throws Exception
    {
      Project project = ServiceTestUtil.createProjectWithExampleFileStructure();


      AdditionalTestsUsagesService additionalTestsUsagesService = new AdditionalTestsUsagesService();

      // transforming the stream of files that the method returns to a list of filenames for better testing
      List<String> actualFiles = additionalTestsUsagesService.findAdditionalUsages(project, nameOfProperty)
          .map(File::getName)
          .sorted()
          .collect(Collectors.toList());

      Collections.sort(expectedFilenames);

      assertEquals(expectedFilenames, actualFiles);
    }
  }


}