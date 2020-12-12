package au.com.nicta.csp.brateval;

import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * BRAT stand-off relation comparison
 *
 * @author Antonio Jimeno Yepes (antonio.jimeno@nicta.com.au)
 *
 */
public class CompareRelations
{
    static  boolean verbose_output;
    static  boolean show_full_taxonomy = false;
	static  TaxonomyConfig taxonomy = TaxonomyConfig.singleton();

	public static void main (String argc []) throws Exception
	{
		Options.common = new Options(argc);
		verbose_output = Options.common.verbose;
		taxonomy = new TaxonomyConfig(Options.common.configFile);
		show_full_taxonomy = Options.common.show_full_taxonomy;
		
		String evalFolder = Options.common.evalFolder;
		String goldFolder = Options.common.goldFolder;

		System.out.println("Evaluating Folder: " + evalFolder + " against Gold Folder: " + goldFolder + " Match settings : " + Options.common.matchType.toString() );
		evaluate(goldFolder, evalFolder, Options.common.matchType);
    }
    
    static void report(int level, String rt, int TP, int FP, int FN, int MFP, int MFN) {
        double precision = 0;
        double recall = 0;
        double f_measure = 0;

        if (TP+FP > 0) { precision = (double)TP/(TP+FP); }

        if (TP+FN > 0) { recall = (double)TP/(TP+FN); }

        if ((precision+recall) > 0)
        { f_measure = (2*precision*recall)/(double)(precision+recall); }

        System.out.println(rt
                + "|tp:" + TP
                + "|fp:" + FP
                + "|fn:" + FN
                + "|precision:" + String.format("%1.4f", precision)
                + "|recall:" + String.format("%1.4f", recall)
                + "|f1:" + String.format("%1.4f", f_measure)
                + "|fpm:" + MFP
                + "|fnm:" + MFN
        );
    }

    public static Map<String,Object> asMap(int level, String rt, int TP, int FP, int FN, int MFP, int MFN) {
        double precision = 0;
        double recall = 0;
        double f_measure = 0;

        if (TP+FP > 0) { precision = (double)TP/(TP+FP); }

        if (TP+FN > 0) { recall = (double)TP/(TP+FN); }

        if ((precision+recall) > 0)
        { f_measure = (2*precision*recall)/(double)(precision+recall); }

        double finalPrecision = precision;
        double finalRecall = recall;
        double finalF_measure = f_measure;
        Map<String,Object> map = new HashMap<String, Object>()
        {
			private static final long serialVersionUID = 1680940560190051518L;

			{
                put("rt", rt);
                put("tp", (double) TP);
                put("fp", (double) FP);
                put("fn", (double) FN);
                put("precision", (double) finalPrecision);
                put("recall", (double) finalRecall);
                put("f1", (double) finalF_measure);
                put("fpm", (double) MFP);
                put("fnm", (double) MFN);
            }
        };

        return map;
    }

    public static void evaluate(String goldFolderPath, String evalFolderPath, MatchType mt)
            throws IOException
    {
        if ( goldFolderPath == null || evalFolderPath == null) {
            System.out.println("Missing folder for evaluation. Aborting.");
            return;
        }

        Set <String> relationTypes = new TreeSet <String> ();

        Map <String, Integer> relationTP = new HashMap <String, Integer> ();
        Map <String, Integer> relationFP = new HashMap <String, Integer> ();
        Map <String, Integer> relationFN = new HashMap <String, Integer> ();

        Map <String, Integer> relationMissingFP = new HashMap <String, Integer> ();
        Map <String, Integer> relationMissingFN = new HashMap <String, Integer> ();

        File goldFolder = new File(goldFolderPath);
        File[] evalFiles = new File(evalFolderPath).listFiles();
        LinkedList<String> evalFileNames = new LinkedList<>();
        for (File evalFile: evalFiles){
            if (evalFile.getName().endsWith(".ann")) {
                evalFileNames.add(evalFile.getName());
            }
        }
        for (File goldFile : goldFolder.listFiles())
        {
            String baseName = goldFile.getName();
            if (goldFile.getName().endsWith(".ann") && evalFileNames.contains(baseName))
            {
                evalFileNames.remove(baseName);
                Map <String, RelationComparison> relations = new TreeMap <String, RelationComparison> ();

                Document goldDoc = Annotations.read(goldFile.getAbsolutePath(),
                        Paths.get(goldFolderPath, goldFile.getName()).toString());
                Document evalDoc = Annotations.read(evalFolderPath + File.separator +  goldFile.getName(),
                        Paths.get(goldFolderPath, goldFile.getName()).toString());

                // TPs and FNs
                for (Relation rel : goldDoc.getRelations())
                {
                    if (relations.get(rel.getRelationType()) == null)
                    { relations.put(rel.getRelationType(), new RelationComparison()); }

                    RelationMatchResult result = evalDoc.findRelation(rel, mt);
                    if (result != null) {
                        relations.get(rel.getRelationType()).addTP(rel);
                    } else {
                        // didn't find matching relation in evalDoc -- FN
                        relations.get(rel.getRelationType()).addFN(rel);
                    }

                }

                // FPs
                for (Relation rel : evalDoc.getRelations())
                {
                    if (relations.get(rel.getRelationType()) == null) {
                        relations.put(rel.getRelationType(), new RelationComparison());
                    }

                    RelationMatchResult result = goldDoc.findRelation(rel, mt);
                    if (result == null) {
                        // relation exists in evalDoc that doesn't exist in gold doc -- FP
                        relations.get(rel.getRelationType()).addFP(rel);
                    }
                }

                for (Map.Entry <String, RelationComparison> entry : relations.entrySet())
                {
                    relationTypes.add(entry.getKey());

                    for (Relation rel : entry.getValue().getTP())
                    {
                        if (verbose_output)
                        {
                            System.out.println(goldFile.getName());
                            System.out.println("TP " + rel.getRelationType());
                            System.out.println(rel.getEntity1());
                            System.out.println(rel.getEntity2());
                            System.out.println("------");
                        }
                    }

                    for (Relation rel : entry.getValue().getFN())
                    {
                        if (verbose_output)
                        {
                            System.out.println(goldFile.getName());
                            System.out.println("FN " + rel.getRelationType());
                            System.out.println(rel.getEntity1());
                            System.out.println(rel.getEntity2());
                            System.out.println("------");
                        }

                        EntityMatchResult matchResult1, matchResult2 = null;
                        matchResult1 = goldDoc.findEntity(rel.getEntity1(), mt);
                        matchResult2 = goldDoc.findEntity(rel.getEntity2(), mt);


                        if (!(matchResult1 != null && matchResult2 != null)) {
                            if (relationMissingFN.get(rel.getRelationType()) == null) {
                                relationMissingFN.put(rel.getRelationType(), 1);
                            } else {
                                relationMissingFN.put(rel.getRelationType(),
                                        relationMissingFN.get(rel.getRelationType()) + 1);
                            }
                        }

                    }

                    for (Relation rel : entry.getValue().getFP())
                    {
                        if (verbose_output)
                        {
                            System.out.println(goldFile.getName());
                            System.out.println("FP " + rel.getRelationType());
                            System.out.println(rel.getEntity1());
                            System.out.println(rel.getEntity2());
                            System.out.println("------");
                        }

                        EntityMatchResult matchResult1, matchResult2 = null;
                        matchResult1 = goldDoc.findEntity(rel.getEntity1(), mt);
                        matchResult2 = goldDoc.findEntity(rel.getEntity2(), mt);


                        if (!(matchResult1 != null && matchResult2 != null)) {
                            if (relationMissingFP.get(rel.getRelationType()) == null) {
                                relationMissingFP.put(rel.getRelationType(), 1);
                            } else {
                                relationMissingFP.put(rel.getRelationType(),
                                        relationMissingFP.get(rel.getRelationType()) + 1);
                            }
                        }

                    }

                    // Overall counting
                    if (relationTP.get(entry.getKey()) == null)
                    { relationTP.put(entry.getKey(), entry.getValue().getTP().size()); }
                    else
                    { relationTP.put(entry.getKey(), relationTP.get(entry.getKey()) + entry.getValue().getTP().size());}

                    if (relationFP.get(entry.getKey()) == null)
                    { relationFP.put(entry.getKey(), entry.getValue().getFP().size()); }
                    else
                    { relationFP.put(entry.getKey(), relationFP.get(entry.getKey()) + entry.getValue().getFP().size());}

                    if (relationFN.get(entry.getKey()) == null)
                    { relationFN.put(entry.getKey(), entry.getValue().getFN().size()); }
                    else
                    { relationFN.put(entry.getKey(), relationFN.get(entry.getKey()) + entry.getValue().getFN().size());}
                }
            }
        }
        if(!evalFileNames.isEmpty()){
            throw new java.lang.Error("mandantory file is missing");
        }

//    System.out.println("");
        System.out.println("Summary:");

        taxonomy.traverseRelations(new HierList.Visitor<TaxonomyConfig.RelationDesc>() {
            public void pre(int level, TaxonomyConfig.RelationDesc curr,
                            TaxonomyConfig.RelationDesc parent) {
            }
            public void post(int level, TaxonomyConfig.RelationDesc curr,
                             TaxonomyConfig.RelationDesc parent) {
                if (parent == null) return;
                String rt = curr.name;
                int TP = (relationTP.get(rt) == null ? 0 : relationTP.get(rt));
                int FP = (relationFP.get(rt) == null ? 0 : relationFP.get(rt));
                int FN = (relationFN.get(rt) == null ? 0 : relationFN.get(rt));
                int MFP = (relationMissingFP.get(rt) == null ? 0 : relationMissingFP.get(rt));
                int MFN = (relationMissingFN.get(rt) == null ? 0 : relationMissingFN.get(rt));
                Utils.plusMap(relationTP, parent.name, TP);
                Utils.plusMap(relationFP, parent.name, FP);
                Utils.plusMap(relationFN, parent.name, FN);
                Utils.plusMap(relationMissingFP, parent.name, MFP);
                Utils.plusMap(relationMissingFN, parent.name, MFN);
            }
        });
        taxonomy.traverseRelations(new HierList.Visitor<TaxonomyConfig.RelationDesc>() {
            public void pre(int level, TaxonomyConfig.RelationDesc curr,
                            TaxonomyConfig.RelationDesc parent) {
                String rt = curr.name;
                int TP = (relationTP.get(rt) == null ? 0 : relationTP.get(rt));
                int FP = (relationFP.get(rt) == null ? 0 : relationFP.get(rt));
                int FN = (relationFN.get(rt) == null ? 0 : relationFN.get(rt));
                int MFP = (relationMissingFP.get(rt) == null ? 0 : relationMissingFP.get(rt));
                int MFN = (relationMissingFN.get(rt) == null ? 0 : relationMissingFN.get(rt));

                if (show_full_taxonomy || TP + FP + FN + MFP + MFN > 0)
                    report(level, rt, TP, FP, FN, MFP, MFN);
                relationTypes.remove(rt);
            }
            public void post(int level, TaxonomyConfig.RelationDesc curr,
                             TaxonomyConfig.RelationDesc parent) {
            }
        });

        int totalTP = 0;
        int totalFP = 0;
        int totalFN = 0;
        int totalMFP = 0;
        int totalMFN = 0;

        List<Map<String,Object>> results = new LinkedList<>();
        for (String rt : relationTypes)
        {
            int TP = (relationTP.get(rt) == null ? 0 : relationTP.get(rt));
            int FP = (relationFP.get(rt) == null ? 0 : relationFP.get(rt));
            int FN = (relationFN.get(rt) == null ? 0 : relationFN.get(rt));
            int MFP = (relationMissingFP.get(rt) == null ? 0 : relationMissingFP.get(rt));
            int MFN = (relationMissingFN.get(rt) == null ? 0 : relationMissingFN.get(rt));

//          report(0, rt, TP, FP, FN, MFP, MFN);
            results.add(asMap(0, rt, TP, FP, FN, MFP, MFN));

            totalTP += TP;
            totalFP += FP;
            totalFN += FN;
            totalMFP += MFP;
            totalMFN += MFN;
        }

        results.add(asMap(0, "all", totalTP, totalFP, totalFN, totalMFP, totalMFN));

        for (String rt : relationTypes)
        {
            int TP = (relationTP.get(rt) == null ? 0 : relationTP.get(rt));
            int FP = (relationFP.get(rt) == null ? 0 : relationFP.get(rt));
            int FN = (relationFN.get(rt) == null ? 0 : relationFN.get(rt));
            int MFP = (relationMissingFP.get(rt) == null ? 0 : relationMissingFP.get(rt));
            int MFN = (relationMissingFN.get(rt) == null ? 0 : relationMissingFN.get(rt));
            report(0, rt, TP, FP, FN, MFP, MFN);
        }
        report(0, "all", totalTP, totalFP, totalFN, totalMFP, totalMFN);
    }
}