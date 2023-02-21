package de.adito.aditoweb.nbm.tests.internal;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.javascript.node.*;
import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.netbeans.api.project.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link TestExecutorFacadeImpl}.
 *
 * @author r.hartinger, 20.02.2023
 */
class TestExecutorFacadeImplTest
{
  
  /**
   * Tests the method {@link TestExecutorFacadeImpl#setup(Project)}
   */
  @Nested
  class Setup
  {
    /**
     * Tests that the methods which take a project will take the root project.
     */
    @Test
    void shouldSetup()
    {

      Project project = Mockito.spy(Project.class);

      Project rootProject = Mockito.spy(Project.class);

      assertNotEquals(project, rootProject, "project and root project should not be the same, important for later tests");


      try (MockedStatic<ProjectUtils> projectUtilsMockedStatic = Mockito.mockStatic(ProjectUtils.class);
           MockedStatic<INodeJSExecutor> nodeJSExecutorMockedStatic = Mockito.mockStatic(INodeJSExecutor.class);
           MockedStatic<INodeJSProvider> nodeJSProviderMockedStatic = Mockito.mockStatic(INodeJSProvider.class))
      {

        projectUtilsMockedStatic.when(() -> ProjectUtils.rootOf(project)).thenReturn(rootProject);

        INodeJSExecutor nodeJSExecutor = Mockito.spy(INodeJSExecutor.class);

        nodeJSExecutorMockedStatic.when(() -> INodeJSExecutor.findInstance(rootProject)).thenReturn(Optional.of(nodeJSExecutor));


        INodeJSEnvironment nodeJSEnvironment = Mockito.spy(INodeJSEnvironment.class);

        INodeJSProvider nodeJSProvider = Mockito.spy(INodeJSProvider.class);
        Mockito.doReturn(Observable.just(Optional.of(nodeJSEnvironment))).when(nodeJSProvider).current();

        nodeJSProviderMockedStatic.when(() -> INodeJSProvider.findInstance(rootProject)).thenReturn(Optional.of(nodeJSProvider));


        TestExecutorFacadeImpl testExecutorFacade = new TestExecutorFacadeImpl();

        assertDoesNotThrow(() -> testExecutorFacade.setup(project));

        nodeJSExecutorMockedStatic.verify(() -> INodeJSExecutor.findInstance(rootProject));
        nodeJSProviderMockedStatic.verify(() -> INodeJSProvider.findInstance(rootProject));
        projectUtilsMockedStatic.verify(() -> ProjectUtils.rootOf(project));
      }
    }
  }

}
