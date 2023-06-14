package de.adito.aditoweb.nbm.tests.internal.runconfig;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.javascript.node.*;
import de.adito.nbm.runconfig.api.*;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test for {@link TestsRunConfigProvider}
 *
 * @author s.seemann, 01.04.2021
 */
class TestsRunConfigProviderTest
{
  @Test
  void test_getRunConfigurations() throws IOException
  {
    // create files
    File projectRoot = Files.createTempDirectory("Test_TestsRunConfigProvider").toFile();
    assertTrue(new File(projectRoot, "node_modules/cypress").mkdirs());
    assertTrue(new File(projectRoot, "package.json").createNewFile());

    try (MockedStatic<INodeJSProvider> nodeJSProviderMockedStatic = Mockito.mockStatic(INodeJSProvider.class))
    {

      nodeJSProviderMockedStatic.when(() -> INodeJSProvider.findInstance(any())).thenReturn(Optional.of(new _INodeJSProvider()));
      TestsRunConfigProvider provider = new TestsRunConfigProvider();

      Project projectMock = mock(Project.class);
      when(projectMock.getProjectDirectory()).thenReturn(FileUtil.toFileObject(projectRoot));
      ISystemInfo systemInfoMock = Mockito.mock(ISystemInfo.class);
      when(systemInfoMock.getProject()).thenReturn(projectMock);

      Project projectMockSecond = mock(Project.class);
      when(projectMockSecond.getProjectDirectory()).thenReturn(FileUtil.toFileObject(projectRoot));
      ISystemInfo systemInfoMockSecond = Mockito.mock(ISystemInfo.class);
      when(systemInfoMockSecond.getProject()).thenReturn(projectMockSecond);

      // one system-info
      List<IRunConfig> configs = provider.runConfigurations(List.of(systemInfoMock)).blockingFirst();
      assertEquals(2, configs.size());

      // two equal system infos
      configs = provider.runConfigurations(List.of(systemInfoMock, systemInfoMock)).blockingFirst();
      assertEquals(2, configs.size());

      // no system infos
      configs = provider.runConfigurations(List.of()).blockingFirst();
      assertEquals(0, configs.size());

      // two different system infos
      configs = provider.runConfigurations(List.of(systemInfoMock, systemInfoMockSecond)).blockingFirst();
      assertEquals(4, configs.size());

      // No node-js present
      FileUtils.deleteDirectory(projectRoot);
      configs = provider.runConfigurations(List.of(systemInfoMock, systemInfoMockSecond)).blockingFirst();
      assertEquals(0, configs.size());
    }
  }

  /**
   * Pseudo-INodeJSProvider
   */
  @SuppressWarnings("ConstantConditions")
  private static class _INodeJSProvider implements INodeJSProvider
  {
    @NonNull
    @Override
    public Observable<Optional<INodeJSEnvironment>> current()
    {
      return Observable.just(Optional.of(new INodeJSEnvironment()
      {
        @NonNull
        @Override
        public File getPath()
        {
          throw new UnsupportedOperationException("not needed for this test");
        }

        @NonNull
        @Override
        public File resolveExecBase(@NonNull INodeJSExecBase pBase)
        {
          throw new UnsupportedOperationException("not needed for this test");
        }

        @NonNull
        @Override
        public String getVersion()
        {
          throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public boolean isValid()
        {
          return true;
        }
      }));
    }
  }
}