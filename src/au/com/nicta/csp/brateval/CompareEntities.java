package au.com.nicta.csp.brateval;

import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * BRAT stand-off entity comparison
 *
 * @author Antonio Jimeno Yepes (antonio.jimeno@nicta.com.au)
 * @author Karin Verspoor (karin.verspoor@unimelb.edu.au)
 *
 * Parameter options:
 * 1. [exact match] exact-match == 'true' : Type match + Exact span match (similarity-threshold not relevant)
 * 2. [approximate string match] exact-match == 'false' && similarity-threshold < 1.0 :
 *      Type match + Edit-distance-based comparison on entity annotations
 *      (does not check spans; only looks for another Entity with string-level similarity)
 * 3. [approximate boundary match] exact-match == 'false' && similarity-threshold == 1.0 :
 *      Span overlap + allows Type mismatch
 *      (only allows for differences in span boundaries and type of entity;
 *       doesn't check string-level similarity)
 *
 */
public class CompareEntities
{
	static  boolean print_inexact = false;
	static  boolean show_full_taxonomy = false;
	static  TaxonomyConfig taxonomy = new TaxonomyConfig();

	public static void main (String argc []) throws Exception
	{
		Options.common = new Options(argc);
		String folder1 = Options.common.argv[0];
		String folder2 = Options.common.argv[1];
		boolean exact_match = Boolean.parseBoolean(Options.common.argv[2]);
		double	similarity_threshold = (Options.common.argv.length > 3) ?
				Double.parseDouble(Options.common.argv[3]) : 1.0;

		evaluate(folder1, folder2, exact_match, similarity_threshold);
	}

	static void report(TableOut summary, int level, String et, int TP, int FP, int FN) {
		double precision = 0;
		double recall = 0;
		double f_measure = 0;

		if (TP+FP > 0) { precision = (double)TP/(TP+FP); }

		if (TP+FN > 0) { recall = (double)TP/(TP+FN); }

		if ((precision+recall) > 0)
		{ f_measure = (2*precision*recall)/(double)(precision+recall); }

		summary.setCell(0,taxonomy.levelPrefix(level) + et);
		summary.setCell(1,TP);
		summary.setCell(2,FP);
		summary.setCell(3,FN);
		summary.setCell(4,String.format("%1.4f", precision));
		summary.setCell(5,String.format("%1.4f", recall));
		summary.setCell(6,String.format("%1.4f", f_measure));
		summary.nextRow();
	}

	public static void evaluate(String folder1, String folder2, boolean exact_match,
								double similarity_threshold)
			throws Exception
	{
		Map <String, Integer> entityTP = new TreeMap <String, Integer> ();
		Map <String, Integer> entityFP = new TreeMap <String, Integer> ();
		Map <String, Integer> entityFN = new TreeMap <String, Integer> ();
		Integer allTP = 0;
		Integer allFP = 0;
		Integer allFN = 0;

		Set <String> entityTypes = new TreeSet <String> ();

		File folder = new File(folder1);
		TableOut mismatches = new TableOut(3);
		File[] validFiles = new File(folder2).listFiles();
		LinkedList<String> validFileNames = new LinkedList<>();
		for (File file: validFiles){
			if (file.getName().endsWith(".ann")) {
				validFileNames.add(file.getName());
			}
		}

		for (File file : folder.listFiles())
		{
			String baseName = file.getName();
			String txtName = baseName.substring(0, baseName.lastIndexOf('.')) + ".txt";
			if (baseName.endsWith(".ann") && validFileNames.contains(baseName))
			{
				validFileNames.remove(baseName);
				BackAnnotate back_annotate = new BackAnnotate(
						new String[]{folder1 + File.separator +  txtName,
								folder2 + File.separator +  txtName});

				TreeMap<Integer,BackAnnotate.SpanTag> ref_map = null;
				Document d1 = Annotations.read(file.getAbsolutePath(),
						Paths.get(folder1, file.getName()).toString());
				Document d2 = Annotations.read(folder2 + File.separator +  file.getName(),
						Paths.get(folder2, file.getName()).toString());

				if (back_annotate.hasSource()) {
					ref_map = BackAnnotate.makeTagMap(d2.getEntities());
				}
				for (Entity e : d1.getEntities())
				{
					entityTypes.add(e.getType());

					Entity match = null;

					if (exact_match)
					{	match = d2.findEntity(e); }
					else if (similarity_threshold < 1.0) // approximate similarity match
					{	match = d2.findEntitySimilarString(e, similarity_threshold);
						if (match != null && print_inexact && !e.getString().equals(match.getString()))
							System.out.println("Inexact: " + e.getString() + " ~ " + match.getString());
					}
					else // relaxed match plus similarity threshold of 1.0
					{ match = d2.findEntityOverlap(e);
						if (match != null && print_inexact) { // some kind of match for e in d2, work out what kind
							// determine what kind of inexact match we have
							if ( !e.getString().equals(match.getString())) { // inexact Span
								if (!e.getType().equals(match.getType())) { // inexact Span + inexact Type
									// TODO: Work out whether the types are related (i.e., one more specific than the other) or a 'clash'
									if ( e.getType().compareTo(match.getType()) > 0 ) {
										System.out.println("DOCUMENT:" + file.getName() + "|" + "Inexact-Span-and-TYPE|" + e.getType() + "|" + e.locationInfo() + "|" + e.getString() + " |~| " + match.getType() + "|" + match.locationInfo() + "|" + match.getString());
									} else {
										System.out.println("DOCUMENT:" + file.getName() + "|" + "Inexact-Span-and-TYPE|" + match.getType() + "|" + match.locationInfo() + "|" + match.getString() + " |~| " + e.getType() + "|" + e.locationInfo() + "|" + e.getString());
									}

								} else { // inexact Span + exact Type
									System.out.println("DOCUMENT:" + file.getName() + "|" + "Inexact-Span|" + e.getType() + "|" + e.locationInfo() + "|" + e.getString() + " |~| " + match.getType() + "|" + match.locationInfo() + "|" + match.getString());
								}
							} else { // exact Span
								if (!e.getType().equals(match.getType())) { // inexact Type
									if ( e.getType().compareTo(match.getType()) > 0 ) {
										System.out.println("DOCUMENT:" + file.getName() + "|" + "Inexact-TYPE|" + e.getType() + "|" + e.locationInfo() + "|" + e.getString() + " |~| " + match.getType() + "|" + match.locationInfo() + "|" + match.getString());
									} else {
										System.out.println("DOCUMENT:" + file.getName() + "|" + "Inexact-TYPE|" + match.getType() + "|" + match.locationInfo() + "|" + match.getString() + " |~| " + e.getType() + "|" + e.locationInfo() + "|" + e.getString());
									}
								}
							}
						}
					}// end match step

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
						{ entityFP.put(e.getType(), entityFP.get(e.getType()) + 1);}
						mismatches.setCell(0, back_annotate.locationInfo(e));
						mismatches.setCell(1, "FP");
						mismatches.setCell(2, (ref_map != null) ? back_annotate.renderContext(e,ref_map) : e.getString());
						mismatches.nextRow();
					}
				}

				if (back_annotate.hasSource()) {
					ref_map = BackAnnotate.makeTagMap(d1.getEntities());
				}
				for (Entity e : d2.getEntities())
				{
					entityTypes.add(e.getType());

					Entity match = null;

					if (exact_match)
					{	match = d1.findEntity(e);
					}
					else if (similarity_threshold < 1.0)
					{	match = d1.findEntitySimilarString(e, similarity_threshold);
						if (match != null && print_inexact && !e.getString().equals(match.getString()))
							System.out.println("Inexact: " + e.getString() + " ~ " + match.getString());
					}
					else
					{ match = d1.findEntityOverlap(e); }

					if (match == null)
					{
						if (entityFN.get(e.getType()) == null)
						{ entityFN.put(e.getType(), 1); }
						else
						{ entityFN.put(e.getType(), entityFN.get(e.getType()) + 1); }
						mismatches.setCell(0, back_annotate.locationInfo(e));
						mismatches.setCell(1,"FN");
						mismatches.setCell(2, (ref_map != null) ? back_annotate.renderContext(e,ref_map) : e.getString());
						mismatches.nextRow();
					}
				}
			}

		}
		if(!validFileNames.isEmpty()){
			throw new Exception("mandantory file is missing");
		}

		TableOut summary = new TableOut(Arrays.asList(
				new Object[] {null,"tp","fp","fn","precision","recall","f1"}
		));


		taxonomy.traverseEntities(new HierList.Visitor<TaxonomyConfig.EntityDesc>() {
			public void pre(int level, TaxonomyConfig.EntityDesc curr,
							TaxonomyConfig.EntityDesc parent) {
			}
			public void post(int level, TaxonomyConfig.EntityDesc curr,
							 TaxonomyConfig.EntityDesc parent) {
				if (parent == null) return;
				String et = curr.name;
				int TP = (entityTP.get(et) == null ? 0 : entityTP.get(et));
				int FP = (entityFP.get(et) == null ? 0 : entityFP.get(et));
				int FN = (entityFN.get(et) == null ? 0 : entityFN.get(et));
				Utils.plusMap(entityTP, parent.name, TP);
				Utils.plusMap(entityFP, parent.name, FP);
				Utils.plusMap(entityFN, parent.name, FN);
			}
		});
		taxonomy.traverseEntities(new HierList.Visitor<TaxonomyConfig.EntityDesc>() {
			public void pre(int level, TaxonomyConfig.EntityDesc curr,
							TaxonomyConfig.EntityDesc parent) {
				String et = curr.name;
				int TP = (entityTP.get(et) == null ? 0 : entityTP.get(et));
				int FP = (entityFP.get(et) == null ? 0 : entityFP.get(et));
				int FN = (entityFN.get(et) == null ? 0 : entityFN.get(et));
				if (show_full_taxonomy || TP + FP + FN > 0)
					report(summary, level, et, TP, FP, FN);
				entityTypes.remove(et);
			}
			public void post(int level, TaxonomyConfig.EntityDesc curr,
							 TaxonomyConfig.EntityDesc parent) {
			}
		});
		for (String et : entityTypes)
		{
			int TP = (entityTP.get(et) == null ? 0 : entityTP.get(et));
			int FP = (entityFP.get(et) == null ? 0 : entityFP.get(et));
			int FN = (entityFN.get(et) == null ? 0 : entityFN.get(et));

			allTP += TP;
			allFP += FP;
			allFN += FN;
			report(summary,0,et,TP,FP,FN);
		}
		report(summary, 0, "all", allTP, allFP, allFN);
		OutFormat summaryFmt = OutFormat.ofEnum(Options.common.outFmt);
		String results = summaryFmt.produceTable(summary);
		System.out.println("Summary:\n" + results);

//	System.out.println(
//		summaryFmt.produceTable(mismatches)+
//		"Summary:\n"+
//		summaryFmt.produceTable(summary));
	}
}
