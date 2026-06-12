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
import org.javastro.ivoacore.tap.upload.TapUploadService;
import org.javastro.ivoacore.tap.upload.TapUploadService.UploadContext;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TapQueryProcessor {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TapQueryProcessor.class);

    public ADQLSet parseQuery(List<DBTable> tables, TAPJobSpecification jobSpec) throws ParseException {

        QueryChecker checker = new DBChecker(tables);

        ADQLParser parser = new ADQLParser();
        parser.setQueryChecker(checker);

        return parser.parseQuery(jobSpec.adqlQuery);
    }

    public String translateQuery(ADQLSet query, TAPJobSpecification jobSpec, UploadContext upload) throws TranslationException {
        String sql = translateADQLToSQL(query, jobSpec);

        if (upload != null) {
            sql = replaceUploadTableReferences(sql, upload);
        }
        return sql;
    }

    /**
     * Translates an ADQL (Astronomical Data Query Language) query into an equivalent SQL query.
     * This method enforces a maximum row retrieval limit (MAXREC) at the SQL level if defined in the TAP job specification.
     * If the query already has a limit but exceeds MAXREC, the limit is adjusted. If no limit exists,
     * MAXREC is applied to the query. It utilizes a specialized ADQL translator to perform the conversion.
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

    private String replaceUploadTableReferences(
            String sql,
            TapUploadService.UploadContext context) {

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
