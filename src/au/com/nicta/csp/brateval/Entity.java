package au.com.nicta.csp.brateval;

import java.util.LinkedList;

import javax.lang.model.util.ElementScanner14;

import au.com.nicta.csp.brateval.MatchType.SpanMatch;
import au.com.nicta.csp.brateval.MatchType.TypeMatch;

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
  private TaxonomyConfig.EntityDesc typeDescription;

  private LinkedList <Location> l = new LinkedList <Location> ();
  
/*  private int start;
  private int end;*/

  private String string;

  private static TaxonomyConfig tx = TaxonomyConfig.singleton();
  //public Entity (String id, String type, int start, int end, String string)
  public Entity (String id, String type, LinkedList <Location> l, String string, String file)
  {
	this.id = id;
	this.type = type;
	typeDescription = tx.getEntityDesc(type);
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

  public TaxonomyConfig.EntityDesc getTypeDesc()
  { return typeDescription; }
  
  public void setType(String type)
  { this.type = type;
	typeDescription = tx.getEntityDesc(type); }

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

  public static boolean entityComparisonExactType(Entity e1, Entity e2)
  {
  	return (e1.getType().equals(e2.getType()));
  }

  public static boolean entityComparisonExactString(Entity e1, Entity e2)
  {
  	return (e1.getString().toLowerCase().equals(e2.getString().toLowerCase()));
  }

  public static boolean entityComparisonExactSpan(Entity e1, Entity e2)
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

  public static boolean entityComparisonExact(Entity e1, Entity e2)
  {
  	if ( entityComparisonExactType(e1, e2)
      && entityComparisonExactString(e1, e2)
      && entityComparisonExactSpan(e1, e2))
    { return true; }
  	else
  	{ return false; }
  }

  public static boolean entityComparisonSpanOverlap(Entity e1, Entity e2)
  {
  	if ((entityCompareOverlapSpan(e1, e2) || entityCompareOverlapSpan(e2, e1))
   	)
   	{ return true; }

  	return false;
  }

  // Helper function for above public function; checks overlap in one direction
  private static boolean entityCompareOverlapSpan(Entity e1, Entity e2)
  {
    for (Location l1 : e1.getLocations())
    {
      for (Location l2 : e2.getLocations())
      {
    	if ((l1.getStart() >= l2.getStart() && l1.getStart() < l2.getEnd())
    	 || (l1.getEnd() > l2.getStart() && l1.getEnd() <= l2.getEnd())
        )
    	{ return true; }
      }
    }
	  
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

  public static boolean entityComparisonOverlap(Entity e1, Entity e2)
  {
  	if (e1.getType().equals(e2.getType())
   	 && (entityComparisonSpanOverlap(e1, e2))
   	)
   	{ return true; }

  	return false;
  }

  public static boolean entityComparisonCommonTypeHierarchy(Entity e1, Entity e2)
  {
	  TaxonomyConfig.EntityDesc s =
	    tx.lowestCommonSubsumer(e1.getTypeDesc(), e2.getTypeDesc());
	  return s != null && s.depth > 0; 
  }
  
  public static EntityMatchResult getMatchResult(Entity e1, Entity e2, MatchType mt)
  {
    EntityMatchResult matchResult = new EntityMatchResult();
    matchResult.setE1(e1);
    matchResult.setE2(e2);
    SpanMatch mtSpanMatch = mt.getSpanMatchType();

    // Check for nature of span/string overlap
    // Get the most specific type of overlap (prefer EXACT)
    if ( entityComparisonExactSpan(e1, e2) ) {
      matchResult.setSpanMatchType(SpanMatch.EXACT);
    } else if (mtSpanMatch == SpanMatch.EXACT)
      return null;
    else if ( entityComparisonSpanOverlap(e1, e2) ) {
      if ( mtSpanMatch == SpanMatch.APPROXIMATE ) { // look for approximate string match if match type allows it
        double minSim = mt.getSimThreshold();
        double sim = entityComparisonStringSimilarity(e1, e2, minSim ); // entities must also be similar
        if ( sim >= minSim ) {
          matchResult.setSpanMatchType(SpanMatch.APPROXIMATE);
          matchResult.setMatchSim(sim);
        } else
          return null;
      } else {
        matchResult.setSpanMatchType(SpanMatch.OVERLAP);
      }
    }
    else return null;

    // Set type equivalence in matchResult
    if ( entityComparisonExactType(e1, e2) )
      matchResult.setTypeMatchType(TypeMatch.EXACT);
    else if (mt.getTypeMatchType() == TypeMatch.EXACT)
      return null;
    else if ( entityComparisonCommonTypeHierarchy(e1, e2) )
      matchResult.setTypeMatchType(TypeMatch.HIERARCHICAL);
    else if (mt.getTypeMatchType() == TypeMatch.HIERARCHICAL)
      return null;
    else  matchResult.setTypeMatchType(TypeMatch.INEXACT);

    return matchResult;
  }
  
  public String locationInfo()
  {
  	return getFile() + ":" + getLocationsString("-", ";");
  }
}