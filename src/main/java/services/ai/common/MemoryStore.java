package services.ai.common;

import java.util.List;

/**
 * MemoryStore contract port (commerce-common/memory.py): implement against
 * your own storage. subject_id is the user id (shopping) or merchant id.
 */
public interface MemoryStore {
    record Fact(String key, String value, String category, long updatedAt) {}

    List<Fact> getFacts(String subjectId);

    void upsertFacts(String subjectId, List<Fact> facts);

    List<Fact> searchFacts(String subjectId, String query);

    /** Remove one fact by key; true when something was deleted. */
    boolean deleteFact(String subjectId, String key);

    /** Purge every fact for the subject. */
    void clear(String subjectId);
}
