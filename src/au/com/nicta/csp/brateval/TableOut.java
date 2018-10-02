package au.com.nicta.csp.brateval;

/**
 * 
 * Table output producer
 * 
 * @author Andrey (Andreas) Scherbakov (andreas@softwareengineer.pro)
 *
 */

import java.util.List;
import java.util.LinkedList;
import java.util.Vector;

public class TableOut {
	LinkedList<Vector<Object>> content = new LinkedList<Vector<Object>> ();
	int nColumn = 0;
	List<Object> header = null;
	Vector<Object> currRow = null;

	TableOut(int col_num)
	{
		nColumn = col_num;
	}

	TableOut(List<Object> hdr)
	{
		nColumn = hdr.size();
		header  = hdr;
	}

	void setCell(int j, Object o) {
		if (currRow == null) {
			currRow = new Vector<Object>(nColumn);
			currRow.setSize(nColumn);
		}
		currRow.set(j,o);
	}
	
	int nextRow() {
		if (currRow != null)
		{
			content.add(currRow);
			currRow = null;
		}
		return content.size();
	}

	int commitRow() {
		if (currRow != null) return nextRow();
		return content.size();		
	}
	
	LinkedList<Vector<Object>> getContent() {
		commitRow();
		return content;
	}
	
	List<Object> getHeader() { return header; }
}

	