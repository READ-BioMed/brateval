package au.com.nicta.csp.brateval;

import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * BRAT stand-off entity comparison
 *
 * @author Antonio Jimeno Yepes (antonio.jimeno@nicta.com.au)
 * @author Karin Verspoor (karin.verspoor@unimelb.edu.au)
 */
public class CompareEntities {
    static boolean verbose_output;
    static boolean show_full_taxonomy;
    static TaxonomyConfig taxonomy = TaxonomyConfig.singleton();

    public static void main(String argc[]) throws Exception {
        Options.common = new Options(argc);
        verbose_output = Options.common.verbose;
        taxonomy.readConfigFile(Options.common.configFile);
        show_full_taxonomy = Options.common.show_full_taxonomy;

        String evalFolder = Options.common.evalFolder;
        String goldFolder = Options.common.goldFolder;

        System.out.println("Evaluating Folder: " + evalFolder + " against Gold Folder: " + goldFolder + " Match settings : " + Options.common.matchType.toString());
        evaluate(goldFolder, evalFolder, Options.common.matchType);
    }

    static void report(TableOut summary, int level, String et, int TP, int FP, int FN) {
        double precision = 0;
        double recall = 0;
        double f_measure = 0;

        if (TP + FP > 0) {
            precision = (double) TP / (TP + FP);
        }

        if (TP + FN > 0) {
            recall = (double) TP / (TP + FN);
        }

        if ((precision + recall) > 0) {
            f_measure = (2 * precision * recall) / (double) (precision + recall);
        }

        summary.setCell(0, taxonomy.levelPrefix(level) + et);
        summary.setCell(1, TP);
        summary.setCell(2, FP);
        summary.setCell(3, FN);
        summary.setCell(4, String.format("%1.4f", precision));
        summary.setCell(5, String.format("%1.4f", recall));
        summary.setCell(6, String.format("%1.4f", f_measure));
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

    @Deprecated
    public static String evaluate(String testFolder, String goldFolder, boolean exact_match, double similarity_threshold) throws Exception {
        evaluate(goldFolder, testFolder, MatchType.buildEntityMatchType(exact_match, verbose_output, similarity_threshold));
        return testFolder;
    }

    public static void evaluate(String goldFolder, String evalFolder, MatchType mt)
            throws Exception {
        TableOut summary = new TableOut(Arrays.asList(
                new Object[]{null, "tp", "fp", "fn", "precision", "recall", "f1"}
        ));

        OutFormat summaryFmt = OutFormat.ofEnum(Options.common.outFmt);
        evaluate(goldFolder,  evalFolder, mt,summary,summaryFmt);
    }


    public static void evaluate(String goldFolder, String evalFolder, MatchType mt, TableOut summary, OutFormat summaryFmt)
            throws Exception {

        Map<String, Integer> entityTP = new TreeMap<String, Integer>();
        Map<String, Integer> entityFP = new TreeMap<String, Integer>();
        Map<String, Integer> entityFN = new TreeMap<String, Integer>();
        Integer allTP = 0;
        Integer allFP = 0;
        Integer allFN = 0;
        Set<String> entityTypes = new TreeSet<String>();
        TableOut mismatches = new TableOut(3);

        File goldFile = new File(goldFolder);
        Path goldPath = goldFile.toPath();
        File evalFile = new File(evalFolder);
        Path evalPath = evalFile.toPath();
        LinkedList<File> validFilesGold = new LinkedList<>();
        LinkedList<File> validFilesEval = new LinkedList<>();
        collectValidFiles(goldFile, validFilesGold);
        collectValidFiles(evalFile, validFilesEval);

        // Iterate through Gold standard files
        for (File gfile : validFilesGold) {
            if (verbose_output)
                System.out.println("Processing: " + gfile.getName());
            String baseName = gfile.getName();
            Path path = gfile.toPath();
            Path relPath = goldPath.relativize(path);

            // find corresponding file in evaluation folder
            File efile = null;
            Path path2 = null;
            for (File validFile2 : validFilesEval) {
                Path validPath2 = validFile2.toPath();
                Path relPath2 = evalPath.relativize(validPath2);
                if (relPath.equals(relPath2)) {
                    efile = validFile2;
                    path2 = validPath2;
                }
            }

            // Compare gfile with corresponding evaluation file (efile)
            if (efile != null) {
                validFilesEval.remove(efile);

                // find text files
                String txtName = baseName.substring(0, baseName.lastIndexOf('.')) + ".txt";
                BackAnnotate back_annotate = new BackAnnotate(
                        new String[]{path.getParent() + txtName,
                                path2.getParent() + txtName});

                TreeMap<Integer, BackAnnotate.SpanTag> ref_map = null;

                Document goldDoc = Annotations.read(gfile.getAbsolutePath(), path.toString());
                Document evalDoc = Annotations.read(efile.getAbsolutePath(), path2.toString());

                if (back_annotate.hasSource()) {
                    ref_map = BackAnnotate.makeTagMap(evalDoc.getEntities());
                }
                for (Entity e : goldDoc.getEntities()) {
                    entityTypes.add(e.getType());

                    EntityMatchResult matchResult = null;

                    matchResult = evalDoc.findEntity(e, mt);
                    if (matchResult != null) {

                        if (verbose_output) {
                            String outStr = matchResult.toString();
                            if (outStr != null)
                                System.out.println(outStr);
                        }

                        if (entityTP.get(e.getType()) == null) {
                            entityTP.put(e.getType(), 1);
                        } else {
                            entityTP.put(e.getType(), entityTP.get(e.getType()) + 1);
                        }
                    } else // no entity matching gold entity in evaluation file -- FN
                    {
                        if (entityFN.get(e.getType()) == null) {
                            entityFN.put(e.getType(), 1);
                        } else {
                            entityFN.put(e.getType(), entityFN.get(e.getType()) + 1);
                        }

                        if (verbose_output)
                            System.out.println("DOCUMENT:" + gfile.getName() + "|" + "FALSE_NEGATIVE|" + e.getType() + "|" + e.locationInfo() + "|" + e.getString());

                        mismatches.setCell(0, back_annotate.locationInfo(e));
                        mismatches.setCell(1, "FN");
                        mismatches.setCell(2, (ref_map != null) ? back_annotate.renderContext(e, ref_map) : e.getString());
                        mismatches.nextRow();
                    }
                }

                if (back_annotate.hasSource()) {
                    ref_map = BackAnnotate.makeTagMap(goldDoc.getEntities());
                }

                // count any entities in evaluation doc not matched when looking through the gold entities as FPs
                for (Entity e : evalDoc.getEntities()) {
                    entityTypes.add(e.getType());

                    //Entity match = null;
                    EntityMatchResult matchResult = null;

                    matchResult = goldDoc.findEntity(e, mt);

                    // No match for entity in evaluation file in gold -- FP
                    if (matchResult == null) {
                        if (entityFP.get(e.getType()) == null) {
                            entityFP.put(e.getType(), 1);
                        } else {
                            entityFP.put(e.getType(), entityFP.get(e.getType()) + 1);
                        }

                        if (verbose_output)
                            System.out.println("DOCUMENT:" + gfile.getName() + "|" + "FALSE_POSITIVE|" + e.getType() + "|" + e.locationInfo() + "|" + e.getString());

                        mismatches.setCell(0, back_annotate.locationInfo(e));
                        mismatches.setCell(1, "FP");
                        mismatches.setCell(2, (ref_map != null) ? back_annotate.renderContext(e, ref_map) : e.getString());
                        mismatches.nextRow();
                    }
                }
            }
        }

        if (!validFilesEval.isEmpty()) {
            throw new Exception("mandatory file is missing");
        }




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
        for (String et : entityTypes) {
            int TP = (entityTP.get(et) == null ? 0 : entityTP.get(et));
            int FP = (entityFP.get(et) == null ? 0 : entityFP.get(et));
            int FN = (entityFN.get(et) == null ? 0 : entityFN.get(et));

            allTP += TP;
            allFP += FP;
            allFN += FN;
            report(summary, 0, et, TP, FP, FN);
        }
        report(summary, 0, "all", allTP, allFP, allFN);
        String results = summaryFmt.produceTable(summary);
        System.out.println("Summary:\n" + results);
    }
}
