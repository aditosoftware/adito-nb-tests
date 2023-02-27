package de.adito.aditoweb.nbm.tests.nbm.node.modification;

import org.jetbrains.annotations.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.netbeans.api.project.*;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * Test class for {@link ViewNodeModificationSupport}.
 *
 * @author r.hartinger, 20.02.2023
 */
class ViewNodeModificationSupportTest
{

  /**
   * Tests the method {@link ViewNodeModificationSupport#canModify(Node)}.
   */
  @Nested
  class CanModify
  {
    /**
     * Tests that modify is always possible and the node is never needed for any checks.
     */
    @Test
    void shouldCanModify()
    {
      Node node = Mockito.mock(Node.class);

      ViewNodeModificationSupport viewNodeModificationSupport = new ViewNodeModificationSupport();

      assertTrue(viewNodeModificationSupport.canModify(node));

      Mockito.verifyNoInteractions(node);
    }
  }


  /**
   * Tests the method {@link ViewNodeModificationSupport#modify(Node, Map)}.
   */
  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class Modify
  {

    /**
     * @return the arguments for {@link #shouldModify(boolean, int, int, int, Map, Project)}.
     */
    @NotNull
    private Stream<Arguments> shouldModify()
    {
      Project project = Mockito.spy(Project.class);

      Map<Object, Object> nullValueMap = new HashMap<>();
      nullValueMap.put("sourceProject", null);


      return Stream.of(
          // no map with arguments
          Arguments.of(false, 0, 1, 1, new HashMap<>(), null),
          Arguments.of(true, 1, 1, 4, new HashMap<>(), project),

          // wrong key in map
          Arguments.of(false, 0, 1, 1, Map.of("another key", project), null),
          Arguments.of(true, 1, 1, 4, Map.of("another key", project), project),

          // correct key, but wrong type
          Arguments.of(false, 0, 1, 1, nullValueMap, null),
          Arguments.of(true, 1, 1, 4, nullValueMap, project),
          Arguments.of(false, 0, 1, 1, Map.of("sourceProject", "not a project"), null),
          Arguments.of(true, 1, 1, 4, Map.of("sourceProject", "not a project"), project),

          // correct key and type
          Arguments.of(true, 0, 0, 3, Map.of("sourceProject", project), null)
      );
    }

    /**
     * Tests the modify method.
     *
     * @param pIsNeonViewNode          if the result of the method is expected to be of the class {@link NeonViewNode} or not.
     * @param pCallRootOf              number of times {@link ProjectUtils#rootOf(Project)} is called
     * @param pCallOfLookupProject     number of times {@link Lookup#lookup(Class)} with the class {@link Project} is called
     * @param pCallOfGetLookup         number of times {@link Node#getLookup()} is called
     * @param pAttributes              the attributes passed the method call
     * @param pProjectReturnedByLookup the project that should be returned by {@link Lookup#lookup(Class)}
     */
    @ParameterizedTest
    @MethodSource
    void shouldModify(boolean pIsNeonViewNode, int pCallRootOf, int pCallOfLookupProject, int pCallOfGetLookup,
                      @NotNull Map<Object, Object> pAttributes, @Nullable Project pProjectReturnedByLookup)
    {
      @SuppressWarnings("unchecked")
      Lookup.Result<DataObject> result = Mockito.mock(Lookup.Result.class);

      Lookup lookup = Mockito.mock(Lookup.class);
      Mockito.doReturn(result).when(lookup).lookupResult(DataObject.class);
      Mockito.doReturn(pProjectReturnedByLookup).when(lookup).lookup(Project.class);

      Node node = Mockito.mock(Node.class);
      Mockito.doReturn(lookup).when(node).getLookup();


      try (MockedStatic<ProjectUtils> projectUtilsMockedStatic = Mockito.mockStatic(ProjectUtils.class))
      {

        Project project = Mockito.spy(Project.class);
        projectUtilsMockedStatic.when(() -> ProjectUtils.rootOf(isNotNull())).thenReturn(project);
        projectUtilsMockedStatic.when(() -> ProjectUtils.rootOf(isNull()))
            .thenThrow(new IllegalArgumentException("we do not want to call root of with any null value"));


        ViewNodeModificationSupport viewNodeModificationSupport = new ViewNodeModificationSupport();

        Node actualNode = viewNodeModificationSupport.modify(node, pAttributes);
        assertNotNull(actualNode, "should not be null");
        assertEquals(pIsNeonViewNode, actualNode instanceof NeonViewNode, "is the actual node " + node + " instanceOf NeonViewNode");

        projectUtilsMockedStatic.verify(() -> ProjectUtils.rootOf(any()), Mockito.times(pCallRootOf));
        
        Mockito.verify(lookup, Mockito.times(pCallOfLookupProject)).lookup(Project.class);

        Mockito.verify(node, Mockito.times(pCallOfGetLookup)).getLookup();
      }
    }
  }

}
