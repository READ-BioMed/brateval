package au.com.nicta.csp.brateval;

import java.util.LinkedList;

public class Attribute
{
  private String id;
  private String value;
  private String file;

  private LinkedList <String> list = new LinkedList <String> ();

  public String getId()
  { return id; }

  public String getValue()
  { return value; }
  
  public LinkedList <String> getList ()
  { return list; }

  public String getFile()
  { return file; }

  public Attribute(String id, String value, LinkedList <String> list, String file)
  {
	this.id = id;
    this.value = value;	
    this.list = list;
    this.file = file;
  }
}