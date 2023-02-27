package de.adito.aditoweb.nbm.tests.actions;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.common.IProjectQuery;
import de.adito.aditoweb.nbm.tests.api.ITestFileProvider;
import de.adito.aditoweb.nbm.tests.nbm.TestsFolderService;
import org.jetbrains.annotations.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.netbeans.api.project.*;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

import java.io.File;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

/**
 * Test class for {@link AbstractFolderBasedAction}.
 *
 * @author r.hartinger, 20.02.2023
 */
class AbstractFolderBasedActionTest
{

  /**
   * Tests the method {@link AbstractFolderBasedAction#findFileOnFileSystem(Node)}.
   */
  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class FindFileOnFileSystem
  {
    /**
     * @return the arguments for {@link #shouldFindFileOnFileSystem(File, int, int, ITestFileProvider, Project)}
     */
    @NotNull
    private Stream<Arguments> shouldFindFileOnFileSystem()
    {
      Project project = Mockito.spy(Project.class);

      ITestFileProvider fileProviderWithoutFile = Mockito.mock(ITestFileProvider.class);
      Mockito.doReturn(null).when(fileProviderWithoutFile).getFile();

      ITestFileProvider fileProviderWithFile = Mockito.mock(ITestFileProvider.class);
      Mockito.doReturn(FileUtil.toFileObject(new File("").getAbsoluteFile())).when(fileProviderWithFile).getFile();

      File projectDir = new File("").getAbsoluteFile();
      File fileWithEnding = new File("pom.xml");
      File fileWithoutEnding = new File("pom");

      return Stream.of(
          // no test file provider
          Arguments.of(fileWithEnding, 1, 0, null, null),
          Arguments.of(fileWithoutEnding, 1, 1, null, project),

          // test file provider without file
          Arguments.of(fileWithEnding, 1, 0, fileProviderWithoutFile, null),
          Arguments.of(fileWithoutEnding, 1, 1, fileProviderWithoutFile, project),

          // test file provider with file
          Arguments.of(projectDir, 0, 0, fileProviderWithFile, null),
          Arguments.of(projectDir, 0, 0, fileProviderWithFile, project)
      );
    }

    /**
     * Tests the method call.
     *
     * @param pExpected               the expected file that should be returned by the method
     * @param pCallProjectQuery       the number of times {@link AbstractFolderBasedAction#findFile(Node)} and
     *                                {@link IProjectQuery#findProjects(Lookup, IProjectQuery.ReturnType)} should be called
     * @param pCallTestFolderForModel the number of times of methods related to the class {@link TestsFolderService} and
     *                                {@link ProjectUtils#rootOf(Project)} are called
     * @param pTestFileProvider       the {@link ITestFileProvider} that should be returned by the node lookup
     * @param pProject                the {@link Project} that should be returned by {@link  IProjectQuery#findProjects(Lookup, IProjectQuery.ReturnType)}
     */
    @ParameterizedTest
    @MethodSource
    void shouldFindFileOnFileSystem(@NotNull File pExpected, int pCallProjectQuery, int pCallTestFolderForModel,
                                    @Nullable ITestFileProvider pTestFileProvider, @Nullable Project pProject)
    {
      Lookup nodeLookup = Mockito.mock(Lookup.class);
      Mockito.doReturn(pTestFileProvider).when(nodeLookup).lookup(ITestFileProvider.class);

      Node node = Mockito.mock(Node.class);
      Mockito.doReturn(nodeLookup).when(node).getLookup();

      AbstractFolderBasedAction abstractFolderBasedAction = Mockito.spy(createAbstractFolderBasedAction());
      Mockito.doReturn(new File("pom.xml")).when(abstractFolderBasedAction).findFile(node);

      try (MockedStatic<IProjectQuery> projectQueryMockedStatic = Mockito.mockStatic(IProjectQuery.class);
           MockedStatic<ProjectUtils> projectUtilsMockedStatic = Mockito.mockStatic(ProjectUtils.class))
      {

        IProjectQuery projectQuery = Mockito.mock(IProjectQuery.class);
        Mockito.doReturn(pProject).when(projectQuery).findProjects(node, IProjectQuery.ReturnType.MULTIPLE_TO_NULL);

        projectQueryMockedStatic.when(IProjectQuery::getInstance).thenReturn(projectQuery);


        TestsFolderService testsFolderService = Mockito.spy(TestsFolderService.class);
        Mockito.doAnswer(pInvocationOnMock -> new File(pInvocationOnMock.getArgument(0, String.class))).when(testsFolderService).getTestsFolderForModel(any());

        Lookup projectLookup = Mockito.mock(Lookup.class);
        Mockito.doReturn(testsFolderService).when(projectLookup).lookup(TestsFolderService.class);

        Project project = Mockito.mock(Project.class);
        Mockito.doReturn(projectLookup).when(project).getLookup();

        projectUtilsMockedStatic.when(() -> ProjectUtils.rootOf(any())).thenReturn(project);


        assertEquals(pExpected, abstractFolderBasedAction.findFileOnFileSystem(node));


        Mockito.verify(testsFolderService, Mockito.times(pCallTestFolderForModel)).getTestsFolderForModel(any());

        Mockito.verify(nodeLookup).lookup(ITestFileProvider.class);
        Mockito.verifyNoMoreInteractions(nodeLookup);

        Mockito.verify(projectLookup, Mockito.times(pCallTestFolderForModel)).lookup(TestsFolderService.class);
        Mockito.verifyNoMoreInteractions(projectLookup);

        projectUtilsMockedStatic.verify(() -> ProjectUtils.rootOf(any()), Mockito.times(pCallTestFolderForModel));

        Mockito.verify(abstractFolderBasedAction, Mockito.times(pCallProjectQuery)).findFile(node);

        Mockito.verify(projectQuery, Mockito.times(pCallProjectQuery)).findProjects(node, IProjectQuery.ReturnType.MULTIPLE_TO_NULL);
        Mockito.verifyNoMoreInteractions(projectQuery);

        projectQueryMockedStatic.verify(IProjectQuery::getInstance, Mockito.times(pCallProjectQuery));
      }
    }
  }


  /**
   * Tests the method {@link AbstractFolderBasedAction#getHelpCtx()}.
   */
  @Nested
  class GetHelpCtx
  {
    /**
     * Tests that no help context will be returned.
     */
    @Test
    void shouldGetHelpCtx()
    {
      assertNull(createAbstractFolderBasedAction().getHelpCtx());
    }
  }


  /**
   * Creates a simple instance of {@link AbstractFolderBasedAction}. All {@code abstract} methods will throw an {@link UnsupportedOperationException}.
   *
   * @return the created instance
   */
  @NotNull
  private static AbstractFolderBasedAction createAbstractFolderBasedAction()
  {
    return new AbstractFolderBasedAction()
    {
      @Override
      protected void performAction0(@NotNull Node pNode, @NotNull File pParent, @NotNull String pName)
      {
        throw new UnsupportedOperationException("not needed for this junit test");
      }

      @NotNull
      @Override
      protected String getInputLineTitle()
      {
        throw new UnsupportedOperationException("not needed for this junit test");
      }

      @Override
      public String getName()
      {
        throw new UnsupportedOperationException("not needed for this junit test");
      }
    };
  }

}
