package de.adito.aditoweb.nbm.tests.internal.runconfig;

import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.netbeans.api.project.*;
import org.openide.util.lookup.Lookups;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for {@link AllTestsRunConfig}
 *
 * @author s.seemann, 01.04.2021
 */
class Test_AllTestsRunConfig
{
  private static final String _PROJECT1_NAME = "Project 1";
  private static final String _PROJECT2_NAME = "Project 2";

  @Test
  void test_displayName()
  {
    ProjectInformation projectInformation1 = Mockito.mock(ProjectInformation.class);
    Mockito.when(projectInformation1.getDisplayName()).thenReturn(_PROJECT1_NAME);

    ProjectInformation projectInformation2 = Mockito.mock(ProjectInformation.class);
    Mockito.when(projectInformation2.getDisplayName()).thenReturn(_PROJECT2_NAME);

    Project project1 = Mockito.mock(Project.class);
    Mockito.when(project1.getLookup()).thenReturn(Lookups.fixed(projectInformation1));

    Project project2 = Mockito.mock(Project.class);
    Mockito.when(project2.getLookup()).thenReturn(Lookups.fixed(projectInformation2));

    AllTestsRunConfig runConfig = new AllTestsRunConfig(project1, Observable.just(List.of(project1)));
    String displayName = runConfig.displayName().blockingFirst();
    assertEquals("Run All Tests", displayName);

    runConfig = new AllTestsRunConfig(project1, Observable.just(List.of()));
    displayName = runConfig.displayName().blockingFirst();
    assertEquals("Run All Tests", displayName);

    runConfig = new AllTestsRunConfig(project1, Observable.just(List.of(project1, project2)));
    displayName = runConfig.displayName().blockingFirst();
    assertEquals("Run All Tests### (" + _PROJECT1_NAME + ")", displayName);

    runConfig = new AllTestsRunConfig(project2, Observable.just(List.of(project1, project2)));
    displayName = runConfig.displayName().blockingFirst();
    assertEquals("Run All Tests### (" + _PROJECT2_NAME + ")", displayName);
  }
}