package au.com.nicta.csp.brateval;

/**
 * 
 * .csv output markup format specification class
 * 
 * @author Andrey (Andreas) Scherbakov (andreas@softwareengineer.pro)
 *
 */

public class CsvOutFormat extends OutFormat
{	
	public String header() {return "";}
	public String footer() {return "";}

	public String beginSpan(byte flags) {return "";}
	public String endSpan(byte flags) {return "";}
	public String beginTable() {return "";}
	public String endTable() {return "";}
	public String nextCell() {return ",";}
	public String beginRow() {return "";}
	public String endRow() {return "\n";}

	public void convert(StringBuffer buff, String s) { buff.append(s); }

	@Override
	public void convertCell(StringBuffer buff, int start, Object header) {
		String s = buff.substring(start);
		if (s.contains(",") || s.contains("\"")) {
			buff.replace(start, start + 1, "\"");
			buff.replace(start+1, buff.length(), s.replaceAll("\"","\"\""));
			buff.append("\"");
		}
	}
}