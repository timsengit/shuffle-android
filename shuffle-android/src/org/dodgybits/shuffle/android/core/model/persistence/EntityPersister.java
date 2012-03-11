package org.dodgybits.shuffle.android.core.model.persistence;

import android.database.Cursor;
import android.net.Uri;
import org.dodgybits.shuffle.android.core.model.Entity;
import org.dodgybits.shuffle.android.core.model.Id;

import java.util.Collection;

public interface EntityPersister<E extends Entity> {

    Uri getContentUri();
    
    String[] getFullProjection();
    
    E findById(Id localId);
    
    E read(Cursor cursor);
        
    Uri insert(E e);
    
    void bulkInsert(Collection<E> entities);
    
    void update(E e);

    /**
     * Set deleted flag entity with the given id to isDeleted.
     *
     * @param id entity id
     * @param isDeleted flag to set deleted flag to
     * @return whether the operation succeeded
     */
    boolean updateDeletedFlag(Id id, boolean isDeleted);


    /**
     * Set deleted flag for entities that match the criteria to isDeleted.
     *
     * @param selection where clause
     * @param selectionArgs parameter values from where clause
     * @param isDeleted flag to set deleted flag to
     * @return number of entities updates
     */
    int updateDeletedFlag(String selection, String[] selectionArgs, boolean isDeleted);

    /**
     * Permanently delete all items that currently flagged as deleted.
     *
     * @return number of entities removed
     */
    int emptyTrash();

    /**
     * Permanently delete entity with the given id.
     *
     * @return whether the operation succeeded
     */
	boolean deletePermanently(Id id);

    /**
     * Find the entity with the given id in the query result.
     *
     * @param cursor query result to search
     * @param id entity id
     * @return position of entity in cursor or -1 if not found
     */
    int getPositionOfItemWithId(Cursor cursor, long id);

}
