package au.com.nicta.csp.brateval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Class with commonly used functions 
 * 
 * @author Antonio Jimeno Yepes (antonio.jimeno@gmail.com)
 *
 */
public class Utils
{
  public static List <Map.Entry <String, Integer>> mapValueIntegerSortDesc(Map <String, Integer> map)
  {
    List <Map.Entry <String, Integer>> scoreRank =
   	    new ArrayList <Map.Entry <String, Integer>> (map.entrySet());

    Collections.sort(scoreRank, new Comparator <Map.Entry <String, Integer>> ()
	{
	  public int compare(Map.Entry <String, Integer> o1, Map.Entry <String, Integer> o2)
	  { return o2.getValue().compareTo(o1.getValue()); }
	});

	return scoreRank;
  }
  
  public static <K> Map<K,Integer> plusMap(Map<K,Integer> m, K key, Integer increment) {
	  if (increment.intValue() != 0)
	  	m.merge(key, increment, Integer::sum);
	  return  m;
  }
  
}
