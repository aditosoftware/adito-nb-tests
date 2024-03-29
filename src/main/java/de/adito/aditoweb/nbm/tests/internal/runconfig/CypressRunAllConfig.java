package de.adito.aditoweb.nbm.tests.internal.runconfig;

import de.adito.aditoweb.nbm.tests.api.ITestExecutorFacade;
import de.adito.nbm.runconfig.api.*;
import de.adito.nbm.runconfig.spi.IActiveConfigComponentProvider;
import de.adito.observables.netbeans.ProjectObservable;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.*;
import org.openide.util.NbBundle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * Run-Configuration, which starts all tests of a project.
 *
 * @author s.seemann, 30.03.2021
 */
public class CypressRunAllConfig implements IRunConfig
{
  protected final Project project;
  private final Observable<List<Project>> openProjects;

  public CypressRunAllConfig(@NonNull Project pProject, @NonNull Observable<List<Project>> pOpenProjects)
  {
    openProjects = pOpenProjects;
    project = pProject;
  }

  @NonNull
  @Override
  public Observable<Optional<IRunConfigCategory>> category()
  {
    return Observable.just(Optional.of(new TestsRunConfigCategory()));
  }

  @NonNull
  @Override
  public Observable<Optional<Image>> icon()
  {
    try
    {
      return Observable.just(Optional.of(ImageIO.read(getClass().getResource("cypress.png"))));
    }
    catch (IOException pE)
    {
      return Observable.just(Optional.empty());
    }
  }

  @NonNull
  @Override
  public Observable<String> displayName()
  {
    return openProjects
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
          String text = getText();
          if (pProjectName.trim().isEmpty())
            return text;
          return text + IActiveConfigComponentProvider.DISPLAY_NAME_SEPARATOR + pProjectName;
        });
  }

  @Override
  public void executeAsnyc(@NonNull ProgressHandle pProgressHandle)
  {
    ITestExecutorFacade.INSTANCE.executeAllTests(project);
  }

  protected String getText()
  {
    return NbBundle.getMessage(CypressRunAllConfig.class, "TITLE_CYPRESS_RUN_CONFIG");
  }
}
