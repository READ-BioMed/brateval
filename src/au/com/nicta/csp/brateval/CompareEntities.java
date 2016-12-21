package au.com.nicta.csp.brateval;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 
 * BRAT stand-off entity comparison
 * 
 * @author Antonio Jimeno Yepes (antonio.jimeno@nicta.com.au)
 *
 */
public class CompareEntities
{
  public static void main (String argc []) throws IOException
  {
	String folder1 = argc[0];
	String folder2 = argc[1];
	boolean exact_match = Boolean.parseBoolean(argc[2]);

	evaluate(folder1, folder2, exact_match);
  }

  public static void evaluate(String folder1, String folder2, boolean exact_match)
  throws IOException
  {
	Map <String, Integer> entityTP = new TreeMap <String, Integer> ();
	Map <String, Integer> entityFP = new TreeMap <String, Integer> ();
	Map <String, Integer> entityFN = new TreeMap <String, Integer> ();

	Set <String> entityTypes = new TreeSet <String> ();

    File folder = new File(folder1);

    for (File file : folder.listFiles())
    {
      if (file.getName().endsWith(".ann"))
      {
        Document d1 = Annotations.read(file.getAbsolutePath(), "ann");
        Document d2 = Annotations.read(folder2 + File.separator +  file.getName(), "ann");

    	for (Entity e : d1.getEntities())
    	{
          entityTypes.add(e.getType());

          Entity match = null;

          if (exact_match)
          {	match = d2.findEntity(e); }
          else
          { match = d2.findEntityOverlapC(e); }

          //if (d2.findEntityOverlapC(e) != null)
          if (match != null)
    	  {
    		if (entityTP.get(e.getType()) == null)
    		{ entityTP.put(e.getType(), 1); }
    		else
    		{ entityTP.put(e.getType(), entityTP.get(e.getType()) + 1); }
          }
          else
    	  {
      		if (entityFP.get(e.getType()) == null)
      		{ entityFP.put(e.getType(), 1); }
      		else
      		{ entityFP.put(e.getType(), entityFP.get(e.getType()) + 1); }
          }
    	}

   	    for (Entity e : d2.getEntities())
        {
          entityTypes.add(e.getType());

          Entity match = null;
          
          if (exact_match)
          {	match = d1.findEntity(e); }
          else
          { match = d1.findEntityOverlapC(e); }

          if (match == null)
          {
            if (entityFN.get(e.getType()) == null)
            { entityFN.put(e.getType(), 1); }
      		else
      		{ entityFN.put(e.getType(), entityFN.get(e.getType()) + 1); }
            
            System.out.println("FN: " + e);
          }
    	}
      }
    }

    System.out.println("");
    System.out.println("Summary");

    for (String et : entityTypes)
    {
      int TP = (entityTP.get(et) == null ? 0 : entityTP.get(et));
      int FP = (entityFP.get(et) == null ? 0 : entityFP.get(et));
      int FN = (entityFN.get(et) == null ? 0 : entityFN.get(et));
      
      
      double precision = 0;
      double recall = 0;
      double f_measure = 0;
      
      if (TP+FP > 0) { precision = (double)TP/(TP+FP); }
      
      if (TP+FN > 0) { recall = (double)TP/(TP+FN); }
      
      if ((precision+recall) > 0)
      { f_measure = (2*precision*recall)/(double)(precision+recall); }
    	
      System.out.println(et
    		           + "|tp:" + TP 
    		           + "|fp:" + FP 
    		           + "|fn:" + FN
    		           + "|precision:" + String.format("%1.4f", precision)
    		           + "|recall:" + String.format("%1.4f", recall)
    		           + "|f1:" + String.format("%1.4f", f_measure)
    		           );
    }
  }  
}