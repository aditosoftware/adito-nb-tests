package de.adito.aditoweb.nbm.tests.internal.runconfig;

import de.adito.aditoweb.nbm.tests.api.ITestExecutorFacade;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.openide.util.NbBundle;

import java.util.List;

/**
 * Run-Configuration, which executes cypress open
 *
 * @author s.seemann, 14.06.2021
 */
public class CypressOpenConfig extends CypressRunAllConfig
{

  public CypressOpenConfig(@NonNull Project pProject, @NonNull Observable<List<Project>> pOpenProjects)
  {
    super(pProject, pOpenProjects);
  }

  @Override
  protected String getText()
  {
    return NbBundle.getMessage(CypressRunAllConfig.class, "TITLE_CYPRESS_OPEN_CONFIG");
  }

  @Override
  public void executeAsnyc(@NonNull ProgressHandle pProgressHandle)
  {
    ITestExecutorFacade.INSTANCE.executeCypressOpen(project);
  }
}
