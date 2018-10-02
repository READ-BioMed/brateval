package au.com.nicta.csp.brateval;

import java.util.LinkedList;

/**
 * Entity class
 * 
 * @author Antonio Jimeno Yepes (antonio.jimeno@gmail.com)
 *
 */
public class Entity
{
  private String id;
  private String type;
  private String file;

  private LinkedList <Location> l = new LinkedList <Location> ();
  
/*  private int start;
  private int end;*/

  private String string;

  //public Entity (String id, String type, int start, int end, String string)
  public Entity (String id, String type, LinkedList <Location> l, String string, String file)
  {
	this.id = id;
	this.type = type;
	this.l = l;
	this.string = string;
	this.file = file;
  }

  public String getFile()
  { return file; }
  
  public String getId()
  { return id; }

  public String getType()
  { return type; }

  public void setType(String type)
  { this.type = type; }

  public LinkedList <Location> getLocations()
  { return l; }
  
  public String getString()
  { return string; }

  public String getLocationsString(String rangeSign, String rangeSeparator) {
	StringBuilder locations = new StringBuilder();

	  	if (l.size() > 0)
	{
      locations.append(l.get(0).getStart())
               .append(rangeSign)
               .append(l.get(0).getEnd());

      for (int i = 1; i < l.size(); i++)
      {
        locations.append(rangeSeparator)
                 .append(l.get(i).getStart())
                 .append(rangeSign)
                 .append(l.get(i).getEnd());
      }
	}
	return locations.toString();
  } 
  
  public String toString()
  {
	return type + "|" + getLocationsString(" ", ";") + "|" + string;
  }

  public static boolean entityComparison(Entity e1, Entity e2)
  {
  	if (e1.getType().equals(e2.getType())
   	 && e1.getString().toLowerCase().equals(e2.getString().toLowerCase())
   	)
   	{
      for (Location l1 : e1.getLocations())
  	  {
    	boolean match = false ;

  	    for (Location l2 : e2.getLocations())
  	    {
          if (l1.getStart() == l2.getStart() && l1.getEnd() == l2.getEnd())
  	      { match = true; }
  	    }

  	    if (!match) { return false; }
  	  }

  	  return true;
   	}
  	else
  	{ return false; }
  }

  private static boolean entityCompareOverlap(Entity e1, Entity e2)
  {
    for (Location l1 : e1.getLocations())
    {
      for (Location l2 : e2.getLocations())
      {
    	if ((l1.getStart() >= l2.getStart() && l1.getStart() <= l2.getEnd())
    	 || (l1.getEnd() >= l2.getStart() && l1.getEnd() <= l2.getEnd())
        )
    	{ return true; }
      }
    }
	  
	return false;  
  }

  public static boolean entityComparisonOverlap(Entity e1, Entity e2)
  {
  	if (e1.getType().equals(e2.getType())
   	 && (entityComparisonSpanOverlap(e1, e2))
   	)
   	{ return true; }

  	return false;
  }

  public static boolean entityComparisonSpanOverlap(Entity e1, Entity e2)
  {
  	if ((entityCompareOverlap(e1, e2) || entityCompareOverlap(e2, e1))
   	)
   	{ return true; }

  	return false;
  }
  
  public static double entityComparisonStringSimilarity(Entity e1, Entity e2,
  	double similarity_threshold)
  {
  	int l = Math.max(e1.getString().length(), e2.getString().length());
  	int max_dist = (int)Math.floor((1.0-similarity_threshold) * ((double)l)) ;
  	int dist = SpanSimilarity.editDistance(e1.getString(), e2.getString(),
  		max_dist);
  	if (dist <= max_dist)
   	{ return 1.0 - ((double)dist)/((double)l); }

  	return 0.0 ;
  }
  
  public String locationInfo()
  {
  	return getFile() + ":" + getLocationsString("-", ";");
  }
}