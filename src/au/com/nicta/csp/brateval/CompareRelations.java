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
    static  boolean show_full_taxonomy = false;
    static  TaxonomyConfig taxonomy = new TaxonomyConfig();

    public static void main (String argc []) throws IOException
    {
        String folder1 = argc[0];
        String folder2 = argc[1];
        boolean exact_match = Boolean.parseBoolean(argc[2]);
        boolean verbose = Boolean.parseBoolean(argc[3]);

        evaluate(folder1, folder2, exact_match, verbose);
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

    public static void evaluate(String folder1, String folder2, boolean exact_match, boolean verbose)
            throws IOException
    {
        Set <String> relationTypes = new TreeSet <String> ();

        Map <String, Integer> relationTP = new HashMap <String, Integer> ();
        Map <String, Integer> relationFP = new HashMap <String, Integer> ();
        Map <String, Integer> relationFN = new HashMap <String, Integer> ();

        Map <String, Integer> relationMissingFP = new HashMap <String, Integer> ();
        Map <String, Integer> relationMissingFN = new HashMap <String, Integer> ();

        File folder = new File(folder1);
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
            if (file.getName().endsWith(".ann") && validFileNames.contains(baseName))
            {
                validFileNames.remove(baseName);
                Map <String, RelationComparison> relations = new TreeMap <String, RelationComparison> ();

                Document d1 = Annotations.read(file.getAbsolutePath(),
                        Paths.get(folder1, file.getName()).toString());
                Document d2 = Annotations.read(folder2 + File.separator +  file.getName(),
                        Paths.get(folder1, file.getName()).toString());

                // TPs and FPs
                for (Relation rel : d1.getRelations())
                {
                    if (relations.get(rel.getRelationType()) == null)
                    { relations.put(rel.getRelationType(), new RelationComparison()); }

                    if  (exact_match)
                    {
                        if (d2.findRelation(rel) != null)
                        { relations.get(rel.getRelationType()).addTP(rel); }
                        else
                        { relations.get(rel.getRelationType()).addFP(rel); }
                    }
                    else
                    {
                        if (d2.findRelationOverlap(rel) != null)
                        { relations.get(rel.getRelationType()).addTP(rel); }
                        else
                        { relations.get(rel.getRelationType()).addFP(rel); }
                    }
                }

                // FNs
                for (Relation rel : d2.getRelations())
                {
                    if (relations.get(rel.getRelationType()) == null)
                    { relations.put(rel.getRelationType(), new RelationComparison()); }
                    if (exact_match){
                        if (d1.findRelation(rel) == null){
                            relations.get(rel.getRelationType()).addFN(rel);
                        }
                    }else if (d1.findRelationOverlap(rel) == null)
                    { relations.get(rel.getRelationType()).addFN(rel);}
                }

                for (Map.Entry <String, RelationComparison> entry : relations.entrySet())
                {
                    relationTypes.add(entry.getKey());

                    for (Relation rel : entry.getValue().getTP())
                    {
                        if (verbose)
                        {
                            System.out.println(file.getName());
                            System.out.println("TP " + rel.getRelationType());
                            System.out.println(rel.getEntity1());
                            System.out.println(rel.getEntity2());
                            System.out.println("------");
                        }
                    }

                    for (Relation rel : entry.getValue().getFN())
                    {
                        if (verbose)
                        {
                            System.out.println(file.getName());
                            System.out.println("FN " + rel.getRelationType());
                            System.out.println(rel.getEntity1());
                            System.out.println(rel.getEntity2());
                            System.out.println("------");
                        }

                        if (exact_match)
                        {
                            if (!(d1.findEntity(rel.getEntity1()) != null && d1.findEntity(rel.getEntity2()) != null))
                            {
                                if (relationMissingFN.get(rel.getRelationType()) == null)
                                { relationMissingFN.put(rel.getRelationType(), 1); }
                                else
                                { relationMissingFN.put(rel.getRelationType(), relationMissingFN.get(rel.getRelationType()) + 1); }
                            }
                        }
                        else
                        {
                            if (!(d1.findEntityOverlap(rel.getEntity1()) != null && d1.findEntityOverlap(rel.getEntity2()) != null))
                            {
                                if (relationMissingFN.get(rel.getRelationType()) == null)
                                { relationMissingFN.put(rel.getRelationType(), 1); }
                                else
                                { relationMissingFN.put(rel.getRelationType(), relationMissingFN.get(rel.getRelationType()) + 1); }
                            }
                        }
                    }

                    for (Relation rel : entry.getValue().getFP())
                    {
                        if (verbose)
                        {
                            System.out.println(file.getName());
                            System.out.println("FP " + rel.getRelationType());
                            System.out.println(rel.getEntity1());
                            System.out.println(rel.getEntity2());
                            System.out.println("------");
                        }

                        if (exact_match)
                        {
                            if (!(d2.findEntity(rel.getEntity1()) != null && d2.findEntity(rel.getEntity2()) != null))
                            {
                                if (relationMissingFP.get(rel.getRelationType()) == null)
                                { relationMissingFP.put(rel.getRelationType(), 1); }
                                else
                                { relationMissingFP.put(rel.getRelationType(), relationMissingFP.get(rel.getRelationType()) + 1); }
                            }
                        }
                        else
                        {
                            if (!(d2.findEntityOverlap(rel.getEntity1()) != null && d2.findEntityOverlap(rel.getEntity2()) != null))
                            {
                                if (relationMissingFP.get(rel.getRelationType()) == null)
                                { relationMissingFP.put(rel.getRelationType(), 1); }
                                else
                                { relationMissingFP.put(rel.getRelationType(), relationMissingFP.get(rel.getRelationType()) + 1); }
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
        if(!validFileNames.isEmpty()){
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