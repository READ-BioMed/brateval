package au.com.nicta.csp.brateval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Loader and writer for Brat stand-off annotations
 * 
 * @author Antonio Jimeno Yepes (antonio.jimeno@gmail.com)
 *
 */
public class Annotations
{
  public static Document read(String fileName, String file) throws IOException
  {
	Document d = new Document();
	read(fileName, file, d);
	return d;
  }

  public static void read(String fileName, String file, Document d) throws IOException
  {
	BufferedReader b = new BufferedReader(new FileReader(fileName));

    String line;

    while ((line = b.readLine()) != null)
    {
      if (line.trim().length() > 0)
      {
        if (line.startsWith("T"))
        {
          String [] fields = line.split("\t");

          if (fields.length == 3)
          {
            String [] ent = fields[1].split(" ");

            String id = fields[0];
            String type = ent[0];

            String [] locations = fields[1].substring(ent[0].length()).trim().split(";");

            LinkedList <Location> l = new LinkedList <Location> ();

            for (String location : locations)
            {
              String [] se = location.split(" ");
              l.add(new Location(Integer.parseInt(se[0]), Integer.parseInt(se[1])));
            }

            String string = fields[2];

            //System.out.println(line);
            d.addEntity(id, new Entity(id, type, l, string, file));
          }
          else
          { //System.err.println("Error: " + line);
          }
        }
        else if (line.startsWith("R"))
        {
          String [] fields = line.trim().split(" ");
          String [] subfields = fields[0].split("\t");

          String [] arg1 = fields[1].split(":");
          //System.out.println(arg1[0] + "|" + arg1[1] + "|" + d.getEntity(arg1[1]));

          String [] arg2 = fields[2].split(":");
          //System.out.println(arg2[0] + "|" + arg2[1] + "|" + d.getEntity(arg2[1]));

          //d.addRelation(subfields[0], new Relation(subfields[0], subfields[1], arg1[0], d.getEntity(fields[1].split(":")[1]), arg2[0], d.getEntity(fields[2].split(":")[1]), file));
          d.addRelation(subfields[0], new Relation(subfields[0], subfields[1], arg1[0], arg1[1], arg2[0], arg2[1], file));
        }
        else if (line.startsWith("E"))
        {
          String [] fields = line.trim().split("\t");
          String id = fields[0];
          String [] subfields = fields[1].split(" ");
          String [] trigger = subfields[0].split(":");

          LinkedList <String> arguments = new LinkedList <String> ();

          for (int i = 1; i < subfields.length; i++)
          { if (subfields[i].trim().length() > 0) { arguments.add(subfields[i]); } }

          d.addEvent(id, new Event(id, trigger[0], trigger[1], arguments, file));
        }
        else if (line.startsWith("*"))
        {
          String [] tokens = line.substring(2).split(" ");

          LinkedList <String> es = new LinkedList <String> ();

          if (tokens[0].equals("Equiv"))
          {
        	for (int i = 1; i < tokens.length; i++)
        	{
              es.add(tokens[i]);
        	}
          }

          d.addEquivalent(new Equivalent(es, file));
        }
        else if (line.startsWith("A") || line.startsWith("M"))
        {
          String [] fields = line.trim().split("\t");
          String id = fields[0];
          String [] subfields = fields[1].split(" ");

          LinkedList <String> attributes = new LinkedList <String> ();

          for (int i = 1; i < subfields.length; i++)
          { attributes.add(subfields[i]); }

          d.addAttribute(id, new Attribute(id, subfields[0], attributes, file));
        }
        else if (line.startsWith("N"))
        {
          String [] fields = line.trim().split("\t");

          String id = fields[0];
          String string = fields[2];

          String [] subfields = fields[1].split(" ");

          d.addNormalization(id, new Normalization(id, subfields[0], subfields[1], subfields[2], string, file));
        }
        else if (line.startsWith("#"))
        {
          String [] fields = line.trim().split("\t");

          String id = fields[0];
          String string = fields[2];

          String [] subfields = fields[1].split(" ");

          d.addNote(id, new Note(id, subfields[0], subfields[1], string, file));
        }
      }
    }
    
    for (Relation r : d.getRelations())
    {
      r.setEntity1(d.getEntity(r.getEntity1Id()));
      r.setEntity2(d.getEntity(r.getEntity2Id()));
    }

    b.close();
  }

  public static void read(String fileName, Document d) throws IOException
  {
	  read(fileName, fileName, d);
  }

  public static Document read(String fileName) throws IOException
  {
	  return read(fileName, fileName);
  }
  
  public static void write(String fileName, Document d) throws IOException
  {
	BufferedWriter w = new BufferedWriter(new FileWriter(fileName));

	for (Entity e : d.getEntities())
	{
	  StringBuilder locations = new StringBuilder();

	  if (e.getLocations().size() > 0)
	  {
	    locations.append(e.getLocations().get(0).getStart())
	             .append(" ")
	             .append(e.getLocations().get(0).getEnd());

	    for (int i = 1; i < e.getLocations().size(); i++)
	    {
	      locations.append(";")
	               .append(e.getLocations().get(i).getStart())
	               .append(" ")
	               .append(e.getLocations().get(i).getEnd());
	    }
      }		

      w.write(e.getId());
      w.write("\t");
      w.write(e.getType());
      w.write(" ");
      w.write(locations.toString());
      w.write("\t");
      w.write(e.getString());

      w.newLine();
	}

	for (Relation r : d.getRelations())
	{
      w.write(r.getId());
      w.write("\t");
      w.write(r.getRelation());
      w.write(" Arg1:");
      w.write(r.getEntity1().getId());
      w.write(" Arg2:");
      w.write(r.getEntity2().getId());

      w.newLine();
	}

	for (Event e : d.getEvents())
	{
      w.write(e.getId());
      w.write("\t");
      w.write(e.getType());
      w.write(":");
      w.write(e.getEventTrigger());

      for (String arg : e.getArguments())
      {
    	w.write(" ");
    	w.write(arg);
      }

      w.newLine();
	}

	for (Equivalent e : d.getEquivalents())
	{
      w.write("*\t");
      w.write("Equiv");

      for (String arg : e.getEquivalent())
      {
    	w.write(" ");
    	w.write(arg);
      }

      w.newLine();
	}
	
	for (Attribute a : d.getAttributes())
	{
	  w.write(a.getId());
      w.write("\t");
      w.write(a.getName());

      for (String arg : a.getList())
      {
    	w.write(" ");
    	w.write(arg);
      }

      w.newLine();
	}
	
	for (Normalization n : d.getNormalizations())
	{
	  w.write(n.getId());
      w.write("\t");
      w.write(n.getType());
      w.write(" ");
      w.write(n.getEntity());
      w.write(" ");
      w.write(n.getSourceId());
      w.write("\t");
      w.write(n.getString());
      
      w.newLine();
	}
	
	for (Note n : d.getNotes())
	{
	  w.write(n.getId());
      w.write("\t");
      w.write(n.getType());
      w.write(" ");
      w.write(n.getEntity());
      w.write("\t");
      w.write(n.getString());
      
      w.newLine();
	}

	w.close();
  }
}