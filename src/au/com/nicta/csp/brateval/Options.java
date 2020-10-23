package au.com.nicta.csp.brateval;
import java.util.Vector;
import java.lang.IllegalArgumentException;
import java.util.ArrayList;

/**
 * 
 * BRAT evaluator options
 * 
 * @author Andrey (Andreas) Scherbakov (andreas@softwareengineer.pro)
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
		
	public OutFmt outFmt = OutFmt.PLAIN;

	public boolean verbose = false; // print full details of TP, FP, FN, etc.
	public boolean hierarchical = false;     
 	public boolean show_full_taxonomy = false;  // print all levels of the taxonomy, even if no annotations at a level
     
	static String toEnumName(String x) {
		return x.replaceAll("[^A-Za-z0-9]","").toUpperCase();
	}
	public Options(String [] argv) {
		Vector<String> av = new Vector<String>(argv.length);
		for (int j=0; j<argv.length; ++j) {
			if (argv[j].charAt(0) == '-') {
				switch(argv[j]) {
					case "-of": case "-out-format":
					    	j++;
						outFmt = OutFmt.valueOf(toEnumName(argv[j]));
						break;
					case "-v": case "-verbose":
						verbose = true;
						break;
					case "-h": case "-hierarchical":
						hierarchical = true;
						break;
					case "-ft": case "-print-full-taxonomy":
						show_full_taxonomy = true;
						break;
					default:
						throw new IllegalArgumentException("Unsupported option" + argv[j]);
				}
			} else av.add(argv[j]);
		}
		av.toArray(this.argv = new String[av.size()]);
	}

     /*
    public void parse(String[] args)
    {
        arguments = new ArrayList();
        for ( int i = 0; i < args.length; i++ ) {
            arguments.add(args[i]);
        }
    }

    public int size()
    {
        return arguments.size();
    }

    public boolean hasOption(String option)
    {
        boolean hasValue = false;
        String str;
        for ( int i = 0; i < arguments.size(); i++ ) {
            str = (String)arguments.get(i);
            if ( true == str.equalsIgnoreCase(option) ) {
                hasValue = true;
                break;
            }
        }

        return hasValue;
    }

    public String valueOf(String option)
    {
        String value = null;
        String str;

        for ( int i = 0; i < arguments.size(); i++ ) {
            str = (String)arguments.get(i);
            if ( true == str.equalsIgnoreCase(option) ) {
                value = (String)arguments.get(i+1);
                break;
            }
        }

        return value;
    }
     */

	public  static void main(String [] argv) {
		Options.common = new Options(argv);
		System.out.println(Options.common.argv.length);
		System.out.println(Options.common.argv[0]);
	}
}

