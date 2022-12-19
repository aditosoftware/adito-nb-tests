package de.adito.aditoweb.nbm.tests.nbm;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.usage.IAdditionalContextChildrenUsageService;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.openide.util.lookup.ServiceProvider;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Service implementation of {@link IAdditionalContextChildrenUsageService}.
 * It finds all test files of the children of a context (e.g. views or entities).
 *
 * @author r.hartinger, 15.12.2022
 */
@ServiceProvider(service = IAdditionalContextChildrenUsageService.class)
public class AdditionalTestsUsagesService implements IAdditionalContextChildrenUsageService
{

  @NotNull
  @Override
  public Stream<File> findAdditionalUsages(@NotNull Project pProject, @NotNull String pNameOfProperty)
  {
    TestsFolderService testsFolderService = TestsFolderService.getInstance(pProject);

    // getting the file of the test folder
    File fileOfModule = testsFolderService.getTestsFolderForModel(pNameOfProperty);
    if (fileOfModule.exists() && fileOfModule.isDirectory())
    {
      // Getting the files in the directory and returning them
      File[] filesInDirectory = fileOfModule.listFiles();
      if (filesInDirectory != null)
        return Arrays.stream(filesInDirectory);
    }

    return Stream.empty();
  }
}
