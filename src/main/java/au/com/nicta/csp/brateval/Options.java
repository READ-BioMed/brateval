package au.com.nicta.csp.brateval;
import au.com.nicta.csp.brateval.MatchType.SpanMatch;
import au.com.nicta.csp.brateval.MatchType.TypeMatch;

import java.util.Vector;
import java.lang.IllegalArgumentException;

/**
 * 
 * BRAT evaluator options
 * 
 * @author Andrey (Andreas) Scherbakov (andreas@softwareengineer.pro)
 * @author Karin Verspoor (karin.verspoor@unimelb.edu.au)
 *
 */

 public class Options {
	public static Options common = null;
	public String[] argv;
     
	public enum OutFmt {
		PLAIN,
		CSV,
		HTML,
		TERMCOLOR
	}

		
	public OutFmt outFmt = OutFmt.CSV;

	// options related to annotation folders
	public String goldFolder; // name of folder for gold standard annotations
	public String evalFolder; // name of folder for annotations to be compared to gold standard
	public String configFile; // location/name of annotation.conf file (path + name) [by default, "annotation.conf" in the current directory will be used]
	public String outputFolder;
     
	// options related to matching
	public MatchType matchType; // specify how spans are treated for matching

	public boolean verbose = false; // print full details of TP, FP, FN, etc.
 	public boolean show_full_taxonomy = false;  // print all levels of the taxonomy, even if no annotations at a level
     
	static String toEnumName(String x) {
		return x.replaceAll("[^A-Za-z0-9]","").toUpperCase();
	}
     
	public Options(String [] argv) {
		Vector<String> av = new Vector<String>(argv.length);
		matchType = new MatchType();

		goldFolder = null;
		evalFolder = null;
		configFile = "annotation.conf";
		outputFolder="";

		for (int j=0; j<argv.length; ++j) {
			if (argv[j].charAt(0) == '-') {
				switch(argv[j]) {
					case "-of": case "-out-format":
					    	j++;
						outFmt = OutFmt.valueOf(toEnumName(argv[j]));
						break;
					case "-tc": case "-taxon-config": case "-config": // location of annotation.conf file
						j++;
						configFile = argv[j];
						break;
					case "-v": case "-verbose":
						verbose = true;
						break;
					case "-g": case "-gold": // gold folder
						j++;
						goldFolder = argv[j];
						break;
					case "-e": case "-eval": // folder to evaluate against gold data
						j++;
						evalFolder = argv[j];
						break;
					case "-ft": case "-print-full-taxonomy":
						show_full_taxonomy = true;
						break;
					case "-s": case "-span-match":
					    j++;
					    String spanSelection = argv[j];
						if (spanSelection.equalsIgnoreCase("approx") || spanSelection.equalsIgnoreCase("approximate")) {
						    spanSelection = "APPROXIMATE";
						    j++; // also get similarity value
							double sim = Double.parseDouble(argv[j]);
							matchType.setSimThreshold(sim);
							matchType.setSpanMatchType(SpanMatch.APPROXIMATE);
						} else if (spanSelection.equalsIgnoreCase("overlap") || spanSelection.equalsIgnoreCase("inexact") || spanSelection.equalsIgnoreCase("o") || spanSelection.equalsIgnoreCase("i"))
							matchType.setSpanMatchType(SpanMatch.OVERLAP); 
						else if (spanSelection.equalsIgnoreCase("exact") || spanSelection.equalsIgnoreCase("e"))
							matchType.setSpanMatchType(SpanMatch.EXACT);
						else // unrecognised argument, default to EXACT
							matchType.setSpanMatchType(SpanMatch.EXACT);
						break;
					case "-t": case "-type-match":
					    j++;
					    String typeSelection = argv[j];
						if (typeSelection.equalsIgnoreCase("hier"))
							typeSelection = "HIERARCHICAL";
						matchType.setTypeMatchType(TypeMatch.valueOf(toEnumName(typeSelection)));
						break;
					case "-o" : case "-outputfolder":
						j++;
						outputFolder = argv[j];
						break;
					default:
						throw new IllegalArgumentException("Unsupported option" + argv[j]);
				}
			} else { 
				// assume first non "-" argument is the comparator folder, and second is the gold folder
				if ( evalFolder == null)
					evalFolder = argv[j];
				else if ( goldFolder == null )
					goldFolder = argv[j];
				else av.add(argv[j]);
			}
		}
		av.toArray(this.argv = new String[av.size()]);
	}


	public  static void main(String [] argv) {
		Options.common = new Options(argv);
		System.out.println(Options.common.argv.length);
		System.out.println(Options.common.argv[0]);
	}
}

