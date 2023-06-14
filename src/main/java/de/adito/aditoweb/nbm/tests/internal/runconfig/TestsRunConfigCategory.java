package de.adito.aditoweb.nbm.tests.internal.runconfig;

import de.adito.nbm.runconfig.api.IRunConfigCategory;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
import org.openide.util.NbBundle;

/**
 * Run-Configuration-Category for the Tests-Plugin
 *
 * @author s.seemann, 30.03.2021
 */
public class TestsRunConfigCategory implements IRunConfigCategory
{
  @NonNull
  @Override
  public String getName()
  {
    return "de-adito-aditoweb-nbm-tests-internal-runconfig-TestsRunConfigCategory";
  }

  @NonNull
  @Override
  public Observable<String> title()
  {
    return Observable.just(NbBundle.getMessage(TestsRunConfigCategory.class, "TITLE_TESTS_RUNCONFIG_CATEGORY"));
  }
}
