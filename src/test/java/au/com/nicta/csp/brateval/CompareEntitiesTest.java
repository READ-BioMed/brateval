package au.com.nicta.csp.brateval;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

class CompareEntitiesTest {

    private String dataDir = "";
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
     void setUp() throws URISyntaxException {
        dataDir = Paths.get(this.getClass().getClassLoader().getResource("data").toURI()).toString();
        GOLD_PATH = getPath(dataDir,"corpora","chemu_sample");
        BRATEVAL_PATH = getPath(dataDir,"bratevals","chemu_sample");
        SYSTEM_PATH = getPath(dataDir,"systems","chemu_sample");
    }


    @Test
    @Tag("e2e")
    void evaluateExactExact() throws Exception {
        String task = "ner", gold ="gt", eval = "test", mathchType = "exact",  entityType = "exact";
        assertResults(task,gold,eval,mathchType,entityType);
    }

    @Test
    @Tag("e2e")
    void evaluateExactInexact() throws Exception {
        String task = "ner", gold ="gt", eval = "test", matchType = "exact",  entityType = "inexact";
        assertResults(task,gold,eval,matchType,entityType);
    }

    @Test
    @Tag("e2e")
    void evaluateExactHierarchical() throws Exception {
        String task = "ner", gold ="gt", eval = "test", matchType = "exact",  entityType = "hierarchical";
        assertResults(task,gold,eval,matchType,entityType);
    }

    @Test
    @Tag("e2e")
    void evaluateOverlapExact() throws Exception {
        String task = "ner", gold ="gt", eval = "test", mathchType = "overlap",  entityType = "exact";
        assertResults(task,gold,eval,mathchType,entityType);
    }

    @Test
    @Tag("e2e")
    void evaluateOverlapInexact() throws Exception {
        String task = "ner", gold ="gt", eval = "test", matchType = "overlap",  entityType = "inexact";
        assertResults(task,gold,eval,matchType,entityType);
    }

    @Test
    @Tag("e2e")
    void evaluateOverlapHierarchical() throws Exception {
        String task = "ner", gold ="gt", eval = "test", matchType = "overlap",  entityType = "hierarchical";
        assertResults(task,gold,eval,matchType,entityType);
    }


    @Test
    @Tag("e2e")
    void evaluateApproxExact() throws Exception {
        String task = "ner", gold ="gt", eval = "test", mathchType = "approx",  entityType = "exact";
        assertResults(task,gold,eval,mathchType,entityType,0.8);
    }

    @Test
    @Tag("e2e")
    void evaluateApproxInexact() throws Exception {
        String task = "ner", gold ="gt", eval = "test", matchType = "approx",  entityType = "inexact";
        assertResults(task,gold,eval,matchType,entityType,0.8);
    }

    @Test
    @Tag("e2e")
    void evaluateApproxHierarchical() throws Exception {
        String task = "ner", gold ="gt", eval = "test", matchType = "approx",  entityType = "hierarchical";
        assertResults(task,gold,eval,matchType,entityType,0.8);
    }

    private void assertResults(String task, String gold, String eval, String matchType, String entityType) throws Exception {
        assertResults(task, gold, eval, matchType, entityType,-1);
    }


    private void assertResults(String task, String gold, String eval, String matchType, String entityType, double threshold) throws Exception {
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
        String[] actual = outContent.toString().trim().split("\n");
        for (int i = 0; i < expected.length-3; i++){
            Assertions.assertEquals(expected[i+3].trim(),actual[i+3].trim());//we start at i+3 because the first three lines are debugging lines
        }
    }

    static String buildBratEvalFileName(String task, String gold, String eval, String mathchType, String entityType, double threshold) {

        if (!mathchType.equals("approx")){
            return String.format("%s-%s-%s-%s-%s.txt",task,gold,eval,mathchType,entityType);
        } else {
            return String.format("%s-%s-%s-%s%.1f-%s.txt",task,gold,eval,mathchType,threshold,entityType);
        }
    }


    public static String getPath(String first, String ... paths) {
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