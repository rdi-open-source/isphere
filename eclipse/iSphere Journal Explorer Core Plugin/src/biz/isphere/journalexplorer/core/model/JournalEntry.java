/*******************************************************************************
 * Copyright (c) 2012-2021 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Initial idea and development: Isaac Ramirez Herrera
 * Continued and adopted to iSphere: iSphere Project Team
 *******************************************************************************/

package biz.isphere.journalexplorer.core.model;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import biz.isphere.base.internal.IntHelper;
import biz.isphere.base.internal.StringHelper;
import biz.isphere.core.swt.widgets.ContentAssistProposal;
import biz.isphere.journalexplorer.base.interfaces.IDatatypeConverterDelegate;
import biz.isphere.journalexplorer.core.Messages;
import biz.isphere.journalexplorer.core.model.adapters.JOESDProperty;
import biz.isphere.journalexplorer.core.model.adapters.JournalProperties;
import biz.isphere.journalexplorer.core.model.adapters.JournalProperty;
import biz.isphere.journalexplorer.core.model.dao.ColumnsDAO;
import biz.isphere.journalexplorer.core.model.shared.JournaledFile;
import biz.isphere.journalexplorer.core.preferences.Preferences;
import biz.isphere.journalexplorer.core.ui.widgets.contentassist.TableColumnContentAssistProposal;

import com.google.gson.annotations.Expose;
import com.ibm.as400.access.AS400Text;

/**
 * Class to store the properties of a journal entry as received from a journal
 * or a journal output file. Journal entries the children of a
 * {@link JournalEntries} and can be serialized in Json format.
 */
public class JournalEntry {

    private static final String ADDRESS_FAMILY_UNKNOWN = "0";
    private static final String ADDRESS_FAMILY_IPV4 = "4";
    private static final String ADDRESS_FAMILY_IPV6 = "6";

    public static final String USER_GENERATED = "U"; //$NON-NLS-1$

    private static final int JOCODE = 0;
    private static final int JOENTT = 1;
    private static final int JOJOB = 2;
    private static final int JOUSER = 3;
    private static final int JONBR = 4;
    private static final int JOLIB = 5;
    private static final int JOOBJ = 6;
    private static final int JOMBR = 7;
    private static final int JODATE = 8;
    private static final int JOTIME = 9;
    private static final int JOTSTP = 10;
    private static final int JOPGM = 11;
    private static final int JOPGMLIB = 12;
    private static final int JOOBJTYP = 13;
    private static final int JOFILTYP = 14;
    private static final int JOSYNM = 15;
    private static final int JORCV = 16;
    private static final int JORCVLIB = 17;
    private static final int JOUSPF = 17;
    private static final int JOSEQN = 18;
    private static final int JOCCID = 19;
    private static final int JOCTRR = 20;

    private static HashMap<String, Integer> basicColumnMappings;
    static {
        basicColumnMappings = new HashMap<String, Integer>();
        basicColumnMappings.put(ColumnsDAO.JOCODE.name(), JOCODE);
        basicColumnMappings.put(ColumnsDAO.JOENTT.name(), JOENTT);
        basicColumnMappings.put(ColumnsDAO.JOJOB.name(), JOJOB);
        basicColumnMappings.put(ColumnsDAO.JOUSER.name(), JOUSER);
        basicColumnMappings.put(ColumnsDAO.JONBR.name(), JONBR);
        basicColumnMappings.put(ColumnsDAO.JOLIB.name(), JOLIB);
        basicColumnMappings.put(ColumnsDAO.JOOBJ.name(), JOOBJ);
        basicColumnMappings.put(ColumnsDAO.JOMBR.name(), JOMBR);
        basicColumnMappings.put(ColumnsDAO.JODATE.name(), JODATE);
        basicColumnMappings.put(ColumnsDAO.JOTIME.name(), JOTIME);
        basicColumnMappings.put(ColumnsDAO.JOTSTP.name(), JOTSTP);
        basicColumnMappings.put(ColumnsDAO.JOPGM.name(), JOPGM);
        basicColumnMappings.put(ColumnsDAO.JOPGMLIB.name(), JOPGMLIB);
        basicColumnMappings.put(ColumnsDAO.JOOBJTYP.name(), JOOBJTYP);
        basicColumnMappings.put(ColumnsDAO.JOFILTYP.name(), JOFILTYP);
        basicColumnMappings.put(ColumnsDAO.JOSYNM.name(), JOSYNM);
        basicColumnMappings.put(ColumnsDAO.JORCV.name(), JORCV);
        basicColumnMappings.put(ColumnsDAO.JORCVLIB.name(), JORCVLIB);
        basicColumnMappings.put(ColumnsDAO.JOUSPF.name(), JOUSPF);
        basicColumnMappings.put(ColumnsDAO.JOSEQN.name(), JOSEQN);
        basicColumnMappings.put(ColumnsDAO.JOCCID.name(), JOCCID);
        basicColumnMappings.put(ColumnsDAO.JOCTRR.name(), JOCTRR);
    }

    private static List<ContentAssistProposal> basicProposals;
    static {
        basicProposals = new LinkedList<ContentAssistProposal>();
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOCODE.name(), ColumnsDAO.JOCODE.sqlType(), ColumnsDAO.JOCODE
            .description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOENTT.name(), ColumnsDAO.JOENTT.sqlType(), ColumnsDAO.JOENTT
            .description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOJOB.name(), ColumnsDAO.JOJOB.sqlType(), ColumnsDAO.JOJOB.description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOUSER.name(), ColumnsDAO.JOUSER.sqlType(), ColumnsDAO.JOUSER
            .description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JONBR.name(), ColumnsDAO.JONBR.sqlType(), ColumnsDAO.JONBR.description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOLIB.name(), ColumnsDAO.JOLIB.sqlType(), ColumnsDAO.JOLIB.description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOOBJ.name(), ColumnsDAO.JOOBJ.sqlType(), ColumnsDAO.JOOBJ.description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOMBR.name(), ColumnsDAO.JOMBR.sqlType(), ColumnsDAO.JOMBR.description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JODATE.name(), ColumnsDAO.JODATE.sqlType(), ColumnsDAO.JODATE
            .description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOTIME.name(), ColumnsDAO.JOTIME.sqlType(), ColumnsDAO.JOTIME
            .description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOTSTP.name(), ColumnsDAO.JOTSTP.sqlType(), ColumnsDAO.JOTSTP
            .description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOPGM.name(), ColumnsDAO.JOPGM.sqlType(), ColumnsDAO.JOPGM.description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOPGMLIB.name(), ColumnsDAO.JOPGMLIB.sqlType(), ColumnsDAO.JOPGMLIB
            .description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOOBJTYP.name(), ColumnsDAO.JOOBJTYP.sqlType(), ColumnsDAO.JOOBJTYP
            .description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOFILTYP.name(), ColumnsDAO.JOFILTYP.sqlType(), ColumnsDAO.JOFILTYP
            .description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOSYNM.name(), ColumnsDAO.JOSYNM.sqlType(), ColumnsDAO.JOSYNM
            .description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JORCV.name(), ColumnsDAO.JORCV.sqlType(), ColumnsDAO.JORCV.description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JORCVLIB.name(), ColumnsDAO.JORCVLIB.sqlType(), ColumnsDAO.JORCVLIB
            .description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOUSPF.name(), ColumnsDAO.JOUSPF.sqlType(), ColumnsDAO.JOUSPF
            .description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOSEQN.name(), ColumnsDAO.JOSEQN.sqlType(), ColumnsDAO.JOSEQN
            .description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOCCID.name(), ColumnsDAO.JOCCID.sqlType(), ColumnsDAO.JOCCID
            .description()));
        basicProposals.add(new TableColumnContentAssistProposal(ColumnsDAO.JOCTRR.name(), ColumnsDAO.JOCTRR.sqlType(), ColumnsDAO.JOCTRR
            .description()));
    }

    @Expose(serialize = true, deserialize = true)
    private String connectionName;
    @Expose(serialize = true, deserialize = true)
    private String outputFileName;
    @Expose(serialize = true, deserialize = true)
    private String outputFileLibraryName;
    @Expose(serialize = true, deserialize = true)
    private String outputFileMemberName;

    @Expose(serialize = true, deserialize = true)
    private int id;
    @Expose(serialize = true, deserialize = true)
    private java.sql.Timestamp timestamp;

    @Expose(serialize = true, deserialize = true)
    private int entryLength; // JOENTL
    @Expose(serialize = true, deserialize = true)
    private BigInteger sequenceNumber; // JOSEQN
    @Expose(serialize = true, deserialize = true)
    private String journalCode; // JOCODE
    @Expose(serialize = true, deserialize = true)
    private String entryType; // JOENTT
    @Expose(serialize = true, deserialize = true)
    private java.sql.Date date; // JODATE
    @Expose(serialize = true, deserialize = true)
    private java.sql.Time time; // JOTIME
    @Expose(serialize = true, deserialize = true)
    private String jobName; // JOJOB
    @Expose(serialize = true, deserialize = true)
    private String jobUserName; // JOUSER
    @Expose(serialize = true, deserialize = true)
    private int jobNumber; // JONBR
    @Expose(serialize = true, deserialize = true)
    private String programName; // JOPGM
    @Expose(serialize = true, deserialize = true)
    private String programLibrary; // JOLIB
    @Expose(serialize = true, deserialize = true)
    private String objectName; // JOOBJ
    @Expose(serialize = true, deserialize = true)
    private String objectLibrary; // JOLIB
    @Expose(serialize = true, deserialize = true)
    private String memberName; // JOMBR
    @Expose(serialize = true, deserialize = true)
    private BigInteger countRrn; // JOCTRR
    /**
     * Contains an indicator for the operation. The following tables show
     * specific values for this field, if applicable:
     * <p>
     * APYJRNCHG (B AT, D DD, E EQ, F AY, Q QH) and RMVJRNCHG (E EX, F RC)
     * journal entries. The results of the apply or remove operation:
     * <ul>
     * <li>0 = Command completed normally.</li>
     * <li>1 = Command completed abnormally.</li>
     * </ul>
     * COMMIT (C CM) journal entry. Whether the commit operation was initiated
     * by the system or the user:
     * <ul>
     * <li>0 = All record-level changes were committed for a commit operation
     * initiated by a user.</li>
     * <li>2 = All record-level changes were committed for a commit operation
     * initiated by the operating system.</li>
     * </ul>
     * INZPFM (F IZ) journal entry. Indicates the type of record initialization
     * that was done:
     * <ul>
     * <li>0 = *DFT (default)</li>
     * <li>1 = *DLT (delete)</li>
     * </ul>
     * IPL (J IA, J IN) and in-use (B OI, C BA, D ID, E EI, F IU, I DA, J JI, Q
     * QI) journal entries. For in-use entries, indicates whether the object was
     * synchronized with the journal:
     * <ul>
     * <li>0 = Object was synchronized with journal</li>
     * <li>1 = Object was not synchronized with journal</li>
     * </ul>
     * Journal code R, all journal entry types except IL. Whether a before-image
     * is present:
     * <ul>
     * <li>0 = Before-image is not present. If before-images are being
     * journaled, this indicates that an update operation or delete operation is
     * being requested for a record that has already been deleted.</li>
     * <li>1 = 1 = Before-image is present.</li>
     * </ul>
     * ROLLBACK (C RB) journal entry. How the rollback operation was initiated
     * and whether it was successful:
     * <ul>
     * <li>0 = All record-level changes were rolled back for a rollback
     * operation initiated by a user.</li>
     * <li>1 = Not all record-level changes were successfully rolled back for a
     * rollback operation initiated by a user.</li>
     * <li>2 = All record-level changes were rolled back for a rollback
     * operation initiated by the operating system.</li>
     * <li>3 = Not all record-level changes were rolled back for a rollback
     * operation initiated by the operating system.</li>
     * </ul>
     * Start journal (B JT, D JF, E EG, F JM, Q QB) journal entries. Indicates
     * the type of images selected:
     * <ul>
     * <li>0 = After images are journaled.</li>
     * <li>1 = Before and after images are journaled.</li>
     * </ul>
     */
    @Expose(serialize = true, deserialize = true)
    private String flag; // JOFLAG
    @Expose(serialize = true, deserialize = true)
    private BigInteger commitmentCycle; // JOCCID
    @Expose(serialize = true, deserialize = true)
    private String userProfile; // JOUSPF
    @Expose(serialize = true, deserialize = true)
    private String systemName; // JOSYNM
    @Expose(serialize = true, deserialize = true)
    private String journalID; // JOJID
    @Expose(serialize = true, deserialize = true)
    private String referentialConstraint; // JORCST
    @Expose(serialize = true, deserialize = true)
    private String referentialConstraintText;
    @Expose(serialize = true, deserialize = true)
    private String trigger; // JOTGR
    @Expose(serialize = true, deserialize = true)
    private String triggerText;
    @Expose(serialize = true, deserialize = true)
    private String incompleteData; // JOINCDAT
    @Expose(serialize = true, deserialize = true)
    private String incompleteDataText;
    @Expose(serialize = true, deserialize = true)
    private String apyRmvJrnChg; // JOIGNAPY
    @Expose(serialize = true, deserialize = true)
    private String apyRmvJrnChgText;
    @Expose(serialize = true, deserialize = true)
    private String minimizedSpecificData; // JOMINESD
    @Expose(serialize = true, deserialize = true)
    private String minimizedSpecificDataText;
    @Expose(serialize = true, deserialize = true)
    private byte[] specificData; // JOESD
    @Expose(serialize = true, deserialize = true)
    private String stringSpecificData; // JOESD (String)
    @Expose(serialize = true, deserialize = true)
    private String programAspDevice; // JOPGMDEV
    @Expose(serialize = true, deserialize = true)
    private long programAsp; // JOPGMASP
    @Expose(serialize = true, deserialize = true)
    private String objectIndicator; // JOOBJIND
    @Expose(serialize = true, deserialize = true)
    private String objectIndicatorText;
    @Expose(serialize = true, deserialize = true)
    private String systemSequenceNumber; // JOSYSSEQ
    @Expose(serialize = true, deserialize = true)
    private String receiver; // JORCV
    @Expose(serialize = true, deserialize = true)
    private String receiverLibrary; // JORCVLIB
    @Expose(serialize = true, deserialize = true)
    private String receiverAspDevice; // JORCVDEV
    @Expose(serialize = true, deserialize = true)
    private int receiverAsp; // JORCVASP
    @Expose(serialize = true, deserialize = true)
    private int armNumber; // JOARM
    @Expose(serialize = true, deserialize = true)
    private String threadId; // JOTHDX
    @Expose(serialize = true, deserialize = true)
    private String addressFamily; // JOADF
    @Expose(serialize = true, deserialize = true)
    private String addressFamilyText;
    @Expose(serialize = true, deserialize = true)
    private int remotePort; // JORPORT
    @Expose(serialize = true, deserialize = true)
    private String remoteAddress; // JORADR
    @Expose(serialize = true, deserialize = true)
    private String logicalUnitOfWork; // JOLUW
    @Expose(serialize = true, deserialize = true)
    private String transactionIdentifier; // JOXID
    @Expose(serialize = true, deserialize = true)
    private String objectType; // JOOBJTYP
    @Expose(serialize = true, deserialize = true)
    private String fileTypeIndicator; // JOFILTYP
    @Expose(serialize = true, deserialize = true)
    private String fileTypeIndicatorText;
    @Expose(serialize = true, deserialize = true)
    private long nestedCommitLevel; // JOCMTLVL
    @Expose(serialize = true, deserialize = true)
    private byte[] nullIndicators; // JONVI

    // Transient values, set on demand
    private transient OutputFile outputFile;
    private transient String qualifiedObjectName;
    private transient JournaledFile journaledFile;
    private transient String stringSpecificDataForUI;

    // Transient values
    private transient IDatatypeConverterDelegate datatypeConverterDelegate;
    private transient DecimalFormat bin8Formatter;
    private transient DecimalFormat nestedCommitLevelFormatter;
    private transient SimpleDateFormat dateFormatter;
    private transient SimpleDateFormat timeFormatter;
    private transient SimpleDateFormat timestampFormatter;
    private transient Calendar calendar;

    private transient JOESDProperty joesdProperty;
    private transient HashMap<String, Integer> fullColumnMapping;
    private transient JournalProperties journalProperties;

    /**
     * Produces a new JournalEntry object. This constructor is used by the Json
     * importer, when loading journal entries from a Json file.
     */
    public JournalEntry() {
        this(null);
    }

    /**
     * Produces a new JournalEntry. This constructor is used when loading
     * journal entries from a journal or a DSPJRN output file.
     * 
     * @param outputFile
     */
    public JournalEntry(OutputFile outputFile) {

        // Serialised values
        if (outputFile != null) {
            this.connectionName = outputFile.getConnectionName();
            this.outputFileName = outputFile.getFileName();
            this.outputFileLibraryName = outputFile.getLibraryName();
            this.outputFileMemberName = outputFile.getMemberName();
        }

        // Transient values, set on demand
        this.qualifiedObjectName = null;
        this.journaledFile = null;
        this.stringSpecificDataForUI = null;

        // Transient values
        this.datatypeConverterDelegate = new DatatypeConverterDelegate();
        this.bin8Formatter = new DecimalFormat("00000000000000000000");
        this.nestedCommitLevelFormatter = new DecimalFormat("0000000");

        this.dateFormatter = biz.isphere.core.preferences.Preferences.getInstance().getDateFormatter();
        this.timeFormatter = biz.isphere.core.preferences.Preferences.getInstance().getTimeFormatter();
        this.timestampFormatter = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS"); //$NON-NLS-1$
        this.calendar = Calendar.getInstance();

        this.joesdProperty = null;
        this.fullColumnMapping = null;
        this.journalProperties = null;

        // Default values
        setObjectType("*N"); // ==> undefined
    }

    /*
     * Returns the output file or record format (*TYPE1 - *TYPE5) of the journal
     * entry.
     */
    public OutputFile getOutputFile() {
        if (outputFile == null) {
            outputFile = new OutputFile(this.connectionName, this.outputFileLibraryName, this.outputFileName, this.outputFileMemberName);
        }
        return outputFile;
    }

    public boolean hasNullIndicatorTable() throws Exception {
        return MetaDataCache.getInstance().retrieveMetaData(getOutputFile()).hasColumn(ColumnsDAO.JONVI.name());
    }

    public boolean isRecordEntryType() {

        JournalEntryType journalEntryType = JournalEntryType.find(entryType);
        if (journalEntryType != null && journalEntryType.isChildOf(JournalCode.R)) {
            return true;
        }

        return false;
    }

    public static HashMap<String, Integer> getBasicColumnMapping() {
        return basicColumnMappings;
    }

    public static ContentAssistProposal[] getBasicContentAssistProposals() {
        return basicProposals.toArray(new ContentAssistProposal[basicProposals.size()]);
    }

    public ContentAssistProposal[] getContentAssistProposals() {

        List<ContentAssistProposal> fullProposals = new LinkedList<ContentAssistProposal>();

        for (ContentAssistProposal contentAssistProposal : basicProposals) {
            fullProposals.add(contentAssistProposal);
        }

        for (ContentAssistProposal contentAssistProposal : getJOESDProperty().getContentAssistProposals()) {
            fullProposals.add(contentAssistProposal);
        }

        return fullProposals.toArray(new ContentAssistProposal[fullProposals.size()]);
    }

    public static Comparable<?>[] getSampleRow() {

        long now = new java.util.Date().getTime();

        JournalEntry journalEntry = new JournalEntry(null);
        journalEntry.setJournalCode("R"); // JOCODE
        journalEntry.setEntryType("DL"); // JOENTT
        journalEntry.setJobName("TRADDATZA1"); // JOJOB
        journalEntry.setJobUserName("RADDATZ"); // JOUSER
        journalEntry.setJobNumber(939207); // JONBR
        journalEntry.setObjectLibrary("ISPHEREDVP"); // JOLIB;
        journalEntry.setObjectName("TYPES_SQL"); // JOOBJ
        journalEntry.setMemberName("TYPES_SQL"); // JOMBR
        journalEntry.setTimestamp(new java.sql.Timestamp(now)); // JODATE/JOTIME
        journalEntry.setProgramName("CRTTSTDTA"); // JOPGM
        journalEntry.setProgramLibrary("*OMITTED"); // JOPGMLIB
        journalEntry.setObjectType("*QDDS"); // JOOBJTYP
        journalEntry.setFileTypeIndicator(""); // JOFILTYP
        journalEntry.setSystemName("GFD400"); // JOSYNM
        journalEntry.setReceiverName("JRN003"); // JORCV
        journalEntry.setReceiverLibraryName("ISPHEREDVP"); // JORCVLIB
        journalEntry.setUserProfile("RADDATZ"); // JOUSPF
        journalEntry.setSequenceNumber(new BigInteger("4836")); // JOSEQN
        journalEntry.setCommitmentCycle(new BigInteger("472568965")); // JOCCID
        journalEntry.setCountRrn(new BigInteger("324705620")); // JOCTRR

        return journalEntry.getBasicRow();
    }

    public HashMap<String, Integer> getColumnMapping() {

        if (fullColumnMapping == null) {
            try {

                fullColumnMapping = new HashMap<String, Integer>();

                for (Map.Entry<String, Integer> columnMapping : getBasicColumnMapping().entrySet()) {
                    fullColumnMapping.put(columnMapping.getKey(), columnMapping.getValue());
                }

                JournalProperty[] rowProperties = getJOESDProperty().toPropertyArray();
                for (JournalProperty rowProperty : rowProperties) {
                    fullColumnMapping.put(rowProperty.name, fullColumnMapping.size() - 1);
                }

                return fullColumnMapping;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return fullColumnMapping;
    }

    public Comparable<?>[] getRow() {

        Comparable<?>[] row = getBasicRow();

        JournalProperty[] rowProperties = joesdProperty.toPropertyArray();
        row = Arrays.copyOf(row, row.length + rowProperties.length);

        int i = JOCTRR + 1;
        for (int p = 0; p < rowProperties.length; p++) {
            if (rowProperties[p].value instanceof String) {
                row[i] = StringHelper.trimR((String)rowProperties[p].value);
            } else {
                row[i] = (Comparable<?>)rowProperties[p].value;
            }
            i++;
        }
        return row;
    }

    public Comparable<?>[] getBasicRow() {

        Comparable<?>[] row = new Comparable[basicColumnMappings.size()];

        row[JOCODE] = getJournalCode();
        row[JOENTT] = getEntryType();
        row[JOJOB] = getJobName();
        row[JOUSER] = getJobUserName();
        row[JONBR] = getJobNumber();
        row[JOLIB] = getObjectLibrary();
        row[JOOBJ] = getObjectName();
        row[JOMBR] = getMemberName();
        row[JODATE] = new java.sql.Date(getDate().getTime());
        row[JOTIME] = getTime();
        row[JOTSTP] = getTimestamp();
        row[JOPGM] = getProgramName();
        row[JOPGMLIB] = getProgramLibrary();
        row[JOOBJTYP] = getObjectType();
        row[JOFILTYP] = getFileTypeIndicator();
        row[JOSYNM] = getSystemName();
        row[JORCV] = getReceiver();
        row[JORCVLIB] = getReceiverLibrary();
        row[JOUSPF] = getUserProfile();
        row[JOSEQN] = getSequenceNumber();
        row[JOCCID] = getCommitmentCycle();
        row[JOCTRR] = getCountRrn();

        return row;
    }

    // //////////////////////////////////////////////////////////
    // / Getters / Setters
    // //////////////////////////////////////////////////////////

    public String getConnectionName() {
        return connectionName;
    }

    public void overwriteConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public String getKey() {
        return Messages.bind(Messages.Journal_RecordNum, new Object[] { getConnectionName(), getOutFileLibrary(), getOutFileName(), getId() });
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOutFileName() {
        return getOutputFile().getFileName();
    }

    public String getOutFileLibrary() {
        return getOutputFile().getLibraryName();
    }

    public String getOutFileMemberName() {
        return getOutputFile().getMemberName();
    }

    // //////////////////////////////////////////////////////////
    // / Getters / Setters of journal entry
    // //////////////////////////////////////////////////////////

    /**
     * Returns the 'Length of Entry'.
     * <p>
     * Date type in journal output file: ZONED(5 0)
     * 
     * @return value of field 'JOENTL'.
     */
    public int getEntryLength() {
        return entryLength;
    }

    public void setEntryLength(int largoEntrada) {
        this.entryLength = largoEntrada;
    }

    /**
     * Returns the 'Sequence number'.
     * <p>
     * Date type in journal output file: CHAR(20)
     * 
     * @return value of field 'JOSEQN'.
     */
    public BigInteger getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(BigInteger sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Returns the 'Journal Code'.
     * <p>
     * Date type in journal output file: CHAR(1)
     * 
     * @return value of field 'JOCODE'.
     */
    public String getJournalCode() {
        return journalCode;
    }

    public void setJournalCode(String journalCode) {
        this.journalCode = journalCode.trim();
    }

    /**
     * Returns the 'Entry Type'.
     * <p>
     * Date type in journal output file: CHAR(2)
     * 
     * @return value of field 'JOENTT'.
     */
    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType.trim();
    }

    /**
     * Returns the time portion of field 'Timestamp of Entry' or 'Date of
     * entry', depending on the type of the journal output file. That has been
     * changed with output file type *TYPE3.
     * <p>
     * Date type in journal output file: TIMESTAMP(26) / CHAR(6)
     * 
     * @return value of field 'JOTSTP' or 'JODATE'.
     */
    public java.sql.Date getDate() {
        return date;
    }

    private void setDate(java.sql.Timestamp timestamp) {

        calendar.clear();
        calendar.setTime(timestamp);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        this.date = new java.sql.Date(calendar.getTimeInMillis());
    }

    public void setDateAndTime(String date, int time, int dateFormat, Character dateSeparator, Character timeSeparator) {
        setTimestamp(JournalEntryDelegate.getDate(date, dateFormat, dateSeparator), JournalEntryDelegate.getTime(time, timeSeparator));
    }

    /**
     * Returns the time portion of field 'Timestamp of Entry' or 'Time of
     * entry', depending on the type of the journal output file. That has been
     * changed with output file type *TYPE3.
     * <p>
     * Date type in journal output file: TIMESTAMP(26) / ZONED(6 0)
     * 
     * @return value of field 'JOTSTP' or 'JOTIME'.
     */
    public java.sql.Time getTime() {
        return time;
    }

    private void setTime(java.sql.Timestamp timestamp) {

        calendar.clear();
        calendar.setTime(timestamp);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.YEAR, 1970);

        this.time = new java.sql.Time(calendar.getTimeInMillis());
    }

    public void setTimestamp(java.sql.Date date, java.sql.Time time) {

        calendar.clear();
        calendar.setTime(time);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int milliseconds = calendar.get(Calendar.MILLISECOND);

        calendar.clear();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, milliseconds);

        setTimestamp(new java.sql.Timestamp(calendar.getTimeInMillis()));
    }

    public void setTimestamp(java.sql.Timestamp timestamp) {

        this.timestamp = timestamp;

        setDate(timestamp);
        setTime(timestamp);
    }

    /**
     * Returns the 'Name of Job'.
     * <p>
     * Date type in journal output file: CHAR(10)
     * 
     * @return value of field 'JOJOB'.
     */
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName.trim();
    }

    /**
     * Returns the 'Name of User'.
     * <p>
     * Date type in journal output file: CHAR(10)
     * 
     * @return value of field 'JOUSER'.
     */
    public String getJobUserName() {
        return jobUserName;
    }

    public void setJobUserName(String userName) {
        this.jobUserName = userName.trim();
    }

    /**
     * Returns the 'Job Number'.
     * <p>
     * Date type in journal output file: ZONED(6 0)
     * 
     * @return value of field 'JONBR'.
     */
    public int getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(int jobNumber) {
        this.jobNumber = jobNumber;
    }

    public void setJobNumber(String jobNumber) {
        this.jobNumber = IntHelper.tryParseInt(jobNumber, -1);
    }

    /**
     * Returns the 'Name of Program'.
     * <p>
     * Date type in journal output file: CHAR(10)
     * 
     * @return value of field 'JOPGM'.
     */
    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName.trim();
    }

    /**
     * Returns the 'Program Library'.
     * <p>
     * Date type in journal output file: CHAR(10)
     * 
     * @return value of field 'JOPGMLIB'.
     * @since *TYPE5
     */
    public String getProgramLibrary() {
        return programLibrary;
    }

    public void setProgramLibrary(String programLibrary) {
        this.programLibrary = programLibrary.trim();
    }

    /**
     * Returns the 'Program ASP Device'.
     * <p>
     * Date type in journal output file: CHAR(10)
     * 
     * @return value of field 'JOPGMDEV'.
     * @since *TYPE5
     */
    public String getProgramAspDevice() {
        return programAspDevice;
    }

    public void setProgramLibraryAspDeviceName(String programAspDevice) {
        this.programAspDevice = programAspDevice.trim();
    }

    /**
     * Returns the 'Program ASP'.
     * <p>
     * Date type in journal output file: ZONED(5 0)
     * 
     * @return value of field 'JOPGMASP'.
     * @since *TYPE5
     */
    public long getProgramAsp() {
        return programAsp;
    }

    public void setProgramLibraryAspNumber(long programAsp) {
        this.programAsp = programAsp;
    }

    /**
     * Returns the 'Name of Object'.
     * <p>
     * Date type in journal output file: CHAR(10)
     * 
     * @return value of field 'JOOBJ'.
     */
    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName.trim();
        this.qualifiedObjectName = null;
    }

    /**
     * Returns the 'Object Library'.
     * <p>
     * Date type in journal output file: CHAR(10)
     * 
     * @return value of field 'JOLIB'.
     */
    public String getObjectLibrary() {
        return objectLibrary;
    }

    public void setObjectLibrary(String objectLibrary) {
        this.objectLibrary = objectLibrary.trim();
        this.qualifiedObjectName = null;
    }

    /**
     * Returns the 'Name of Member'.
     * <p>
     * Date type in journal output file: CHAR(10)
     * 
     * @return value of field 'JOMBR'.
     */
    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = getValueChecked(memberName);
        this.qualifiedObjectName = null;
    }

    /**
     * Returns the 'Count or relative record number changed'.
     * <p>
     * Date type in journal output file: CHAR(20)
     * 
     * @return value of field 'JOCTRR'.
     */
    public BigInteger getCountRrn() {
        return countRrn;
    }

    public void setCountRrn(BigInteger countRrn) {
        this.countRrn = countRrn;
    }

    /**
     * Returns the 'Flag'.
     * <p>
     * Date type in journal output file: CHAR(1)
     * 
     * @return value of field 'JOFLAG'.
     */
    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag.trim();
    }

    /**
     * Returns the 'Commit cycle identifier'.
     * <p>
     * Date type in journal output file: CHAR(20)
     * 
     * @return value of field 'JOCCID'.
     */
    public BigInteger getCommitmentCycle() {
        return commitmentCycle;
    }

    public void setCommitmentCycle(BigInteger commitmentCycle) {
        this.commitmentCycle = commitmentCycle;
    }

    /**
     * Returns the 'User Profile'.
     * <p>
     * Date type in journal output file: CHAR(10)
     * 
     * @return value of field 'JOUSPF'.
     * @since *TYPE2
     */
    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile.trim();
    }

    /**
     * Returns the 'System Name'.
     * <p>
     * Date type in journal output file: CHAR(8)
     * 
     * @return value of field 'JOSYNM'.
     * @since *TYPE2
     */
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName.trim();
    }

    /**
     * Returns the 'Journal Identifier'.
     * <p>
     * Date type in journal output file: CHAR(10)
     * 
     * @return value of field 'JOJID'.
     * @since *TYPE4
     */
    public String getJournalID() {
        return journalID;
    }

    public void setJournalID(String journalID) {
        this.journalID = journalID.trim();
    }

    /**
     * Returns the 'Referential Constraint'.
     * <p>
     * Date type in journal output file: CHAR(1)
     * 
     * @return value of field 'JORCST'.
     * @since *TYPE4
     */
    public String getReferentialConstraint() {
        return referentialConstraint;
    }

    public String getReferentialConstraintText() {
        if (referentialConstraintText == null) {
            if ("0".equals(referentialConstraint)) {
                referentialConstraintText = "no";
            } else if ("1".equals(referentialConstraint)) {
                referentialConstraintText = "yes";
            } else {
                referentialConstraintText = referentialConstraint;
            }
        }

        return referentialConstraintText;
    }

    public void setReferentialConstraint(boolean isReferentialConstraint) {
        setReferentialConstraint(toString(isReferentialConstraint));
    }

    public void setReferentialConstraint(String referentialConstraint) {
        this.referentialConstraint = referentialConstraint.trim();
        this.referentialConstraintText = null;
    }

    /**
     * Returns the 'Trigger'.
     * <p>
     * Date type in journal output file: CHAR(1)
     * 
     * @return value of field 'JOTGR'.
     * @since *TYPE4
     */
    public String getTrigger() {
        return trigger;
    }

    public String getTriggerText() {
        if (triggerText == null) {
            if ("0".equals(trigger)) {
                triggerText = "no";
            } else if ("1".equals(trigger)) {
                triggerText = "yes";
            } else {
                triggerText = trigger;
            }
        }

        return triggerText;
    }

    public void setTrigger(boolean isTrigger) {
        setTrigger(toString(isTrigger));
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger.trim();
        this.triggerText = null;
    }

    /**
     * Returns the 'Incomplete Data'.
     * <p>
     * Date type in journal output file: CHAR(1)
     * 
     * @return value of field 'JOINCDAT'.
     */
    public String getIncompleteData() {
        return incompleteData;
    }

    public String getIncompleteDataText() {
        if (incompleteDataText == null) {
            if ("0".equals(incompleteData)) {
                incompleteDataText = "no";
            } else if ("1".equals(incompleteData)) {
                incompleteDataText = "yes";
            } else {
                incompleteDataText = incompleteData;
            }
        }

        return incompleteDataText;
    }

    public void setIncompleteData(boolean isIncompleteData) {
        setIncompleteData(toString(isIncompleteData));
    }

    public void setIncompleteData(String incompleteData) {
        this.incompleteData = incompleteData.trim();
        this.incompleteDataText = null;
    }

    /**
     * Returns the 'Ignored by APY/RMVJRNCHG'.
     * <p>
     * Date type in journal output file: CHAR(1)
     * 
     * @return value of field 'JOIGNAPY'.
     * @since *TYPE4
     */
    public String getIgnoredByApyRmvJrnChg() {
        return apyRmvJrnChg;
    }

    public String getIgnoredByApyRmvJrnChgText() {
        if (apyRmvJrnChgText == null) {
            if ("0".equals(apyRmvJrnChg)) {
                apyRmvJrnChgText = "no";
            } else if ("1".equals(apyRmvJrnChg)) {
                apyRmvJrnChgText = "yes";
            } else {
                apyRmvJrnChgText = apyRmvJrnChg;
            }
        }

        return apyRmvJrnChgText;
    }

    public void setIgnoredByApyRmvJrnChg(boolean isApyRmvJrnChg) {
        setIgnoredByApyRmvJrnChg(toString(isApyRmvJrnChg));
    }

    public void setIgnoredByApyRmvJrnChg(String apyRmvJrnChg) {
        this.apyRmvJrnChg = apyRmvJrnChg.trim();
        this.apyRmvJrnChgText = null;
    }

    /**
     * Returns the 'Minimized Entry Specific Data'.
     * <p>
     * Date type in journal output file: CHAR(1)
     * 
     * @return value of field 'JOMINESD'.
     */
    public String getMinimizedSpecificData() {
        return minimizedSpecificData;
    }

    public String getMinimizedSpecificDataText() {
        if (minimizedSpecificDataText == null) {
            if ("0".equals(minimizedSpecificData)) {
                minimizedSpecificDataText = "no";
            } else if ("1".equals(minimizedSpecificData)) {
                minimizedSpecificDataText = "minimized";
            } else if ("2".equals(minimizedSpecificData)) {
                minimizedSpecificDataText = "field boundaries";
            } else {
                minimizedSpecificDataText = minimizedSpecificData;
            }
        }

        return minimizedSpecificDataText;
    }

    public void setMinimizedSpecificData(String minimizedSpecificData) {
        this.minimizedSpecificData = minimizedSpecificData.trim();
        this.minimizedSpecificDataText = null;
    }

    public void setMinimizedSpecificData(boolean minimizedSpecificData) {
        if (minimizedSpecificData) {
            setMinimizedSpecificData("1");
        } else {
            setMinimizedSpecificData("0");
        }
    }

    /**
     * Returns the 'Object Name Indicator'.
     * <p>
     * Date type in journal output file: CHAR(1)
     * <p>
     * Either the journal entry has no object information or the object
     * information in the journal entry header does not necessarily reflect the
     * name of the object at the time the journal entry was deposited into the
     * journal.<br>
     * <b>Note:</b> This value is returned only when retrieving journal entries
     * from a journal receiver that was attached to a journal prior to V4R2M0.
     * 
     * @return value of field 'JOOBJIND'.
     * @since *TYPE5
     */
    public String getObjectNameIndicator() {
        return objectIndicator;
    }

    public String getObjectIndicatorText() {
        if (objectIndicatorText == null) {
            if ("0".equals(objectIndicator)) {
                objectIndicatorText = "-/-";
            } else if ("1".equals(objectIndicator)) {
                objectIndicatorText = "accurate";
            } else if ("2".equals(objectIndicator)) {
                objectIndicatorText = "uncertain";
            } else {
                objectIndicatorText = objectIndicator;
            }
        }

        return objectIndicatorText;
    }

    public void setObjectNameIndicator(String objectIndicator) {
        this.objectIndicator = objectIndicator.trim();
        this.objectIndicatorText = null;
    }

    /**
     * Returns the 'System Sequence Number'.
     * <p>
     * Date type in journal output file: CHAR(20)
     * 
     * @return value of field 'JOSYSSEQ'.
     * @since *TYPE5
     */
    public String getSystemSequenceNumber() {
        return systemSequenceNumber;
    }

    public void setSystemSequenceNumber(BigInteger systemSequenceNumber) {
        String tSystemSequenceNumber = bin8Formatter.format(systemSequenceNumber);
        this.systemSequenceNumber = tSystemSequenceNumber;
    }

    public void setSystemSequenceNumber(String systemSequenceNumber) {
        this.systemSequenceNumber = systemSequenceNumber.trim();
    }

    /**
     * Returns the 'Receiver'.
     * <p>
     * Date type in journal output file: CHAR(10)
     * 
     * @return value of field 'JORCV'.
     * @since *TYPE5
     */
    public String getReceiver() {
        return receiver;
    }

    public void setReceiverName(String receiver) {
        this.receiver = receiver.trim();
    }

    /**
     * Returns the 'Receiver Library'.
     * <p>
     * Date type in journal output file: CHAR(10)
     * 
     * @return value of field 'JORCVLIB'.
     * @since *TYPE5
     */
    public String getReceiverLibrary() {
        return receiverLibrary;
    }

    public void setReceiverLibraryName(String receiverLibrary) {
        this.receiverLibrary = receiverLibrary.trim();
    }

    /**
     * Returns the 'Receiver ASP Device'.
     * <p>
     * Date type in journal output file: CHAR(10)
     * 
     * @return value of field 'JORCVDEV'.
     * @since *TYPE5
     */
    public String getReceiverAspDevice() {
        return receiverAspDevice;
    }

    public void setReceiverLibraryASPDeviceName(String receiverAspDevice) {
        this.receiverAspDevice = receiverAspDevice.trim();
    }

    /**
     * Returns the 'Receiver ASP'.
     * <p>
     * Date type in journal output file: ZONED(5 0)
     * 
     * @return value of field 'JORCVASP'.
     * @since *TYPE5
     */
    public int getReceiverAsp() {
        return receiverAsp;
    }

    public void setReceiverLibraryASPNumber(int receiverAsp) {
        this.receiverAsp = receiverAsp;
    }

    /**
     * Returns the 'ARM Number'.
     * <p>
     * Date type in journal output file: ZONED(5 0)
     * 
     * @return value of field 'JOARM'.
     * @since *TYPE5
     */
    public int getArmNumber() {
        return armNumber;
    }

    public void setArmNumber(int armNumber) {
        this.armNumber = armNumber;
    }

    /**
     * Returns the 'Thread ID Hex'.
     * <p>
     * Date type in journal output file: CHAR(16)
     * 
     * @return value of field 'JOTHDX'.
     * @since *TYPE5
     */
    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId.trim();
    }

    /**
     * Returns the 'Address Family'.
     * <p>
     * Date type in journal output file: CHAR(1)
     * 
     * @return value of field 'JOADF'.
     * @since *TYPE5
     */
    public String getAddressFamily() {
        return addressFamily;
    }

    public String getAddressFamilyText() {
        if (addressFamilyText == null) {
            if (ADDRESS_FAMILY_UNKNOWN.equals(addressFamily)) {
                addressFamilyText = "";
            } else if (ADDRESS_FAMILY_IPV4.equals(addressFamily)) {
                addressFamilyText = "IPv4";
            } else if (ADDRESS_FAMILY_IPV6.equals(addressFamily)) {
                addressFamilyText = "IPv6";
            } else {
                addressFamilyText = addressFamily;
            }
        }

        return addressFamilyText;
    }

    public void setAddressFamily(String addressFamily) {
        this.addressFamily = addressFamily.trim();
        this.addressFamilyText = null;
    }

    /**
     * Returns the 'Remote Port'.
     * <p>
     * Date type in journal output file: ZONED(5 0)
     * 
     * @return value of field 'JORPORT'.
     * @since *TYPE5
     */
    public int getRemotePort() {
        return remotePort;
    }

    public String getRemotePortText() {
        if (remotePort > 0) {
            return Integer.toString(remotePort);
        } else {
            return "";
        }
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    /**
     * Returns the 'Remote Address'.
     * <p>
     * Date type in journal output file: CHAR(46)
     * 
     * @return value of field 'JORADR'.
     * @since *TYPE5
     */
    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress.trim();
    }

    /**
     * Returns the 'Logical Unit of Work'.
     * <p>
     * Date type in journal output file: CHAR(39)
     * 
     * @return value of field 'JOLUW'.
     * @since *TYPE5
     */
    public String getLogicalUnitOfWork() {
        return logicalUnitOfWork;
    }

    public void setLogicalUnitOfWork(String logicalUnitOfWork) {
        this.logicalUnitOfWork = logicalUnitOfWork.trim();
    }

    /**
     * Returns the 'Transaction ID'.
     * <p>
     * Date type in journal output file: CHAR(140)
     * 
     * @return value of field 'JOXID'.
     * @since *TYPE5
     */
    public String getTransactionIdentifier() {
        return transactionIdentifier;
    }

    public void setTransactionIdentifier(String transactionIdentifier) {
        this.transactionIdentifier = transactionIdentifier.trim();
    }

    /**
     * Returns the 'Object Type'.
     * <p>
     * Date type in journal output file: CHAR(7)
     * 
     * @return value of field 'JOOBJTYP'.
     * @since *TYPE5
     */
    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType.trim();
    }

    /**
     * Returns the 'File type indicator' that indicates the type of object
     * associated with this entry. ('0' is physical, '1' is logical)
     * <p>
     * Date type in journal output file: CHAR(1)
     * <p>
     * The possible values are:
     * <ul>
     * <li>0 - This entry is not associated with a logical file.</li>
     * <li>1 - This entry is associated with a logical file.</li>
     * </ul>
     * 
     * @return value of field 'JOFILTYP'.
     * @since *TYPE5
     */
    public String getFileTypeIndicator() {
        return fileTypeIndicator;
    }

    public String getFileTypeIndicatorText() {
        if (fileTypeIndicatorText == null) {
            if ("0".equals(fileTypeIndicator)) {
                fileTypeIndicatorText = "PF";
            } else if ("1".equals(fileTypeIndicator)) {
                fileTypeIndicatorText = "LF";
            } else {
                fileTypeIndicatorText = fileTypeIndicator;
            }
        }

        return fileTypeIndicatorText;
    }

    public void setFileTypeIndicator(String fileTypeIndicator) {
        this.fileTypeIndicator = fileTypeIndicator.trim();
        this.fileTypeIndicatorText = null;
    }

    /**
     * Returns the 'Nested Commit Level'.
     * <p>
     * Date type in journal output file: CHAR(7)
     * 
     * @return value of field 'JOCMTLVL'.
     * @since *TYPE5
     */
    public long getNestedCommitLevel() {
        return nestedCommitLevel;
    }

    public void setNestedCommitLevel(long nestedCommitLevel) {
        this.nestedCommitLevel = nestedCommitLevel;
    }

    public int getNullTableLength() {

        if (nullIndicators == null) {
            return 0;
        }

        return nullIndicators.length;
    }

    public boolean isNull(int index) {

        if (nullIndicators == null) {
            return false;
        }

        if (index >= nullIndicators.length) {
            return false;
        }

        return nullIndicators[index] == '1';
    }

    public String getNullIndicators() {
        if (nullIndicators != null) {
            return new String(nullIndicators);
        }
        return "";
    }

    public void setNullIndicators(byte[] nullIndicators) {
        this.nullIndicators = nullIndicators;
    }

    /**
     * Returns the string representation of field 'Entry Specific Data'.
     * 
     * @return value of field 'JOESD'.
     */
    public String getStringSpecificData() {
        return stringSpecificData;
    }

    /**
     * Returns the 'Entry Specific Data'.
     * 
     * @return value of field 'JOESD'.
     */
    public int getSpecificDataLength() {
        return specificData.length;
    }

    public byte[] getSpecificData(int recordLength) {

        if (recordLength > specificData.length) {
            byte[] recordData = new byte[recordLength];
            System.arraycopy(specificData, 0, recordData, 0, specificData.length);
            return recordData;
        }

        return specificData;
    }

    public void setStringSpecificData(byte[] specificData) {

        AS400Text text = new AS400Text(specificData.length, Preferences.getInstance().getJournalEntryCcsid());
        this.stringSpecificData = StringHelper.trimR((String)text.toObject(specificData));
    }

    public void setStringSpecificData(String specificData) {

        byte[] bytes = datatypeConverterDelegate.parseHexBinary(specificData);
        setStringSpecificData(bytes);
    }

    public void setSpecificData(byte[] specificData) {
        this.specificData = specificData;
    }

    public String getValueForUi(String name) {

        String data = "?"; //$NON-NLS-1$

        if (ColumnsDAO.ID.name().equals(name)) {
            return Integer.toString(getId()).trim();
        } else if (ColumnsDAO.JOENTL.name().equals(name)) {
            return Integer.toString(getEntryLength());
        } else if (ColumnsDAO.JOSEQN.name().equals(name)) {
            return toString(getSequenceNumber());
        } else if (ColumnsDAO.JOCODE.name().equals(name)) {
            return getJournalCode();
        } else if (ColumnsDAO.JOENTT.name().equals(name)) {
            return getEntryType();
        } else if (ColumnsDAO.JOTSTP.name().equals(name)) {
            java.sql.Timestamp timestamp = getTimestamp();
            if (timestamp == null) {
                return ""; //$NON-NLS-1$
            }
            return timestampFormatter.format(timestamp);
        } else if (ColumnsDAO.JODATE.name().equals(name)) {
            java.sql.Date date = getDate();
            if (date == null) {
                return ""; //$NON-NLS-1$
            }
            return dateFormatter.format(date);
        } else if (ColumnsDAO.JOTIME.name().equals(name)) {
            java.sql.Time time = getTime();
            if (time == null) {
                return ""; //$NON-NLS-1$
            }
            return timeFormatter.format(time);
        } else if (ColumnsDAO.JOJOB.name().equals(name)) {
            return getJobName();
        } else if (ColumnsDAO.JOUSER.name().equals(name)) {
            return getJobUserName();
        } else if (ColumnsDAO.JONBR.name().equals(name)) {
            return Integer.toString(getJobNumber());
        } else if (ColumnsDAO.JOPGM.name().equals(name)) {
            return getProgramName();
        } else if (ColumnsDAO.JOPGMLIB.name().equals(name)) {
            return getProgramLibrary();
        } else if (ColumnsDAO.JOPGMDEV.name().equals(name)) {
            return getProgramAspDevice();
        } else if (ColumnsDAO.JOPGMASP.name().equals(name)) {
            return Long.toString(getProgramAsp());
        } else if (ColumnsDAO.JOOBJ.name().equals(name)) {
            return getObjectName();
        } else if (ColumnsDAO.JOLIB.name().equals(name)) {
            return getObjectLibrary();
        } else if (ColumnsDAO.JOMBR.name().equals(name)) {
            return getMemberName();
        } else if (ColumnsDAO.JOCTRR.name().equals(name)) {
            return toString(getCountRrn());
        } else if (ColumnsDAO.JOFLAG.name().equals(name)) {
            return getFlag();
        } else if (ColumnsDAO.JOCCID.name().equals(name)) {
            return toString(getCommitmentCycle());
        } else if (ColumnsDAO.JOUSPF.name().equals(name)) {
            return getUserProfile();
        } else if (ColumnsDAO.JOSYNM.name().equals(name)) {
            return getSystemName();
        } else if (ColumnsDAO.JOJID.name().equals(name)) {
            return getJournalID();
        } else if (ColumnsDAO.JORCST.name().equals(name)) {
            return getReferentialConstraintText();
        } else if (ColumnsDAO.JOTGR.name().equals(name)) {
            return getTriggerText();
        } else if (ColumnsDAO.JOINCDAT.name().equals(name)) {
            return getIncompleteDataText();
        } else if (ColumnsDAO.JOIGNAPY.name().equals(name)) {
            return getIgnoredByApyRmvJrnChgText();
        } else if (ColumnsDAO.JOMINESD.name().equals(name)) {
            return getMinimizedSpecificDataText();
        } else if (ColumnsDAO.JOOBJIND.name().equals(name)) {
            return getObjectIndicatorText();
        } else if (ColumnsDAO.JOSYSSEQ.name().equals(name)) {
            return getSystemSequenceNumber();
        } else if (ColumnsDAO.JORCV.name().equals(name)) {
            return getReceiver();
        } else if (ColumnsDAO.JORCVLIB.name().equals(name)) {
            return getReceiverLibrary();
        } else if (ColumnsDAO.JORCVDEV.name().equals(name)) {
            return getReceiverAspDevice();
        } else if (ColumnsDAO.JORCVASP.name().equals(name)) {
            return Integer.toString(getReceiverAsp());
        } else if (ColumnsDAO.JOARM.name().equals(name)) {
            return Integer.toString(getArmNumber());
        } else if (ColumnsDAO.JOTHDX.name().equals(name)) {
            return getThreadId();
        } else if (ColumnsDAO.JOADF.name().equals(name)) {
            return getAddressFamilyText();
        } else if (ColumnsDAO.JORPORT.name().equals(name)) {
            return getRemotePortText();
        } else if (ColumnsDAO.JORADR.name().equals(name)) {
            return getRemoteAddress();
        } else if (ColumnsDAO.JOLUW.name().equals(name)) {
            return getLogicalUnitOfWork();
        } else if (ColumnsDAO.JOXID.name().equals(name)) {
            return getTransactionIdentifier();
        } else if (ColumnsDAO.JOOBJTYP.name().equals(name)) {
            return getObjectType();
        } else if (ColumnsDAO.JOFILTYP.name().equals(name)) {
            return getFileTypeIndicatorText();
        } else if (ColumnsDAO.JOCMTLVL.name().equals(name)) {
            return toStringNestedCommitLevel(getNestedCommitLevel());
        } else if (ColumnsDAO.JONVI.name().equals(name)) {
            return getNullIndicators();
        } else if (ColumnsDAO.JOESD.name().equals(name)) {
            if (stringSpecificDataForUI == null) {
                stringSpecificDataForUI = getStringSpecificData();
                if (stringSpecificDataForUI == null) {
                    return "";
                }

                // For displaying purposes, replace 0x00 with blanks.
                // Otherwise, the string will be truncate by JFace.
                if (stringSpecificDataForUI.lastIndexOf('\0') >= 0) {
                    stringSpecificDataForUI = stringSpecificDataForUI.replace('\0', ' ');
                }

                // Display only the first 250 bytes.
                if (stringSpecificDataForUI.length() > 250) {
                    stringSpecificDataForUI = stringSpecificDataForUI.substring(0, 250) + "..."; //$NON-NLS-1$
                }
            }
            return stringSpecificDataForUI;
        }

        return data;
    }

    private String toString(BigInteger unsignedBin8Value) {
        return bin8Formatter.format(unsignedBin8Value);
    }

    private String toStringNestedCommitLevel(long longValue) {
        return nestedCommitLevelFormatter.format(longValue);
    }

    public synchronized String getQualifiedObjectName() {

        if (qualifiedObjectName == null) {

            if (!StringHelper.isNullOrEmpty(objectLibrary) && !StringHelper.isNullOrEmpty(objectName)) {
                if (!StringHelper.isNullOrEmpty(memberName)) {
                    qualifiedObjectName = String.format("%s/%s(%s)", objectLibrary, objectName, memberName);
                } else {
                    qualifiedObjectName = String.format("%s/%s", objectLibrary, objectName);
                }
            }

        }

        return qualifiedObjectName;
    }

    private String getValueChecked(String value) {
        if (value != null) {
            return value.trim();
        }
        return "";
    }

    private java.sql.Timestamp getTimestamp() {
        return timestamp;
    }

    private String toString(boolean isTrue) {

        if (isTrue) {
            return "1";
        } else {
            return "0";
        }
    }

    public void setJoesdProperty(JOESDProperty joesdProperty) {
        this.joesdProperty = joesdProperty;
    }

    @Override
    public String toString() {
        return getQualifiedObjectName() + " (" + getEntryType() + ")";
    }

    public JournalProperties getJournalProperties() {

        if (journalProperties == null) {
            this.journalProperties = new JournalProperties(this);
        }

        return journalProperties;
    }

    public void setJournalProperties(JournalProperties journalProperties) {
        this.journalProperties = journalProperties;
    }

    public JOESDProperty getJOESDProperty() {
        return getJournalProperties().getJOESDProperty();
    }

    public boolean isFile() {

        if (StringHelper.isNullOrEmpty(memberName)) {
            return false;
        } else {
            return true;
        }
    }

    public JournaledFile getJournaledFile() {

        if (journaledFile == null) {
            journaledFile = new JournaledFile(connectionName, objectLibrary, objectName, memberName);
        }

        return journaledFile;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((addressFamily == null) ? 0 : addressFamily.hashCode());
        result = prime * result + ((addressFamilyText == null) ? 0 : addressFamilyText.hashCode());
        result = prime * result + ((apyRmvJrnChg == null) ? 0 : apyRmvJrnChg.hashCode());
        result = prime * result + ((apyRmvJrnChgText == null) ? 0 : apyRmvJrnChgText.hashCode());
        result = prime * result + armNumber;
        result = prime * result + ((commitmentCycle == null) ? 0 : commitmentCycle.hashCode());
        result = prime * result + ((connectionName == null) ? 0 : connectionName.hashCode());
        result = prime * result + ((countRrn == null) ? 0 : countRrn.hashCode());
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + entryLength;
        result = prime * result + ((entryType == null) ? 0 : entryType.hashCode());
        result = prime * result + ((fileTypeIndicator == null) ? 0 : fileTypeIndicator.hashCode());
        result = prime * result + ((fileTypeIndicatorText == null) ? 0 : fileTypeIndicatorText.hashCode());
        result = prime * result + ((flag == null) ? 0 : flag.hashCode());
        result = prime * result + id;
        result = prime * result + ((incompleteData == null) ? 0 : incompleteData.hashCode());
        result = prime * result + ((incompleteDataText == null) ? 0 : incompleteDataText.hashCode());
        result = prime * result + ((jobName == null) ? 0 : jobName.hashCode());
        result = prime * result + jobNumber;
        result = prime * result + ((jobUserName == null) ? 0 : jobUserName.hashCode());
        result = prime * result + ((journalCode == null) ? 0 : journalCode.hashCode());
        result = prime * result + ((journalID == null) ? 0 : journalID.hashCode());
        result = prime * result + ((logicalUnitOfWork == null) ? 0 : logicalUnitOfWork.hashCode());
        result = prime * result + ((memberName == null) ? 0 : memberName.hashCode());
        result = prime * result + ((minimizedSpecificData == null) ? 0 : minimizedSpecificData.hashCode());
        result = prime * result + ((minimizedSpecificDataText == null) ? 0 : minimizedSpecificDataText.hashCode());
        result = prime * result + (int)(nestedCommitLevel ^ (nestedCommitLevel >>> 32));
        result = prime * result + Arrays.hashCode(nullIndicators);
        result = prime * result + ((objectIndicator == null) ? 0 : objectIndicator.hashCode());
        result = prime * result + ((objectIndicatorText == null) ? 0 : objectIndicatorText.hashCode());
        result = prime * result + ((objectLibrary == null) ? 0 : objectLibrary.hashCode());
        result = prime * result + ((objectName == null) ? 0 : objectName.hashCode());
        result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
        result = prime * result + ((outputFileLibraryName == null) ? 0 : outputFileLibraryName.hashCode());
        result = prime * result + ((outputFileMemberName == null) ? 0 : outputFileMemberName.hashCode());
        result = prime * result + ((outputFileName == null) ? 0 : outputFileName.hashCode());
        result = prime * result + (int)(programAsp ^ (programAsp >>> 32));
        result = prime * result + ((programAspDevice == null) ? 0 : programAspDevice.hashCode());
        result = prime * result + ((programLibrary == null) ? 0 : programLibrary.hashCode());
        result = prime * result + ((programName == null) ? 0 : programName.hashCode());
        result = prime * result + ((receiver == null) ? 0 : receiver.hashCode());
        result = prime * result + receiverAsp;
        result = prime * result + ((receiverAspDevice == null) ? 0 : receiverAspDevice.hashCode());
        result = prime * result + ((receiverLibrary == null) ? 0 : receiverLibrary.hashCode());
        result = prime * result + ((referentialConstraint == null) ? 0 : referentialConstraint.hashCode());
        result = prime * result + ((referentialConstraintText == null) ? 0 : referentialConstraintText.hashCode());
        result = prime * result + ((remoteAddress == null) ? 0 : remoteAddress.hashCode());
        result = prime * result + remotePort;
        result = prime * result + ((sequenceNumber == null) ? 0 : sequenceNumber.hashCode());
        result = prime * result + Arrays.hashCode(specificData);
        result = prime * result + ((stringSpecificData == null) ? 0 : stringSpecificData.hashCode());
        result = prime * result + ((systemName == null) ? 0 : systemName.hashCode());
        result = prime * result + ((systemSequenceNumber == null) ? 0 : systemSequenceNumber.hashCode());
        result = prime * result + ((threadId == null) ? 0 : threadId.hashCode());
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        result = prime * result + ((transactionIdentifier == null) ? 0 : transactionIdentifier.hashCode());
        result = prime * result + ((trigger == null) ? 0 : trigger.hashCode());
        result = prime * result + ((triggerText == null) ? 0 : triggerText.hashCode());
        result = prime * result + ((userProfile == null) ? 0 : userProfile.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        JournalEntry other = (JournalEntry)obj;
        if (addressFamily == null) {
            if (other.addressFamily != null) return false;
        } else if (!addressFamily.equals(other.addressFamily)) return false;
        if (addressFamilyText == null) {
            if (other.addressFamilyText != null) return false;
        } else if (!addressFamilyText.equals(other.addressFamilyText)) return false;
        if (apyRmvJrnChg == null) {
            if (other.apyRmvJrnChg != null) return false;
        } else if (!apyRmvJrnChg.equals(other.apyRmvJrnChg)) return false;
        if (apyRmvJrnChgText == null) {
            if (other.apyRmvJrnChgText != null) return false;
        } else if (!apyRmvJrnChgText.equals(other.apyRmvJrnChgText)) return false;
        if (armNumber != other.armNumber) return false;
        if (commitmentCycle == null) {
            if (other.commitmentCycle != null) return false;
        } else if (!commitmentCycle.equals(other.commitmentCycle)) return false;
        if (connectionName == null) {
            if (other.connectionName != null) return false;
        } else if (!connectionName.equals(other.connectionName)) return false;
        if (countRrn == null) {
            if (other.countRrn != null) return false;
        } else if (!countRrn.equals(other.countRrn)) return false;
        if (date == null) {
            if (other.date != null) return false;
        } else if (!date.equals(other.date)) return false;
        if (entryLength != other.entryLength) return false;
        if (entryType == null) {
            if (other.entryType != null) return false;
        } else if (!entryType.equals(other.entryType)) return false;
        if (fileTypeIndicator == null) {
            if (other.fileTypeIndicator != null) return false;
        } else if (!fileTypeIndicator.equals(other.fileTypeIndicator)) return false;
        if (fileTypeIndicatorText == null) {
            if (other.fileTypeIndicatorText != null) return false;
        } else if (!fileTypeIndicatorText.equals(other.fileTypeIndicatorText)) return false;
        if (flag == null) {
            if (other.flag != null) return false;
        } else if (!flag.equals(other.flag)) return false;
        if (id != other.id) return false;
        if (incompleteData == null) {
            if (other.incompleteData != null) return false;
        } else if (!incompleteData.equals(other.incompleteData)) return false;
        if (incompleteDataText == null) {
            if (other.incompleteDataText != null) return false;
        } else if (!incompleteDataText.equals(other.incompleteDataText)) return false;
        if (jobName == null) {
            if (other.jobName != null) return false;
        } else if (!jobName.equals(other.jobName)) return false;
        if (jobNumber != other.jobNumber) return false;
        if (jobUserName == null) {
            if (other.jobUserName != null) return false;
        } else if (!jobUserName.equals(other.jobUserName)) return false;
        if (journalCode == null) {
            if (other.journalCode != null) return false;
        } else if (!journalCode.equals(other.journalCode)) return false;
        if (journalID == null) {
            if (other.journalID != null) return false;
        } else if (!journalID.equals(other.journalID)) return false;
        if (logicalUnitOfWork == null) {
            if (other.logicalUnitOfWork != null) return false;
        } else if (!logicalUnitOfWork.equals(other.logicalUnitOfWork)) return false;
        if (memberName == null) {
            if (other.memberName != null) return false;
        } else if (!memberName.equals(other.memberName)) return false;
        if (minimizedSpecificData == null) {
            if (other.minimizedSpecificData != null) return false;
        } else if (!minimizedSpecificData.equals(other.minimizedSpecificData)) return false;
        if (minimizedSpecificDataText == null) {
            if (other.minimizedSpecificDataText != null) return false;
        } else if (!minimizedSpecificDataText.equals(other.minimizedSpecificDataText)) return false;
        if (nestedCommitLevel != other.nestedCommitLevel) return false;
        if (!Arrays.equals(nullIndicators, other.nullIndicators)) return false;
        if (objectIndicator == null) {
            if (other.objectIndicator != null) return false;
        } else if (!objectIndicator.equals(other.objectIndicator)) return false;
        if (objectIndicatorText == null) {
            if (other.objectIndicatorText != null) return false;
        } else if (!objectIndicatorText.equals(other.objectIndicatorText)) return false;
        if (objectLibrary == null) {
            if (other.objectLibrary != null) return false;
        } else if (!objectLibrary.equals(other.objectLibrary)) return false;
        if (objectName == null) {
            if (other.objectName != null) return false;
        } else if (!objectName.equals(other.objectName)) return false;
        if (objectType == null) {
            if (other.objectType != null) return false;
        } else if (!objectType.equals(other.objectType)) return false;
        if (outputFileLibraryName == null) {
            if (other.outputFileLibraryName != null) return false;
        } else if (!outputFileLibraryName.equals(other.outputFileLibraryName)) return false;
        if (outputFileMemberName == null) {
            if (other.outputFileMemberName != null) return false;
        } else if (!outputFileMemberName.equals(other.outputFileMemberName)) return false;
        if (outputFileName == null) {
            if (other.outputFileName != null) return false;
        } else if (!outputFileName.equals(other.outputFileName)) return false;
        if (programAsp != other.programAsp) return false;
        if (programAspDevice == null) {
            if (other.programAspDevice != null) return false;
        } else if (!programAspDevice.equals(other.programAspDevice)) return false;
        if (programLibrary == null) {
            if (other.programLibrary != null) return false;
        } else if (!programLibrary.equals(other.programLibrary)) return false;
        if (programName == null) {
            if (other.programName != null) return false;
        } else if (!programName.equals(other.programName)) return false;
        if (receiver == null) {
            if (other.receiver != null) return false;
        } else if (!receiver.equals(other.receiver)) return false;
        if (receiverAsp != other.receiverAsp) return false;
        if (receiverAspDevice == null) {
            if (other.receiverAspDevice != null) return false;
        } else if (!receiverAspDevice.equals(other.receiverAspDevice)) return false;
        if (receiverLibrary == null) {
            if (other.receiverLibrary != null) return false;
        } else if (!receiverLibrary.equals(other.receiverLibrary)) return false;
        if (referentialConstraint == null) {
            if (other.referentialConstraint != null) return false;
        } else if (!referentialConstraint.equals(other.referentialConstraint)) return false;
        if (referentialConstraintText == null) {
            if (other.referentialConstraintText != null) return false;
        } else if (!referentialConstraintText.equals(other.referentialConstraintText)) return false;
        if (remoteAddress == null) {
            if (other.remoteAddress != null) return false;
        } else if (!remoteAddress.equals(other.remoteAddress)) return false;
        if (remotePort != other.remotePort) return false;
        if (sequenceNumber == null) {
            if (other.sequenceNumber != null) return false;
        } else if (!sequenceNumber.equals(other.sequenceNumber)) return false;
        if (!Arrays.equals(specificData, other.specificData)) return false;
        if (stringSpecificData == null) {
            if (other.stringSpecificData != null) return false;
        } else if (!stringSpecificData.equals(other.stringSpecificData)) return false;
        if (systemName == null) {
            if (other.systemName != null) return false;
        } else if (!systemName.equals(other.systemName)) return false;
        if (systemSequenceNumber == null) {
            if (other.systemSequenceNumber != null) return false;
        } else if (!systemSequenceNumber.equals(other.systemSequenceNumber)) return false;
        if (threadId == null) {
            if (other.threadId != null) return false;
        } else if (!threadId.equals(other.threadId)) return false;
        if (time == null) {
            if (other.time != null) return false;
        } else if (!time.equals(other.time)) return false;
        if (timestamp == null) {
            if (other.timestamp != null) return false;
        } else if (!timestamp.equals(other.timestamp)) return false;
        if (transactionIdentifier == null) {
            if (other.transactionIdentifier != null) return false;
        } else if (!transactionIdentifier.equals(other.transactionIdentifier)) return false;
        if (trigger == null) {
            if (other.trigger != null) return false;
        } else if (!trigger.equals(other.trigger)) return false;
        if (triggerText == null) {
            if (other.triggerText != null) return false;
        } else if (!triggerText.equals(other.triggerText)) return false;
        if (userProfile == null) {
            if (other.userProfile != null) return false;
        } else if (!userProfile.equals(other.userProfile)) return false;
        return true;
    }
}
