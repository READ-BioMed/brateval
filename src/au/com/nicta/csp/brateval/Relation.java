package au.com.nicta.csp.brateval;

/**
 * Relation class
 * 
 * @author Antonio Jimeno Yepes (antonio.jimeno@gmail.com)
 *
 */
public class Relation
{
  private String id;
  private String relation;
  private String arg1;
  private Entity e1;
  private String arg2;
  private Entity e2;

  private String file;

  public Relation (String id, String relation, String arg1, Entity e1, String arg2, Entity e2, String file)
  {
    this.id = id;
    this.relation = relation;

    if (e1.getType().compareTo(e2.getType()) > 0)
    {
      this.e1 = e2;
      this.arg1 = arg2;
      this.e2 = e1;
      this.arg2 = arg1;
    }
    else
    {
      this.e1 = e1;
      this.e2 = e2;
      this.arg1 = arg1;
      this.arg2 = arg2;
    }

    this.file = file;
  }

  public String getId()
  { return id; }

  public String getRelation()
  { return relation; }

  public String getRelationType()
  { return new StringBuilder(relation).append("|").append(e1.getType()).append("|").append(e2.getType()).toString(); }

  public String getArg1()
  { return arg1; }
  
  public Entity getEntity1()
  { return e1; }

  public void setEntity1(Entity e)
  { e1 = e; }

  public String getArg2()
  { return arg2; }

  public Entity getEntity2()
  { return e2; }

  public void setEntity2(Entity e)
  { e2 = e; }

  public String getFile()
  { return file; }
}