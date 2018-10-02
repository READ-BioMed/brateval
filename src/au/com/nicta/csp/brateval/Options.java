package au.com.nicta.csp.brateval;
import java.util.Vector;
import java.lang.IllegalArgumentException;

/**
 * 
 * BRAT evaluator options
 * 
 * @author Andrey (Andreas) Scherbakov (andreas@softwareengineer.pro)
 *
 */

 public class Options {
	public static Options common = null;

	public enum OutFmt {
		PLAIN,
		CSV,
		HTML,
		TERMCOLOR
	}
		
	public OutFmt outFmt = OutFmt.PLAIN;
	public String[] argv;
	
	static String toEnumName(String x) {
		return x.replaceAll("[^A-Za-z0-9]","").toUpperCase();
	}
	public Options(String [] argv) {
		Vector<String> av = new Vector<String>(argv.length);
		for (int j=0; j<argv.length; ++j) {
			if (argv[j].charAt(0) == '-') {
				j++;
				switch(argv[j-1]) {
					case "-of": case "-out-format":
						outFmt = OutFmt.valueOf(toEnumName(argv[j]));
						break;
					default:
						throw new IllegalArgumentException("Unsupported option" + argv[j]);
				}
			} else av.add(argv[j]);
		}
		av.toArray(this.argv = new String[av.size()]);
	}

	public  static void main(String [] argv) {
		Options.common = new Options(argv);
		System.out.println(Options.common.argv.length);
		System.out.println(Options.common.argv[0]);
	}
}

