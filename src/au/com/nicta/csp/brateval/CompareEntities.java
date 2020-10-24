package au.com.nicta.csp.brateval;

import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.util.*;

import au.com.nicta.csp.brateval.MatchType.SpanMatch;
import au.com.nicta.csp.brateval.MatchType.TypeMatch;

/**
 *
 * BRAT stand-off entity comparison
 *
 * @author Antonio Jimeno Yepes (antonio.jimeno@nicta.com.au)
 * @author Karin Verspoor (karin.verspoor@unimelb.edu.au)
 *
 *
 */
public class CompareEntities
{
	static  boolean verbose_output;
	static  boolean show_full_taxonomy;
	static  TaxonomyConfig taxonomy = new TaxonomyConfig();

	public static void main (String argc []) throws Exception
	{
		Options.common = new Options(argc);
		verbose_output = Options.common.verbose;
		show_full_taxonomy = Options.common.show_full_taxonomy;
		
		String compFolder = Options.common.compFolder;
		String goldFolder = Options.common.goldFolder;
		//		boolean exact_match = Boolean.parseBoolean(Options.common.argv[2]);
		//double	similarity_threshold = (Options.common.argv.length > 3) ?
		//		Double.parseDouble(Options.common.argv[3]) : 1.0;

		System.out.println("Evaluating Folder: " + compFolder + " against Gold Folder: " + goldFolder + " Match settings : " + Options.common.matchType.toString() );
		evaluate(compFolder, goldFolder, Options.common.matchType);
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


    public static void collectValidFiles(File dir, LinkedList<File> fileList) {
		try {
			File[] files = dir.listFiles();
			
			for (File file : files) {
				if (file.isDirectory()) {
				    if (verbose_output)
					System.out.println("directory:" + file.getCanonicalPath());
				    collectValidFiles(file, fileList);
				} else if (file.getName().endsWith(".ann")) {
					fileList.add(file);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}    

    public static void evaluate(String folder1, String folder2, MatchType mt)
			throws Exception
	{
		Map <String, Integer> entityTP = new TreeMap <String, Integer> ();
		Map <String, Integer> entityFP = new TreeMap <String, Integer> ();
		Map <String, Integer> entityFN = new TreeMap <String, Integer> ();
		Integer allTP = 0;
		Integer allFP = 0;
		Integer allFN = 0;
		Set <String> entityTypes = new TreeSet <String> ();
		TableOut mismatches = new TableOut(3);

		File folder1File = new File(folder1);
		Path folder1Path = folder1File.toPath();
		File folder2File = new File(folder2);
		Path folder2Path = folder2File.toPath();
		LinkedList<File> validFiles1 = new LinkedList<>();
		LinkedList<File> validFiles2 = new LinkedList<>();
		collectValidFiles(folder1File, validFiles1);
		collectValidFiles(folder2File, validFiles2);

		for (File file : validFiles1)
		{
		    if (verbose_output)
			    System.out.println("Processing: " + file.getName());
			String baseName = file.getName();
			Path path = file.toPath();
			Path relPath = folder1Path.relativize(path);

			// find corresponding file in folder2
			File file2 = null;
			Path path2 = null;
			for (File validFile2 : validFiles2)
			{
			    Path validPath2 = validFile2.toPath();
			    Path relPath2 = folder2Path.relativize(validPath2);
			    if (relPath.equals(relPath2)) {
					file2 = validFile2;
					path2 = validPath2;
			    }
			}
			
			if (file2 != null)
			{
				validFiles2.remove(file2);

				// find text files
				String txtName = baseName.substring(0, baseName.lastIndexOf('.')) + ".txt";
				BackAnnotate back_annotate = new BackAnnotate(
									      new String[]{path.getParent() +  txtName,
											   path2.getParent() +  txtName});

				TreeMap<Integer,BackAnnotate.SpanTag> ref_map = null;

				Document d1 = Annotations.read(file.getAbsolutePath(), path.toString());
				Document d2 = Annotations.read(file2.getAbsolutePath(), path2.toString());

				if (back_annotate.hasSource()) {
					ref_map = BackAnnotate.makeTagMap(d2.getEntities());
				}
				for (Entity e : d1.getEntities())
				{
					entityTypes.add(e.getType());

					EntityMatchResult matchResult = null;

					matchResult = d2.findEntity(e, mt);
					if ( matchResult != null ) {

						if ( verbose_output ) {
							String outStr = matchResult.toString();
							if (outStr != null)
								System.out.println(outStr);
						}

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

						if (verbose_output)
						    System.out.println("DOCUMENT:" + file.getName() + "|" + "FALSE_POSITIVE|" + e.getType() + "|" + e.locationInfo() + "|" + e.getString());

						mismatches.setCell(0, back_annotate.locationInfo(e));
						mismatches.setCell(1, "FP");
						mismatches.setCell(2, (ref_map != null) ? back_annotate.renderContext(e,ref_map) : e.getString());
						mismatches.nextRow();
					}
				}

				if (back_annotate.hasSource()) {
					ref_map = BackAnnotate.makeTagMap(d1.getEntities());
				}

				// count any entities in d2 not matched when looking through the d1 entities as FNs
				for (Entity e : d2.getEntities())
				{
					entityTypes.add(e.getType());

					//Entity match = null;
					EntityMatchResult matchResult = null;

					matchResult = d1.findEntity(e, mt);

					/** 
					if (mt.getSpanMatchType() == SpanMatch.EXACT)
					{	
						matchResult = d1.findEntity(e,mt);
					}
					else if (mt.getSimThreshold() < 1.0)
					{	match = d1.findEntitySimilarString(e, mt.getSimThreshold());
						if (match != null && verbose_output && !e.getString().equals(match.getString()))
							System.out.println("Inexact: " + e.getString() + " ~ " + match.getString());
					}
					else
					{ matchResult = d1.findEntity(e, mt); }
					*/

					// No match for d2 entity in d1
					if (matchResult == null)
					{
						if (entityFN.get(e.getType()) == null)
						{ entityFN.put(e.getType(), 1); }
						else
						{ entityFN.put(e.getType(), entityFN.get(e.getType()) + 1); }

						if (verbose_output)
						    System.out.println("DOCUMENT:" + file.getName() + "|" + "FALSE_NEGATIVE|" + e.getType() + "|" + e.locationInfo() + "|" + e.getString());
						
						mismatches.setCell(0, back_annotate.locationInfo(e));
						mismatches.setCell(1,"FN");
						mismatches.setCell(2, (ref_map != null) ? back_annotate.renderContext(e,ref_map) : e.getString());
						mismatches.nextRow();
					}
				}
			}
		}

		if(!validFiles2.isEmpty()){
			throw new Exception("mandatory file is missing");
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
