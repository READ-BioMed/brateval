package au.com.nicta.csp.brateval;

import java.util.LinkedList;

public class Equivalent
{
  private LinkedList <String> e = new LinkedList <String> ();
  private String file;

  public Equivalent(LinkedList <String> e, String file)
  {
	this.e = e;
    this.file = file;
  }

  public LinkedList <String> getEquivalent()
  { return e; }
  
  public String getFile()
  { return file; }
}