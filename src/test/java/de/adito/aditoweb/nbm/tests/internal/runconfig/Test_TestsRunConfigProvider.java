package de.adito.aditoweb.nbm.tests.internal.runconfig;

import de.adito.nbm.runconfig.api.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.netbeans.api.project.Project;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for {@link TestsRunConfigProvider}
 *
 * @author s.seemann, 01.04.2021
 */
class Test_TestsRunConfigProvider
{
  @Test
  void test_getRunConfigurations()
  {
    TestsRunConfigProvider provider = new TestsRunConfigProvider();

    ISystemInfo systemInfoMock = Mockito.mock(ISystemInfo.class);
    Mockito.when(systemInfoMock.getProject()).thenReturn(Mockito.mock(Project.class));

    ISystemInfo systemInfoMockSecond = Mockito.mock(ISystemInfo.class);
    Mockito.when(systemInfoMockSecond.getProject()).thenReturn(Mockito.mock(Project.class));

    List<IRunConfig> configs = provider.runConfigurations(List.of(systemInfoMock)).blockingFirst();
    assertEquals(1, configs.size());

    configs = provider.runConfigurations(List.of(systemInfoMock, systemInfoMock)).blockingFirst();
    assertEquals(1, configs.size());

    configs = provider.runConfigurations(List.of()).blockingFirst();
    assertEquals(0, configs.size());

    configs = provider.runConfigurations(List.of(systemInfoMock, systemInfoMockSecond)).blockingFirst();
    assertEquals(2, configs.size());
  }
}