package org.javastro.ivoacore.tap;

import adql.db.DBChecker;
import adql.db.DBTable;
import adql.parser.ADQLParser;
import adql.parser.QueryChecker;
import adql.parser.grammar.ParseException;
import adql.query.ADQLSet;
import adql.translator.ADQLTranslator;
import adql.translator.PgSphereTranslator;
import adql.translator.TranslationException;
import org.javastro.ivoacore.tap.upload.TapUploadService.UploadContext;
import org.slf4j.*;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The TapQueryProcessor class processes queries written in the Astronomical Data Query
 * Language (ADQL), translating them into SQL queries that can be executed against a database.
 * This class also provides features for parsing ADQL queries, applying constraints defined
 * in a TAP (Table Access Protocol) Job Specification, and replacing upload table references.
 */
public class TapQueryProcessor {

    private static final Logger log = LoggerFactory.getLogger(TapQueryProcessor.class);

    /**
     * Constructs a new instance of TapQueryProcessor.
     */
    public TapQueryProcessor() {
    }

    /**
     * Parses an ADQL (Astronomical Data Query Language) query into an {@code ADQLSet} object,
     * which represents the parsed components of the query. The method utilises a query checker
     * to validate the tables referenced in the query based on the provided database table definitions.
     *
     * @param tables A list of {@code DBTable} objects representing the database tables that
     *               can be accessed and validated while parsing the ADQL query.
     * @param jobSpec An instance of {@code TAPJobSpecification} containing the TAP job details,
     *                including the ADQL query string to be parsed.
     * @return An {@code ADQLSet} object that encapsulates the parsed components of the provided
     *         ADQL query, including its metadata and structure.
     * @throws ParseException If the ADQL query contains syntax errors or violates any rules
     *                        during parsing or validation.
     */
    public ADQLSet parseQuery(List<DBTable> tables, TAPJobSpecification jobSpec) throws ParseException {

        QueryChecker checker = new DBChecker(tables);

        ADQLParser parser = new ADQLParser();
        parser.setQueryChecker(checker);

        return parser.parseQuery(jobSpec.adqlQuery);
    }

    /**
     * Translates an ADQL (Astronomical Data Query Language) query into an SQL query. This method processes
     * the ADQL query set using the provided TAP job specification and optionally applies transformations
     * to handle uploaded table references.
     *
     * @param query The ADQL query set to be translated. It encapsulates components such as tables, columns,
     *              and constraints defined in the query.
     * @param jobSpec An instance of {@code TAPJobSpecification} containing the TAP job details,
     *                such as the query and its execution parameters.
     * @param uploads An optional {@code UploadContext} instance that contains information about upload
     *               table mappings, allowing references to uploaded tables in the translated query.
     * @return A {@code String} representing the SQL query translated from the given ADQL query.
     * @throws TranslationException If the translation process encounters an error such as syntax issues
     *                              or unsupported ADQL features.
     */
    public String translateQuery(ADQLSet query, TAPJobSpecification jobSpec, List<UploadContext> uploads) throws TranslationException {
        String sql = translateADQLToSQL(query, jobSpec);

        if (uploads != null && !uploads.isEmpty()) {
            sql = replaceUploadTableReferences(sql, uploads);
        }
        return sql;
    }

    /**
     * Translates an ADQL (Astronomical Data Query Language) query into an equivalent SQL query.
     * This method enforces a maximum row retrieval limit (MAXREC) at the SQL level if defined in the TAP job specification.
     * If the query already has a limit but exceeds MAXREC, the limit is adjusted. If no limit exists,
     * MAXREC is applied to the query. It utilises a specialized ADQL translator to perform the conversion.
     *
     * @param query The ADQL query set to be translated into SQL. This includes information
     *              about columns, tables, and constraints defined in ADQL.
     * @return A {@code String} representing the translated SQL query equivalent to the provided ADQL query.
     * @throws TranslationException If an error occurs during the translation process, such as invalid syntax
     *                              or unsupported operations in the ADQL query.
     */
    private String translateADQLToSQL(ADQLSet query, TAPJobSpecification jobSpec) throws TranslationException {
        if (jobSpec.maxrec != null && jobSpec.maxrec >= 0) {
            // Apply MAXREC at SQL level to avoid fetching unnecessary rows.
            //TODO need to add system wide MAXREC - needs to get from ExecutionPolicy / environment
            if(query.hasLimit() && query.getLimit() > jobSpec.maxrec) {
                log.info("Applying MAXREC limit of {} to query with existing limit of {}", jobSpec.maxrec, query.getLimit());
                query.setLimit(Math.toIntExact(jobSpec.maxrec));
            }
            else if (!query.hasLimit()) {
                log.info("Applying MAXREC limit of {} to query with no existing limit", jobSpec.maxrec);
                query.setLimit(Math.toIntExact(jobSpec.maxrec));
            }
        }
        logQuery(query);     // TODO - IF log enabled
        ADQLTranslator translator = new PgSphereTranslator();
        return translator.translate(query);
    }

    /**
     * Replaces references to logical table names in the given SQL query with their corresponding
     * physical table names based on the provided upload context. This is useful for translating
     * queries that reference TAP_UPLOAD tables to their physical equivalents.
     *
     * @param sql The SQL query string containing logical table references that need to be replaced.
     * @param contexts A List of {@code UploadContext} containing the logical and physical table
     *                 name mappings.
     * @return A modified SQL query with logical table references replaced by their physical counterparts.
     */
    private String replaceUploadTableReferences(String sql, List<UploadContext> contexts) {

        for (UploadContext context : contexts) {
            sql = sql.replaceAll("(?i)\"TAP_UPLOAD\"\\s*\\.\\s*\"" +
                            Pattern.quote(context.logicalTableName()) +
                            "\"",
                    Matcher.quoteReplacement(context.physicalTableName()));

            sql = sql.replaceAll(
                    "(?i)TAP_UPLOAD\\s*\\.\\s*" +
                            Pattern.quote(context.logicalTableName()),
                    Matcher.quoteReplacement(context.physicalTableName()));

            sql = sql.replaceAll(
                    "(?i)\\b" + Pattern.quote(context.logicalTableName()) + "\\b",
                    Matcher.quoteReplacement(context.physicalTableName()));
        }

        return sql;
    }

    /**
     * Logs details about the ADQL query, specifically the resulting columns and their associated tables.
     * Each column's ADQL name and the name of its originating table (if available) are included in the log output.
     * If no table is associated with a column, "table null" is logged for that column.
     *
     * @param query the ADQL query set containing the resulting columns to be logged.
     */
    private void logQuery(ADQLSet query) {
        Arrays.stream(query.getResultingColumns())
                .forEach(col -> {
                    if(col.getTable() != null) {
                        log.debug( "ADQL column: {} table {}", col.getADQLName(), col.getTable().getADQLName());
                    } else {
                        log.debug( "ADQL column: {} table null", col.getADQLName());
                    }
                });
    }
}
