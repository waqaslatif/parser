package com.stella.ccd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * @author ali
 * @author Shamsi
 * @author Waqas
 *
 */
public class CCDSQLScriptBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(CCDSQLScriptBuilder.class);

    private static final String XML_EXTENSION = "xml";
    private final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder dBuilder;
    private Document document;

    private final CCDElementExtractor immunizationExtractor = new ImmunizationSectionExtractor();
    private final CCDElementExtractor progressNoteEntryExtractor = new ProgressNoteSectionExtractor();
    private final CCDElementExtractor activeProblemExtractor = new ActiveProblemActExtractor();

    public void build(final String directoryPath) {
        StringBuilder sbCcdaSQL = new StringBuilder();
        try {
            File ccdDatasetDir = new File(directoryPath);
            if (ccdDatasetDir.isDirectory()) {
                for (File ccdFile : ccdDatasetDir.listFiles()) {
                    // extractProgressNoteSection(doc);
                    if (FilenameUtils.getExtension(ccdFile.getName()).equals(XML_EXTENSION)) {
                        LOG.info("----------------------------");
                        LOG.info("Reading File : " + ccdFile.getName());
                        
                        dBuilder = dbFactory.newDocumentBuilder();
                        document = dBuilder.parse(ccdFile);
                        document.getDocumentElement().normalize();
                        
                        sbCcdaSQL.append(immunizationExtractor.extract(document));
                        sbCcdaSQL.append(progressNoteEntryExtractor.extract(document));
                        sbCcdaSQL.append(activeProblemExtractor.extract(document));
                    }
                }

                LOG.info("Generating SQL File .....");
                writeSQLFile(sbCcdaSQL.toString(), directoryPath);
            }
            LOG.debug(sbCcdaSQL.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Extracts the active problems CCD document and build SQL insert queries for ActiveProblem table.
     * 
     * @param doc
     * @return
     * @throws XPathExpressionException
     */
    private void writeSQLFile(final String strSql, final String dirPath) throws IOException {

        final File file = new File(dirPath + "\\hugo_ccda.sql");
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }
        final FileWriter fw = new FileWriter(file.getAbsoluteFile());
        final BufferedWriter bw = new BufferedWriter(fw);
        bw.write(strSql);
        bw.close();
        LOG.info("Generated SQL script in '" + file.getAbsolutePath() + "'");
    }

}