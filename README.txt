BRAT-Eval v1.0
--------------

This tool performs pairwise comparison of annotation sets done on the same set of documents.
The annotated sets are required to be in the BRAT stand-off annotation format (http://brat.nlplab.org/standoff.html).
The current version of the tool has been tested on annotations made with Brat v1.2.
The tool only needs the jar file brateval.jar to work, which is included in the distribution file, and no further libraries are required.
The jar file contains the compiled java classes and the java source files.
In the following examples we assume that the jar file is in the directory from which the java program is called, adjust the classpath parameter (-cp) accordingly.

Entities are evaluated using the following command:

java -cp brateval.jar au.com.nicta.csp.brateval.CompareEntities reference_set_folder evaluation_set_folder exact_match

reference_set_folder = reference folder
evaluation_set_folder = folder with annotations to evaluate
exact_match = true - exact match of the entity span / false - overlap between entities span

The entity evaluation results show the statistics for true positives, false negatives and false positives.
Two entities match when they to agree on the entity type and on the span of text (exact or overlap span matches are available).

Relations are evaluated using the following command:

java -cp brateval.jar au.com.nicta.csp.brateval.CompareRelations reference_set_folder evaluation_set_folder exact_match verbose

reference_set_folder = reference folder
evaluation_set_folder = folder with annotations to evaluate
exact_match = true - exact match of the entity span / false - overlap between entities span
verbose = true - in addition to the overall comparison statistics, the program shows examples of true positives, false negatives and false positives / false - show only the overall comparison statistics

The relation evaluation results shows the statistics for true positives, false negatives and false positives.
Two relations match when the entities and the relation type match.
The statistics show when the statistics for relations in which the entities are matched in the reference set but the relation does not exist in the reference set.

The software has been used to produce results for the Variome corpus presented in the following publication:

Karin Verspoor, Antonio Jimeno Yepes, Lawrence Cavedon, Tara McIntosh, Asha Herten-Crabb, Zoë Thomas, John-Paul Plazzer (2013)
Annotating the Biomedical Literature for the Human Variome.
Database: The Journal of Biological Databases and Curation, virtual issue for BioCuration 2013 meeting. 2013:bat019.
doi:10.1093/database/bat019