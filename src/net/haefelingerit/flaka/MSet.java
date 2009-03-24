/*
 * Copyright (c) 2009 Haefelinger IT 
 *
 * Licensed  under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required  by  applicable  law  or  agreed  to in writing, 
 * software distributed under the License is distributed on an "AS 
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied.
 
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package net.haefelingerit.flaka;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.haefelingerit.flaka.util.Static;
import net.haefelingerit.flaka.util.TextReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * 
 * @author merzedes
 * @since 1.0
 */
public class MSet extends Task
{
  private String text = null;
  private String comment = null;

  public void setComment(String s)
  {
    this.comment = Static.trim2(s, null);
  }

  public void addText(String text)
  {
    this.text = text;
  }

  public String toString()
  {
    StringBuilder buf = new StringBuilder();
    if (this.text != null)
    {
      buf.append("<mset>\n");
      buf.append(this.text);
      buf.append("\n</mset>\n");
    } else
    {
      buf.append("<mset/>\n");
    }
    return buf.toString();
  }

  protected Pattern makeregex(String s)
  {
    Pattern re = null;
    try
    {
      re = Pattern.compile(s);
    } catch (Exception e)
    {
      /* TODO: error */
      this.debug("error compiling regex '" + s + "'", e);
    }
    return re;
  }

  protected Pattern getPropRegex()
  {
    return makeregex("([^=:]+)(=|:=|::=)(.*)");
  }

  protected int howto(String s)
  {
    int r = Static.VARREF;
    switch (s.charAt(0))
    {
      case '=':
        r = Static.VARREF;
        break;
      default:
        switch (s.length())
        {
          case 3: /* ::= */
            r = Static.WRITEPROPTY;
            break;
          default:
            r = Static.PROPTY;
        }
        break;
    }
    return r;
  }

  public void execute() throws BuildException
  {
    Project project;
    Pattern regex;
    TextReader tr;
    Matcher M;
    String line;
    String k, v;
    Object o;
    int type;

    project = this.getProject();
    type = Static.VARREF;

    if (this.comment == null)
      this.comment = ";";

    if (this.text == null)
      return;

    regex = getPropRegex();

    tr = new TextReader(this.text).setComment(this.comment);
    tr.skipempty = true;

    while ((line = tr.readLine()) != null)
    {
      /* eval text */
      if ((M = regex.matcher(line)).matches() == false)
      {
        debug("line " + tr.lineno + ": syntax error '" + line + "'");
        continue;
      }
      // otherwise:
      k = project.replaceProperties(M.group(1));
      v = project.replaceProperties(M.group(3));
      o = null;
      try
      {
        k = Static.elresolve(project, k);
        v = Static.elresolve(project, v);
        o = Static.el2obj(project, v);
      } catch (Exception e)
      {
        if (this.debug)
          debug("line " + tr.lineno + ": error evaluating EL expression (ignored) in "
              + Static.q(v));
      }
      type = howto(M.group(2));
      Static.assign(project, k, o, type);
    }
  }
}