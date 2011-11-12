/*
 * Copyright (C) 2009 Android Shuffle Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dodgybits.shuffle.android.list.activity.task;

import org.dodgybits.shuffle.android.core.model.Id;
import org.dodgybits.shuffle.android.core.model.Project;
import org.dodgybits.shuffle.android.core.model.Task;
import org.dodgybits.shuffle.android.core.model.persistence.EntityPersister;
import org.dodgybits.shuffle.android.core.model.persistence.selector.TaskSelector;
import org.dodgybits.shuffle.android.core.view.MenuUtils;
import org.dodgybits.shuffle.android.list.annotation.ProjectTasks;
import org.dodgybits.shuffle.android.list.config.ListConfig;
import org.dodgybits.shuffle.android.list.config.ProjectTasksListConfig;
import org.dodgybits.shuffle.android.persistence.provider.ProjectProvider;
import org.dodgybits.shuffle.android.persistence.provider.TaskProvider;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;

import com.google.inject.Inject;

public class ProjectTasksActivity extends AbstractTaskListActivity {

	private static final String cTag = "ProjectTasksActivity";
	private Id mProjectId;
	private Project mProject;

    @Inject @ProjectTasks
    private ProjectTasksListConfig mTaskListConfig;

    @Inject private EntityPersister<Project> mPersister;
    
	@Override
    public void onCreate(Bundle icicle) {
		Uri contextURI = getIntent().getData();
		mProjectId = Id.create(ContentUris.parseId(contextURI));
        super.onCreate(icicle);
	}

	@Override
    protected ListConfig<Task,TaskSelector> createListConfig()
	{
        mTaskListConfig.setProjectId(mProjectId);
        return mTaskListConfig;
    }


	@Override
	protected void onResume() {
		Log.d(cTag, "Fetching project " + mProjectId);
		Cursor cursor = getContentResolver().query(ProjectProvider.Projects.CONTENT_URI, ProjectProvider.Projects.FULL_PROJECTION,
				ProjectProvider.Projects._ID + " = ? ", new String[] {String.valueOf(mProjectId)}, null);
		if (cursor.moveToNext()) {
			mProject = mPersister.read(cursor);
            mTaskListConfig.setProject(mProject);
		}
		cursor.close();
		
		super.onResume();
	}

    /**
     * Return the intent generated when a list item is clicked.
     * 
     * @param uri type of data selected
     */ 
    protected Intent getClickIntent(Uri uri) {
    	long taskId = ContentUris.parseId(uri);
    	Uri taskUri = ContentUris.appendId(TaskProvider.Tasks.CONTENT_URI.buildUpon(), taskId).build();
    	return new Intent(Intent.ACTION_VIEW, taskUri);
    }
    
    /**
     * Add project id to intent extras so it can be pre-filled for the task.
     */
    protected Intent getInsertIntent() {
    	Intent intent = super.getInsertIntent();
    	Bundle extras = intent.getExtras();
    	if (extras == null) {
    	    extras = new Bundle();
    	}
    	extras.putLong(TaskProvider.Tasks.PROJECT_ID, mProject.getLocalId().getId());
    	
    	final Id defaultContextId = mProject.getDefaultContextId();
    	if (defaultContextId.isInitialised()) {
       		extras.putLong(TaskProvider.Tasks.CONTEXT_ID, defaultContextId.getId());
    	}
    	
    	intent.putExtras(extras);
    	return intent;
    }

    @Override
    protected void onCreateEntityContextMenu(ContextMenu menu, int position, Task task) {
    	MenuUtils.addMoveMenuItems(menu,
    			moveUpPermitted(position), moveDownPermitted(position));
    }

    @Override
    protected boolean onContextEntitySelected(MenuItem item, int position, Task task) {
        switch (item.getItemId()) {
	        case MenuUtils.MOVE_UP_ID:
	            moveUp(position);
	            return true;
	        case MenuUtils.MOVE_DOWN_ID:
	            moveDown(position);
	            return true;
        }
        return super.onContextEntitySelected(item, position, task);
    }	
    
    private boolean moveUpPermitted(int selection) {
    	return selection > 0;
    }
    
    private boolean moveDownPermitted(int selection) {
    	return selection < getItemCount() - 1;
    }
    
    protected final void moveUp(int selection) {
    	if (moveUpPermitted(selection)) {
    		Cursor cursor = (Cursor) getListAdapter().getItem(selection);
    		getTaskPersister().swapTaskPositions(cursor, selection - 1, selection);
    	}
    }
    
    protected final void moveDown(int selection) {
    	if (moveDownPermitted(selection)) {
    		Cursor cursor = (Cursor) getListAdapter().getItem(selection);
            getTaskPersister().swapTaskPositions(cursor, selection, selection + 1);
    	}
    }

}