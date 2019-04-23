BRAT-Eval v1.0
--------------

This tool performs pairwise comparison of annotation sets done on the same set of documents.
The annotated sets are required to be in the BRAT stand-off annotation format (http://brat.nlplab.org/standoff.html).
The current version of the tool has been tested on annotations made with Brat v1.3.
The tool only needs the jar file brateval.jar to work, which is included in the distribution file, and no further libraries are required.
The jar file contains the compiled java classes and the java source files.
In the following examples we assume that the jar file brateval.jar has been generated and is in the directory from which the java program is called, adjust the classpath parameter (-cp) accordingly.

After downloading brateval, it is possible to generate a jar file using maven:

mvn install

The jar file will be under the target folder. The name of the generated jar file contains the version of the software, e.g. BRATEval-0.0.1-SNAPSHOT.jar.
Change the name accordingly to run the examples below.

Entities are evaluated using the following command:

java -cp brateval.jar au.com.nicta.csp.brateval.CompareEntities evaluation_set_folder groundtruth_set_folder exact_match

evaluation_set_folder = folder with annotations to evaluate
groundtruth_set_folder = reference folder
exact_match = true - exact match of the entity span / false - overlap between entities span

The entity evaluation results show the statistics for true positives, false negatives and false positives.
Two entities match when they to agree on the entity type and on the span of text (exact or overlap span matches are available).

To allow for Approximate Span match, use the following settings:
java -cp brateval.jar au.com.nicta.csp.brateval.CompareEntities evaluation_set_folder groundtruth_set_folder false 1.0
     (in this case, annotations will match if the boundaries of the annotation overlap, whether or not the type matches)

Relations are evaluated using the following command:

java -cp brateval.jar au.com.nicta.csp.brateval.CompareRelations evaluation_set_folder groundtruth_set_folder exact_match verbose

evaluation_set_folder = folder with annotations to evaluate
groundtruth_set_folder = reference folder
exact_match = true - exact match of the entity span / false - overlap between entities span
verbose = true - in addition to the overall comparison statistics, the program shows examples of true positives, false negatives and false positives / false - show only the overall comparison statistics

The relation evaluation results shows the statistics for true positives, false negatives and false positives.
Two relations match when the entities and the relation type match.
The statistics show when the statistics for relations in which the entities are matched in the reference set but the relation does not exist in the reference set.

It is possible to run the software directly using maven after installing it:

mvn install

Based on the examples above, using maven consider the following call from the directory where the software was installed to perform entity comparison:

mvn exec:java -Dexec.mainClass=au.com.nicta.csp.brateval.CompareEntities -Dexec.args="evaluation_set_folder groundtruth_set_folder exact_match"

and the following one for relation comparison:

mvn exec:java -Dexec.mainClass=au.com.nicta.csp.brateval.CompareRelations -Dexec.args="evaluation_set_folder groundtruth_set_folder exact_match verbose"

The software has been used to produce results for the Variome corpus presented in the following publication:

Karin Verspoor, Antonio Jimeno Yepes, Lawrence Cavedon, Tara McIntosh, Asha Herten-Crabb, Zo� Thomas, John-Paul Plazzer (2013)
Annotating the Biomedical Literature for the Human Variome.
Database: The Journal of Biological Databases and Curation, virtual issue for BioCuration 2013 meeting. 2013:bat019.
doi:10.1093/database/bat019
