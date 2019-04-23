package au.com.nicta.csp.brateval;
import java.lang.Character;
import java.io.*;
import java.util.ArrayList;

/**
 * Taxonomy Configuration
 * 
 * @author Andrey (Andreas) Scherbakov (andreas@softwareengineer.pro)
 * @author Karin Verspoor (karin.verspoor@unimelb.edu.au)
 *
 */
 
 public class TaxonomyConfig {
    public final int TOP_LEVEL = 0;
    public final String TOP = "ALL";

	public class EntityDesc {
		String name;
		EntityDesc(String s) {name = s;}
	}

	public class RelationDesc {
		String name;
		// Add args
		RelationDesc(String s) {name = s;} //Rewrite!
	}
	
	HierList<EntityDesc> entities = new HierList<EntityDesc>();
	HierList<RelationDesc> relations = new HierList<RelationDesc>();
	
	static File findConfigFile() {
		File fn = new File("./annotation.conf");
		if (fn.isFile() && fn.canRead()) return fn;
		return null;
	}

    TaxonomyConfig(String confFile) {
	    File fn = new File(confFile);
	    if (!(fn.isFile() && fn.canRead())) {
		    fn = null;
		}

	    readConfigFile(fn);
	}  

	TaxonomyConfig() {
		readConfigFile(findConfigFile());
	}
	
	private interface EntryAdder {
		void add(int level, String spec);
	}

	String consumeSection(BufferedReader r, EntryAdder a)
		throws IOException {
		String text;
		a.add(this.TOP_LEVEL,this.TOP); // Dummy TOP node
		int level_default = this.TOP_LEVEL + 1;
		while ((text = r.readLine()) != null) {
			int level = level_default;
			for (int i = 0; i < text.length() && Character.isWhitespace(text.charAt(i)); ++i)
			switch (text.charAt(i)) {
					case ' ': level ++;
					break;
					case '\t': level +=4;
					break;
					default: level = level_default;
					break;
			}
			String s = text.trim();
			if (!s.isEmpty()) {
				if (s.charAt(0) == '[')
					return text; //New section started
				a.add(level, s);
			}
		}
		return text; //null
	}
	
	void readConfigFile(File file) {
		if (file == null)	return;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String text = reader.readLine();

			while (text != null) {
			  String tt = text.trim();
			  if (tt.equals("[entities]"))
			  	text = consumeSection(reader, new EntryAdder() {
				  	public void add(int l, String s)
			  		{
				  		entities.add(l, new EntityDesc(s));
			  		}});
			  else if (tt.equals("[relations]"))
			  	text = consumeSection(reader, new EntryAdder() {
				  	public void add(int l, String s)
			  		{
				  		relations.add(l, new RelationDesc(s));
			  		}});
		  	  else if (tt.charAt(0) == '[')
			  	text = consumeSection(reader, new EntryAdder() {
				  	public void add(int l, String s) {} });
			  else
				text = reader.readLine();
			}

		} catch (FileNotFoundException e) {
    		e.printStackTrace();
		} catch (IOException e) {
    		e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {}
		}
	}
	
	private ArrayList<String> levelIndents = new ArrayList<String>(16);
	public String levelIndent(int level) {
		if (levelIndents.size() <= level ||
			levelIndents.get(level) == null) {
				StringBuffer buffer = new StringBuffer();
				int j = level;
				while (j >= 4) {buffer.append("\t"); j-=4;}
				while (j > 0) {buffer.append(" "); j--;}
				while (levelIndents.size() <= level)
					levelIndents.add(null);
				levelIndents.set(level, buffer.toString());
			}
		return levelIndents.get(level);
	}
     
     public String levelPrefix(int level) {
         if (levelIndents.size() <= level ||
             levelIndents.get(level) == null) {
             StringBuffer buffer = new StringBuffer();
             int j = level;
             while (j >= 4) {buffer.append("+"); j-=4;}
             while (j > 0) {buffer.append(" "); j--;}
             while (levelIndents.size() <= level)
                 levelIndents.add(null);
             levelIndents.set(level, buffer.toString());
         }
         return levelIndents.get(level);
     }
	
	public void traverseEntities(HierList.Visitor<EntityDesc> v) {
		entities.traverse(v);
	}
	public void traverseRelations(HierList.Visitor<RelationDesc> v) {
		relations.traverse(v);
	}
}	
			
