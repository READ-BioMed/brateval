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
   * Return a list composed of the target ID and (optionally) the value of the attribute */
  public LinkedList <String> getList ()
  { return list; }

  /** Return the file which was the source of the annotation */
  public String getFile()
  { return file; }

  /**
   * Create a new attribute instance
   *
   * @param id The ID of the attribute (eg 'M3', 'A27')
   * @param name The name of the attribute, denoting what the attribute represents (eg 'Negation')
   * @param valueList The list of values of the attribute, which are stored separated by spaces after the name in the '.ann' file.
   *                  This should contain exactly one element if the attribute doesn't have a value, and two if it does
   *                  (the second denotes the value)
   * @param file The name of the file which the attributes came from.
   */
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
    this.list = new LinkedList<String>(valueList); // only needed for deprecated getList
    this.file = file;
  }
}