package de.adito.aditoweb.nbm.tests.internal.runconfig;

import de.adito.aditoweb.nbm.tests.api.ITestExecutorFacade;
import de.adito.aditoweb.nbm.vaadinicons.IVaadinIconsProvider;
import de.adito.nbm.runconfig.api.*;
import de.adito.nbm.runconfig.spi.IActiveConfigComponentProvider;
import de.adito.observables.netbeans.*;
import de.adito.swing.icon.IconAttributes;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.*;
import org.openide.util.*;

import java.awt.*;
import java.util.Optional;

/**
 * Run-Configuration, which starts all tests of a project.
 *
 * @author s.seemann, 30.03.2021
 */
public class AllTestsRunConfig implements IRunConfig
{
  private final Project project;
  private final IVaadinIconsProvider iconsProvider;

  public AllTestsRunConfig(@NotNull Project pProject)
  {
    iconsProvider = Lookup.getDefault().lookup(IVaadinIconsProvider.class);
    project = pProject;
  }

  @NotNull
  @Override
  public Observable<Optional<IRunConfigCategory>> category()
  {
    return Observable.just(Optional.of(new TestsRunConfigCategory()));
  }

  @NotNull
  @Override
  public Observable<Optional<Image>> icon()
  {
    return Observable.just(Optional.ofNullable(iconsProvider)
                               .map(pIconProvider -> pIconProvider.findImage(IVaadinIconsProvider.VaadinIcon.AUTOMATION, new IconAttributes.Builder().create())));
  }

  @NotNull
  @Override
  public Observable<String> displayName()
  {
    return OpenProjectsObservable.create()
        .switchMap(pProjects -> {
          if (pProjects.size() > 1) // Only if there a more than two projects opened, the project name should be displayed
          {
            return ProjectObservable.createInfos(project)
                .map(ProjectInformation::getDisplayName)
                .map(pName -> " (" + pName + ")");
          }
          return Observable.just("");
        })
        .map(pProjectName -> {
          String text = NbBundle.getMessage(AllTestsRunConfig.class, "TITLE_TESTS_RUNFCONFIG");
          if (pProjectName.trim().isEmpty())
            return text;
          return text + IActiveConfigComponentProvider.DISPLAY_NAME_SEPARATOR + pProjectName;
        });
  }

  @Override
  public void executeAsnyc(@NotNull ProgressHandle pProgressHandle)
  {
    ITestExecutorFacade.INSTANCE.executeAllTests(project);
  }
}
