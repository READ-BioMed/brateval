package au.com.nicta.csp.brateval;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

class CompareEntitiesTest {

    private String dataDir = "";
    private String[] MATCH_TYPES = {"exact","overlap","approx"};
    private String[] ENTITY_TYPES = {"exact","inexact","hierarchical"};
    private String GOLD_PATH;
    private  String BRATEVAL_PATH;
    private String SYSTEM_PATH;


    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @BeforeEach
     void setUp() {

        dataDir = this.getClass().getClassLoader().getResource("data").getFile();
        GOLD_PATH = getPath(dataDir,"corpora","chemu_sample");
        BRATEVAL_PATH = getPath(dataDir,"bratevals","chemu_sample");
        SYSTEM_PATH = getPath(dataDir,"systems","chemu_sample");
    }


    @org.junit.jupiter.api.Test
    void report() {
    }

    @org.junit.jupiter.api.Test
    void collectValidFiles() {
    }

    @org.junit.jupiter.api.Test
    void evaluateExactExactNER() throws Exception {
        String task = "ner", gold ="gt", eval = "test", mathchType = "exact",  entityType = "exact";
        assertNERResults(task,gold,eval,mathchType,entityType);
    }

    @org.junit.jupiter.api.Test
    void evaluateExactInexactNER() throws Exception {
        String task = "ner", gold ="gt", eval = "test", matchType = "exact",  entityType = "inexact";
        assertNERResults(task,gold,eval,matchType,entityType);
    }

    @org.junit.jupiter.api.Test
    void evaluateExactHierarchicalNER() throws Exception {
        String task = "ner", gold ="gt", eval = "test", matchType = "exact",  entityType = "hierarchical";
        assertNERResults(task,gold,eval,matchType,entityType);
    }


    @org.junit.jupiter.api.Test
    void evaluateOverlapExactNER() throws Exception {
        String task = "ner", gold ="gt", eval = "test", mathchType = "overlap",  entityType = "exact";
        assertNERResults(task,gold,eval,mathchType,entityType);
    }

    @org.junit.jupiter.api.Test
    void evaluateOverlapInexactNER() throws Exception {
        String task = "ner", gold ="gt", eval = "test", matchType = "overlap",  entityType = "inexact";
        assertNERResults(task,gold,eval,matchType,entityType);
    }

    @org.junit.jupiter.api.Test
    void evaluateOverlapHierarchicalNER() throws Exception {
        String task = "ner", gold ="gt", eval = "test", matchType = "overlap",  entityType = "hierarchical";
        assertNERResults(task,gold,eval,matchType,entityType);
    }


    @org.junit.jupiter.api.Test
    void evaluateApproxExactNER() throws Exception {
        String task = "ner", gold ="gt", eval = "test", mathchType = "approx",  entityType = "exact";
        assertNERResults(task,gold,eval,mathchType,entityType,0.8);
    }

    @org.junit.jupiter.api.Test
    void evaluateApproxInexactNER() throws Exception {
        String task = "ner", gold ="gt", eval = "test", matchType = "approx",  entityType = "inexact";
        assertNERResults(task,gold,eval,matchType,entityType,0.8);
    }

    @org.junit.jupiter.api.Test
    void evaluateApproxHierarchicalNER() throws Exception {
        String task = "ner", gold ="gt", eval = "test", matchType = "approx",  entityType = "hierarchical";
        assertNERResults(task,gold,eval,matchType,entityType,0.8);
    }

    private void assertNERResults(String task, String gold, String eval, String matchType, String entityType) throws Exception {
        assertNERResults(task, gold, eval, matchType, entityType,-1);
    }


    private void assertNERResults(String task, String gold, String eval, String matchType, String entityType, double threshold) throws Exception {
        OptionBuilder optionBuilder = new OptionBuilder();
        CompareEntities.main( optionBuilder
                .gtPath(getPath(GOLD_PATH,task,gold))
                .evalPath(getPath(SYSTEM_PATH,task,eval))
                .matchType(matchType)
                .entityType(entityType)
                .threshold(threshold)
                .build().split(" ")
        );

        ///                                  File name follows this pattern: task-gt-system-matchType-entityType.txt
        String brateEvalPath = buildBratEvalFileName(task, gold,eval, matchType,entityType, threshold);
        String[] expected = Files.readAllLines(Paths.get(BRATEVAL_PATH,brateEvalPath)).toArray(new String[1]);
        String[] actual = outContent.toString().split("\n");
        for (int i = 0; i < expected.length-3; i++){
            Assertions.assertEquals(expected[i+3],actual[i+3]);//we start at i+3 because the first three lines are debugging lines
        }
    }

    private String buildBratEvalFileName(String task, String gold, String eval, String mathchType, String entityType, double threshold) {

        if (!mathchType.equals("approx")){
            return String.format("%s-%s-%s-%s-%s.txt",task,gold,eval,mathchType,entityType);
        } else {
            return String.format("%s-%s-%s-%s%.1f-%s.txt",task,gold,eval,mathchType,threshold,entityType);
        }
    }


    public static String getPath(String first, String ... paths){
        return Paths.get(first,paths).toString();
    }


}

class OptionBuilder {

    private String gtPath = null;
    private String evalPath = null;

    private String matchType = null;
    private String entityType = null;
    private double threshold = 1.0;


    String build() {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(" -g " + gtPath );
        stringBuilder.append(" -e " + evalPath );
        if (matchType.equals("approx")){

            matchType = "approx " + threshold;
        }

        stringBuilder.append(" -s " + (matchType==null?"exact":matchType));
        stringBuilder.append(" -t " + (entityType==null?"exact":entityType));

        return stringBuilder.toString().trim();
    }

    OptionBuilder gtPath(String gtPath){
        this.gtPath = gtPath;
        return this;
    }


    OptionBuilder evalPath(String evalPath){
        this.evalPath = evalPath;
        return this;
    }

    OptionBuilder matchType(String matchType) {
        this.matchType = matchType;
        return this;
    }

    OptionBuilder entityType(String entityType) {
        this.entityType = entityType;
        return this;
    }

    OptionBuilder threshold(double threshold) {
        this.threshold = threshold;
        return this;
    }

}