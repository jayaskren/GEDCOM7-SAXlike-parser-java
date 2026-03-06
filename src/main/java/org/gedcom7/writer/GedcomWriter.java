package org.gedcom7.writer;

import org.gedcom7.writer.context.*;
import org.gedcom7.writer.internal.LineEmitter;
import org.gedcom7.writer.internal.XrefGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

/**
 * Streaming GEDCOM writer that produces valid GEDCOM output via
 * typed context classes and lambda-scoped nesting.
 *
 * <p>Example usage:
 * <pre>{@code
 * try (GedcomWriter writer = new GedcomWriter(outputStream)) {
 *     writer.head(head -> head.source("MyApp"));
 *     Xref john = writer.individual(indi -> {
 *         indi.personalName("John /Doe/");
 *         indi.birth(birt -> birt.date("15 MAR 1955"));
 *     });
 *     writer.trailer();
 * }
 * }</pre>
 */
public final class GedcomWriter implements AutoCloseable {

    private final LineEmitter emitter;
    private final GedcomWriterConfig config;
    private final XrefGenerator xrefGenerator;
    private boolean headWritten;
    private boolean trlrWritten;
    private boolean closed;

    /**
     * Creates a writer with default GEDCOM 7 configuration.
     */
    public GedcomWriter(OutputStream out) {
        this(out, GedcomWriterConfig.gedcom7());
    }

    /**
     * Creates a writer with the specified configuration.
     */
    public GedcomWriter(OutputStream out, GedcomWriterConfig config) {
        this.emitter = new LineEmitter(out, config);
        this.config = config;
        this.xrefGenerator = new XrefGenerator();
    }

    // --- HEAD ---

    /**
     * Writes the HEAD record.
     */
    public void head(Consumer<HeadContext> body) throws GedcomWriteException {
        checkNotClosed();
        try {
            emitter.emitLine(0, null, "HEAD", null);
            // GEDC substructure
            emitter.emitLine(1, null, "GEDC", null);
            String versString = config.getVersion().isGedcom7() ? "7.0"
                    : config.getVersion().toString();
            emitter.emitLine(2, null, "VERS", versString);

            // GEDCOM 5.5.5 requires CHAR substructure
            if (!config.getVersion().isGedcom7()) {
                emitter.emitLine(1, null, "CHAR", "UTF-8");
            }

            if (body != null) {
                HeadContext ctx = new HeadContext(emitter, 0);
                body.accept(ctx);
            }
            headWritten = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // --- Top-level records (auto-generated xref) ---

    public Xref individual(Consumer<IndividualContext> body) throws GedcomWriteException {
        return writeRecord("INDI", "I", null, emitter -> {
            IndividualContext ctx = new IndividualContext(emitter, 0);
            body.accept(ctx);
        });
    }

    public Xref family(Consumer<FamilyContext> body) throws GedcomWriteException {
        return writeRecord("FAM", "F", null, emitter -> {
            FamilyContext ctx = new FamilyContext(emitter, 0);
            body.accept(ctx);
        });
    }

    public Xref source(Consumer<SourceContext> body) throws GedcomWriteException {
        return writeRecord("SOUR", "S", null, emitter -> {
            SourceContext ctx = new SourceContext(emitter, 0);
            body.accept(ctx);
        });
    }

    public Xref repository(Consumer<RepositoryContext> body) throws GedcomWriteException {
        return writeRecord("REPO", "R", null, emitter -> {
            RepositoryContext ctx = new RepositoryContext(emitter, 0);
            body.accept(ctx);
        });
    }

    public Xref multimedia(Consumer<MultimediaContext> body) throws GedcomWriteException {
        return writeRecord("OBJE", "O", null, emitter -> {
            MultimediaContext ctx = new MultimediaContext(emitter, 0);
            body.accept(ctx);
        });
    }

    public Xref submitter(Consumer<SubmitterContext> body) throws GedcomWriteException {
        return writeRecord("SUBM", "U", null, emitter -> {
            SubmitterContext ctx = new SubmitterContext(emitter, 0);
            body.accept(ctx);
        });
    }

    public Xref sharedNote(Consumer<NoteContext> body) throws GedcomWriteException {
        return writeRecord("SNOTE", "N", null, emitter -> {
            NoteContext ctx = new NoteContext(emitter, 0);
            body.accept(ctx);
        });
    }

    /**
     * Writes a shared note record with text as the record-level value.
     *
     * @param text the note text (may contain newlines, which produce CONT lines)
     * @param body lambda to add child structures
     * @return an Xref handle for the created record
     */
    public Xref sharedNoteWithText(String text, Consumer<NoteContext> body) throws GedcomWriteException {
        return writeRecordWithValue("SNOTE", "N", null, text, emitter -> {
            NoteContext ctx = new NoteContext(emitter, 0);
            body.accept(ctx);
        });
    }

    /**
     * Writes a shared note record with a developer-provided ID and text as the record-level value.
     *
     * @param id   the cross-reference identifier
     * @param text the note text (may contain newlines, which produce CONT lines)
     * @param body lambda to add child structures
     * @return an Xref handle for the created record
     */
    public Xref sharedNoteWithText(String id, String text, Consumer<NoteContext> body) throws GedcomWriteException {
        return writeRecordWithValue("SNOTE", "N", id, text, emitter -> {
            NoteContext ctx = new NoteContext(emitter, 0);
            body.accept(ctx);
        });
    }

    // --- Top-level records (developer-provided xref ID) ---

    public Xref individual(String id, Consumer<IndividualContext> body) throws GedcomWriteException {
        return writeRecord("INDI", "I", id, emitter -> {
            IndividualContext ctx = new IndividualContext(emitter, 0);
            body.accept(ctx);
        });
    }

    public Xref family(String id, Consumer<FamilyContext> body) throws GedcomWriteException {
        return writeRecord("FAM", "F", id, emitter -> {
            FamilyContext ctx = new FamilyContext(emitter, 0);
            body.accept(ctx);
        });
    }

    public Xref source(String id, Consumer<SourceContext> body) throws GedcomWriteException {
        return writeRecord("SOUR", "S", id, emitter -> {
            SourceContext ctx = new SourceContext(emitter, 0);
            body.accept(ctx);
        });
    }

    public Xref repository(String id, Consumer<RepositoryContext> body) throws GedcomWriteException {
        return writeRecord("REPO", "R", id, emitter -> {
            RepositoryContext ctx = new RepositoryContext(emitter, 0);
            body.accept(ctx);
        });
    }

    public Xref multimedia(String id, Consumer<MultimediaContext> body) throws GedcomWriteException {
        return writeRecord("OBJE", "O", id, emitter -> {
            MultimediaContext ctx = new MultimediaContext(emitter, 0);
            body.accept(ctx);
        });
    }

    public Xref submitter(String id, Consumer<SubmitterContext> body) throws GedcomWriteException {
        return writeRecord("SUBM", "U", id, emitter -> {
            SubmitterContext ctx = new SubmitterContext(emitter, 0);
            body.accept(ctx);
        });
    }

    public Xref sharedNote(String id, Consumer<NoteContext> body) throws GedcomWriteException {
        return writeRecord("SNOTE", "N", id, emitter -> {
            NoteContext ctx = new NoteContext(emitter, 0);
            body.accept(ctx);
        });
    }

    // --- Escape hatches for arbitrary record types ---

    public Xref record(String tag, Consumer<GeneralContext> body) throws GedcomWriteException {
        return writeRecord(tag, "X", null, emitter -> {
            GeneralContext ctx = new GeneralContext(emitter, 0);
            body.accept(ctx);
        });
    }

    public Xref record(String id, String tag, Consumer<GeneralContext> body) throws GedcomWriteException {
        return writeRecord(tag, "X", id, emitter -> {
            GeneralContext ctx = new GeneralContext(emitter, 0);
            body.accept(ctx);
        });
    }

    public void record(String tag, String value) throws GedcomWriteException {
        checkNotClosed();
        ensureHead();
        try {
            emitter.emitLine(0, null, tag, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // --- Trailer ---

    /**
     * Writes the TRLR (trailer) record.
     */
    public void trailer() {
        if (trlrWritten) return;
        try {
            emitter.emitLine(0, null, "TRLR", null);
            trlrWritten = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // --- AutoCloseable ---

    @Override
    public void close() throws IOException {
        if (closed) return;
        try {
            if (!headWritten) {
                head(head -> {});
            }
            if (!trlrWritten) {
                trailer();
            }
            emitter.flush();
        } finally {
            closed = true;
        }
    }

    // --- Internal ---

    @FunctionalInterface
    private interface RecordBody {
        void write(LineEmitter emitter);
    }

    private Xref writeRecord(String tag, String prefix, String devId, RecordBody body) throws GedcomWriteException {
        checkNotClosed();
        ensureHead();

        String xrefId = devId != null ? devId : xrefGenerator.next(prefix);
        Xref xref = Xref.of(xrefId);

        try {
            emitter.emitLine(0, xrefId, tag, null);
            body.write(emitter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return xref;
    }

    private Xref writeRecordWithValue(String tag, String prefix, String devId, String value, RecordBody body) throws GedcomWriteException {
        checkNotClosed();
        ensureHead();

        String xrefId = devId != null ? devId : xrefGenerator.next(prefix);
        Xref xref = Xref.of(xrefId);

        try {
            emitter.emitValueWithCont(0, xrefId, tag, value);
            body.write(emitter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return xref;
    }

    private void checkNotClosed() {
        if (closed) {
            throw new IllegalStateException("Writer has been closed");
        }
    }

    private void ensureHead() throws GedcomWriteException {
        if (!headWritten) {
            if (config.isStrict()) {
                throw new GedcomWriteException(
                        "HEAD not explicitly written; call head() before writing records in strict mode");
            }
            WarningHandler handler = config.getWarningHandler();
            if (handler != null) {
                handler.handle(new GedcomWriteWarning(
                        "HEAD not explicitly written; auto-generating minimal HEAD record", "HEAD"));
            }
            head(head -> {});
        }
    }
}
