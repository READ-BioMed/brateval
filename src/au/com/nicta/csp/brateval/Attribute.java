package au.com.nicta.csp.brateval;

import java.util.LinkedList;

public class Attribute
{
  private String id;
  private String name;
  private String file;
  private final String targetId;
  private final String value;

  /** Return the ID of the entity which this attribute applies to */
  public String getTargetId() {
    return targetId;
  }

  /** Return the value of the attribute, which is null (implying true) for binary attributes */
  public String getValue() {
    return value;
  }

  private LinkedList <String> list = new LinkedList <String> ();

  /** Return the unique ID of the attribute instance */
  public String getId()
  { return id; }

  /** Return the name of the attribute */
  public String getName()
  { return name; }

  /** @deprecated
   * Return a list composed of the target ID and the value of the attribute */
  public LinkedList <String> getList ()
  { return list; }

  /** Return the file which was the source of the annotation */
  public String getFile()
  { return file; }

  public Attribute(String id, String name, LinkedList<String> valueList, String file)
  {
	this.id = id;
    this.name = name;
    this.targetId = valueList.removeFirst();
    if (!valueList.isEmpty()) {
      this.value = valueList.removeFirst();
    } else {
      this.value = null;
    }
    assert valueList.isEmpty(); // should now have consumed all items (at most 2)
    this.file = file;
  }
}