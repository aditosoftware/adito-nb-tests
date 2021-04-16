package de.adito.aditoweb.nbm.tests.internal.runconfig;

import de.adito.nbm.runconfig.api.*;
import de.adito.observables.netbeans.OpenProjectsObservable;
import de.adito.util.reactive.ObservableCollectors;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.openide.util.lookup.ServiceProvider;

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
        .map(pProject -> Observable.just((IRunConfig) new AllTestsRunConfig(pProject, getOpenProjects())))
        .collect(ObservableCollectors.combineToList());
  }

  protected Observable<List<Project>> getOpenProjects()
  {
    return OpenProjectsObservable.create();
  }

  @Override
  public ISystemRunConfigProvider getInstance(Project pProject)
  {
    return this;
  }
}
