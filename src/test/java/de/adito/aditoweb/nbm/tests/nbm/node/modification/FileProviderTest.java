package de.adito.aditoweb.nbm.tests.nbm.node.modification;

import de.adito.aditoweb.nbm.tests.nbm.TestsFolderService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.netbeans.api.project.Project;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for {@link de.adito.aditoweb.nbm.tests.nbm.node.modification.NeonViewNode.FileProvider}.
 *
 * @author r.hartinger, 14.02.2023
 */
class FileProviderTest
{

  /**
   * Tests the method {@link NeonViewNode.FileProvider#getFile()}
   */
  @Nested
  class GetFile
  {

    /**
     * Tests that a file will be returned by the method call via the {@link TestsFolderService}.
     */
    @Test
    void shouldGetFile()
    {
      TestsFolderService testsFolderService = Mockito.spy(new TestsFolderService());
      Mockito.doReturn(new File("").getAbsoluteFile()).when(testsFolderService).getTestsFolderForModel("CarFilter_view");

      Lookup projectLookup = Mockito.mock(Lookup.class);
      Mockito.doReturn(testsFolderService).when(projectLookup).lookup(TestsFolderService.class);

      Project project = Mockito.mock(Project.class);
      Mockito.doReturn(projectLookup).when(project).getLookup();


      Node node = Mockito.mock(Node.class);
      Mockito.doReturn("CarFilter_view").when(node).getName();


      NeonViewNode.FileProvider fileProvider = new NeonViewNode.FileProvider(node, project);
      assertNotNull(fileProvider.getFile());

      Mockito.verify(testsFolderService).getTestsFolderForModel("CarFilter_view");
    }
  }

}
