package au.com.nicta.csp.brateval;

import com.opencsv.CSVReader;

import java.io.*;
import java.nio.file.Paths;
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

    static String outFolder;

    public static void main (String argc []) throws Exception
	{
        Locale.setDefault(new Locale("en", "US")); //Reported scores use "." as decimal seperator
        Options.common = new Options(argc);
		verbose_output = Options.common.verbose;
		taxonomy = new TaxonomyConfig(Options.common.configFile);
		show_full_taxonomy = Options.common.show_full_taxonomy;
		
		String evalFolder = Options.common.evalFolder;
		String goldFolder = Options.common.goldFolder;
        outFolder = Options.common.outputFolder;

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
                + "," + TP
                + "," + FP
                + "," + FN
                + "," + String.format("%1.4f", precision)
                + "," + String.format("%1.4f", recall)
                + "," + String.format("%1.4f", f_measure)
                + "," + MFP
                + "," + MFN
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

    @Deprecated
    public static void evaluate(String evalFolderPath, String goldFolderPath, boolean exact_match, boolean verbose) throws Exception {
	    evaluate(goldFolderPath,evalFolderPath,MatchType.from(exact_match,verbose));
    }

    public static void evaluate(String goldFolderPath, String evalFolderPath, MatchType mt) throws Exception {
        List<Map<String,Object>> results = new LinkedList<>();
        evaluate(goldFolderPath, evalFolderPath, mt,results);

    }

    public static void evaluate(String goldFolderPath, String evalFolderPath, MatchType mt, List<Map<String,Object>> results)
            throws Exception
    {

        if (goldFolderPath == null || evalFolderPath == null) {
            System.out.println("Missing folder for evaluation. Aborting.");
            return;
        }

        Set<String> relationTypes = new TreeSet<>();

        Map<String, Integer> relationTP = new HashMap<>();
        Map<String, Integer> relationFP = new HashMap<>();
        Map<String, Integer> relationFN = new HashMap<>();
        Map<String, Integer> relationMissingFP = new HashMap<>();
        Map<String, Integer> relationMissingFN = new HashMap<>();

        File goldFolder = new File(goldFolderPath);
        File evalFolder = new File(evalFolderPath);
        Set<String> evalFileNames = new TreeSet<>();
        for (File evalFile: evalFolder.listFiles()) {
            if (evalFile.getName().endsWith(".ann")) {
                evalFileNames.add(evalFile.getName());
            }
        }

        for (File goldFile : goldFolder.listFiles()) {
            if (goldFile.getName().endsWith(".ann")) {
                if(!evalFileNames.contains(goldFile.getName())) {
                    throw new Exception("mandatory file is missing: " + goldFile.getName());
                }

                Map<String, RelationComparison> relations = new TreeMap<>();

                Document goldDoc = Annotations.read(goldFile.getAbsolutePath(),
                        Paths.get(goldFolderPath, goldFile.getName()).toString());
                Document evalDoc = Annotations.read(evalFolderPath + File.separator +  goldFile.getName(),
                        Paths.get(goldFolderPath, goldFile.getName()).toString());

                // TPs and FNs
                for (Relation rel : goldDoc.getRelations()) {
                    if (relations.get(rel.getRelationType()) == null) {
                        relations.put(rel.getRelationType(), new RelationComparison());
                    }

                    RelationMatchResult result = evalDoc.findRelation(rel, mt);
                    if (result != null) {
                        relations.get(rel.getRelationType()).addTP(rel);
                        evalDoc.removeRelation(result.getRel().getId()); // so that they won't be matched again
                    } else {
                        // didn't find matching relation in evalDoc -- FN
                        relations.get(rel.getRelationType()).addFN(rel);
                    }
                }

                // all the remaining relations are FPs, if not, they would have already been matched.
                for (Relation rel : evalDoc.getRelations()) {
                    if (relations.get(rel.getRelationType()) == null) {
                        relations.put(rel.getRelationType(), new RelationComparison());
                    }
                    relations.get(rel.getRelationType()).addFP(rel);
                }

                for (Map.Entry <String, RelationComparison> entry : relations.entrySet()) {
                    relationTypes.add(entry.getKey());

                    for (Relation rel : entry.getValue().getTP()) {
                        if (verbose_output) {
                            System.out.println(goldFile.getName());
                            System.out.println("TP " + rel.getRelationType());
                            System.out.println(rel.getEntity1());
                            System.out.println(rel.getEntity2());
                            System.out.println("------");
                        }
                    }

                    for (Relation rel : entry.getValue().getFN()) {
                        if (verbose_output) {
                            System.out.println(goldFile.getName());
                            System.out.println("FN " + rel.getRelationType());
                            System.out.println(rel.getEntity1());
                            System.out.println(rel.getEntity2());
                            System.out.println("------");
                        }
                        if (evalDoc.findEntity(rel.getEntity1(), mt) == null ||
                                evalDoc.findEntity(rel.getEntity2(), mt) == null) {
                            relationMissingFN.merge(rel.getRelationType(), 1, Integer::sum);
                        }
                    }

                    for (Relation rel : entry.getValue().getFP()) {
                        if (verbose_output) {
                            System.out.println(goldFile.getName());
                            System.out.println("FP " + rel.getRelationType());
                            System.out.println(rel.getEntity1());
                            System.out.println(rel.getEntity2());
                            System.out.println("------");
                        }
                        if (goldDoc.findEntity(rel.getEntity1(), mt) == null ||
                                goldDoc.findEntity(rel.getEntity2(), mt) == null) {
                            relationMissingFP.merge(rel.getRelationType(), 1, Integer::sum);
                        }
                    }

                    // Overall counting
                    relationTP.merge(entry.getKey(), entry.getValue().getTP().size(), Integer::sum);
                    relationFP.merge(entry.getKey(), entry.getValue().getFP().size(), Integer::sum);
                    relationFN.merge(entry.getKey(), entry.getValue().getFN().size(), Integer::sum);
                }
            }
        }
        //<Catches the System-out-output>
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        PrintStream stdout = System.out;
        System.setOut(new java.io.PrintStream(out)); //Catch sysrtem out
        //</Catches the System-out-output>

        System.out.println("Summary:");
        System.out.println("type,tp,fp,fn,precision,recall,f1,fpm,fnm");

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

        //<Reset System.out to default>
        System.setOut(stdout);
        System.out.println(out);
        //</Reset System.out to default>

        //<Here we parse the system out stream>
        int precisionPos = 4; int recallPos=5; int f1Pos=6;

        List<Double> macroPrecisionScores = new ArrayList<>();
        List<Double> macroRecallScores = new ArrayList<>();
        List<Double> macroF1Scores = new ArrayList<>();

        FileWriter output = new FileWriter(outFolder +File.separator +"scores.txt");
        String[] nextLine;
        CSVReader reader = new CSVReader(new StringReader(out.toString()));
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine != null) { //Until end
                if (nextLine.length == 9  && !nextLine[0].equals("type")){
                    if(nextLine[0].equals("all")){

                        output.append("Task2bMicroP: " +nextLine[precisionPos] +"\n");
                        output.append("Task2bMicroR: " +nextLine[recallPos] +"\n");
                        output.append("Task2bMicroF1: " +nextLine[f1Pos] +"\n");

                        //System.out.println("MicroP=" +nextLine[precisionPos]);
                        //System.out.println("MicroR=" +nextLine[recallPos]);
                        //System.out.println("MicroF1=" +nextLine[f1Pos]);
                    }
                    else{
                        //System.out.println(String.join(",", nextLine));
                        macroPrecisionScores.add(Double.parseDouble(nextLine[precisionPos]));
                        macroRecallScores.add(Double.parseDouble(nextLine[recallPos]));
                        macroF1Scores.add(Double.parseDouble(nextLine[f1Pos]));
                    }
                }

            }
        }

        output.append("Task2bMacroP: " +String.format("%1.4f",macroPrecisionScores.stream().mapToDouble(var -> var).average().getAsDouble()) +"\n");
        output.append("Task2bMacroR: " +String.format("%1.4f",macroRecallScores.stream().mapToDouble(var -> var).average().getAsDouble())+"\n");
        output.append("Task2bMacroF1: " +String.format("%1.4f",macroF1Scores.stream().mapToDouble(var -> var).average().getAsDouble())+"\n");

        //System.out.println("MacroP=" +String.format("%1.4f",macroPrecisionScores.stream().mapToDouble(var -> var).average().getAsDouble()));
        //System.out.println("MacroR=" +String.format("%1.4f",macroRecallScores.stream().mapToDouble(var -> var).average().getAsDouble()));
        //System.out.println("MacroF1=" +String.format("%1.4f",macroF1Scores.stream().mapToDouble(var -> var).average().getAsDouble()));

        output.close();
        //</Here we parse the system out stream>
    }
}