package it.haefelinger.flaka;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FilterSetCollection;
import org.apache.tools.ant.util.FileUtils;

public class Copy extends org.apache.tools.ant.taskdefs.Copy
{
  protected boolean preserveExecutable = false;
  
  public Copy()
  {
    super();
    fileUtils = new FileUtils()
    {
      @Override
      public void copyFile(File sourceFile, File destFile,
          FilterSetCollection filters, Vector filterChains,
          boolean overwrite, boolean preserveLastModified,
          boolean append,
          String inputEncoding, String outputEncoding,
          Project project, boolean force) throws IOException 
      {
        super.copyFile(sourceFile, destFile, filters, filterChains, overwrite, preserveLastModified, append, inputEncoding, outputEncoding, project, force);
        if(preserveExecutable && sourceFile != null && destFile != null && sourceFile.canExecute())
          if(!destFile.setExecutable(true, false))
            log("Failed to set execute permissions for " + destFile, Project.MSG_WARN);
      }
    };
  }

  public boolean isPreserveExecutable()
  {
    return preserveExecutable;
  }

  public void setPreserveExecutable(boolean preserveExecutable)
  {
    this.preserveExecutable = preserveExecutable;
  }
}
