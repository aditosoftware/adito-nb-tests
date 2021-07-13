package de.adito.aditoweb.nbm.tests.internal.runconfig;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.javascript.node.*;
import de.adito.nbm.runconfig.api.*;
import de.adito.observables.netbeans.*;
import de.adito.util.reactive.ObservableCollectors;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

import java.io.File;
import java.util.*;

/**
 * Provider for Run-Configurations of this plugin
 *
 * @author s.seemann, 30.03.2021
 */
@ServiceProvider(service = ISystemRunConfigProvider.class)
public class TestsRunConfigProvider implements ISystemRunConfigProvider
{

  @NotNull
  @Override
  public Observable<List<IRunConfig>> runConfigurations(List<ISystemInfo> pSystemInfos)
  {
    return pSystemInfos.stream()
        .map(ISystemInfo::getProject)
        .filter(Objects::nonNull)
        .distinct()
        .map(pProject -> {
          INodeJSProvider provider = INodeJSProvider.findInstance(pProject).orElse(null);
          Observable<Optional<INodeJSEnvironment>> envObs = Observable.just(Optional.empty());
          if (provider != null)
            envObs = provider.current();

          // node_modules and environment must be present
          return Observable.combineLatest(FileObservable.create(new File(FileUtil.toFile(pProject.getProjectDirectory()), "node_modules/cypress")), envObs,
                                          (pCypressOpt, pEnvOpt) -> {
                                            if (pCypressOpt.isPresent() && pCypressOpt.get().exists()
                                                && pEnvOpt.isPresent() && pEnvOpt.get().isValid())
                                              return List.<IRunConfig>of(new CypressRunAllConfig(pProject, observeOpenProjects()),
                                                                         new CypressOpenConfig(pProject, observeOpenProjects()));
                                            return List.<IRunConfig>of();
                                          });

        })
        .collect(ObservableCollectors.combineListToList());
  }

  @Override
  public ISystemRunConfigProvider getInstance(Project pProject)
  {
    return this;
  }

  protected Observable<List<Project>> observeOpenProjects()
  {
    return OpenProjectsObservable.create();
  }
}
