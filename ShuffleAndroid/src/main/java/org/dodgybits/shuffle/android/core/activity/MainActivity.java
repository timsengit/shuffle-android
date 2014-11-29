package org.dodgybits.shuffle.android.core.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import com.google.inject.Inject;
import org.dodgybits.android.shuffle.R;
import org.dodgybits.shuffle.android.core.event.*;
import org.dodgybits.shuffle.android.core.listener.MainListeners;
import org.dodgybits.shuffle.android.core.util.UiUtilities;
import org.dodgybits.shuffle.android.core.view.MainView;
import org.dodgybits.shuffle.android.core.view.NavigationDrawerFragment;
import org.dodgybits.shuffle.android.list.event.ViewPreferencesEvent;
import org.dodgybits.shuffle.android.list.view.context.ContextListFragment;
import org.dodgybits.shuffle.android.list.view.project.ProjectListFragment;
import org.dodgybits.shuffle.android.list.view.task.TaskListContext;
import org.dodgybits.shuffle.android.list.view.task.TaskListFragment;
import org.dodgybits.shuffle.android.view.fragment.TaskPagerFragment;
import roboguice.activity.RoboActionBarActivity;
import roboguice.event.EventManager;
import roboguice.event.Observes;

public class MainActivity extends RoboActionBarActivity {
    private static final String TAG = "MainActivity";

    private static final int WHATS_NEW_DIALOG = 5000;

    public static final String MAIN_VIEW_KEY = "MainActivity.mainView";

    /** Tags used when loading fragments. */
    public static final String TAG_TASK_LIST = "tag-task-list";
    public static final String TAG_TASK_ITEM = "tag-task-item";
    public static final String TAG_CONTEXT_LIST = "tag-context-list";
    public static final String TAG_PROJECT_LIST = "tag-project-list";

    private MainView mMainView;

    @Inject
    private MainListeners mListeners;

    @Inject
    private EventManager mEventManager;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * The application can be started from the following entry points:
     * <ul>
     *     <li>Launcher: you tap on Shuffle icon in the launcher. This is what most users think of
     *         as “Starting the app”.</li>
     *     <li>Shortcut: Users can make a shortcut to take them directly to view.</li>
     *     <li>Widget: Shows the contents of a list view, and allows:
     *     <ul>
     *         <li>Viewing the list (tapping on the title)</li>
     *         <li>Creating a new action (tapping on the new message icon in the title. This
     *         launches the {@link org.dodgybits.shuffle.android.editor.activity.EditTaskActivity}.
     *         </li>
     *         <li>Viewing a single action (tapping on a list element)</li>
     *     </ul>
     *
     *     </li>
     *     <li>...and most importantly, the activity life cycle can tear down the application and
     *     restart it:
     *     <ul>
     *         <li>Rotate the application: it is destroyed and recreated.</li>
     *         <li>Navigate away, and return from recent applications.</li>
     *     </ul>
     *     </li>
     * </ul>
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        // don't show soft keyboard unless user clicks on quick add box
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        final boolean tabletUi = UiUtilities.useTabletUI(this.getResources());
        Log.d(TAG, "Using tablet layout? " + tabletUi);
        setContentView(tabletUi ? R.layout.two_pane_activity : R.layout.one_pane_activity);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                drawerLayout);

        mEventManager.fire(new OnCreatedEvent());
    }

    @Override
    public Dialog onCreateDialog(int id, Bundle bundle) {
        Dialog dialog = null;
        if (id == WHATS_NEW_DIALOG) {
            dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.whats_new_dialog_title)
                    .setPositiveButton(R.string.ok_button_title, null)
                    .setMessage(R.string.whats_new_dialog_message)
                    .create();
        }

        return dialog;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.

            // TODO define menu when drawer open
//            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_preferences:
                Log.d(TAG, "Bringing up preferences");
                mEventManager.fire(new ViewPreferencesEvent());
                return true;
            case R.id.action_search:
                Log.d(TAG, "Bringing up search");
                // TODO - start search
                return true;
        }

        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(MAIN_VIEW_KEY, mMainView);

        super.onSaveInstanceState(outState);
    }

    private void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
//        if (mTitle != null) {
//            actionBar.setTitle(mTitle);
//        }
    }

    public void onViewChanged(@Observes MainViewUpdateEvent event) {
        mMainView = event.getMainView();
    }

    public void onTaskListCursorLoaded(@Observes TaskListCursorLoadedEvent event) {
        Log.i(TAG, "Task list Cursor loaded - loading fragment now");
        switch (mMainView.getViewMode()) {
            case TASK_LIST:
                addTaskList(event.getTaskListContext());
                break;
            case TASK:
                addTaskList(event.getTaskListContext());
                addTaskView(event.getTaskListContext());
                break;
            default:
                Log.w(TAG, "Unexpected view mode " + mMainView.getViewMode());
                break;
        }
    }

    public void onContextListCursorLoaded(@Observes ContextListCursorLoadedEvent event) {
        addContextList();
    }

    public void onProjectListCursorLoaded(@Observes ProjectListCursorLoadedEvent event) {
        addProjectList();
    }

    private void addTaskList(TaskListContext listContext) {
        TaskListFragment fragment = getTaskListFragment();
        if (fragment == null) {
            fragment = new TaskListFragment();
            Log.d(TAG, "Creating task list fragment " + fragment);

            FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            // Use cross fading animation.
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragmentTransaction.replace(R.id.entity_list_pane, fragment,
                    TAG_TASK_LIST);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private void addTaskView(TaskListContext listContext) {
        TaskPagerFragment fragment = getTaskPagerFragment() ;
        if (fragment == null) {
            fragment = new TaskPagerFragment();
            Log.d(TAG, "Creating task pager " + fragment);

            FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            // Use cross fading animation.
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragmentTransaction.replace(R.id.task_pane, fragment,
                    TAG_TASK_ITEM);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private void addContextList() {
        ContextListFragment fragment = getContextListFragment();
        if (fragment == null) {
            fragment = new ContextListFragment();
            Log.d(TAG, "Creating context list fragment " + fragment);

            FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            // Use cross fading animation.
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragmentTransaction.replace(R.id.entity_list_pane, fragment,
                    TAG_CONTEXT_LIST);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private void addProjectList() {
        ProjectListFragment fragment = getProjectListFragment();
        if (fragment == null) {
            fragment = new ProjectListFragment();
            Log.d(TAG, "Creating project list fragment " + fragment);

            FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            // Use cross fading animation.
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragmentTransaction.replace(R.id.entity_list_pane, fragment,
                    TAG_PROJECT_LIST);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    /**
     * Get the task list fragment for this activity. If the task list fragment is
     * not attached, this method returns null.
     *
     * Caution! This method returns the {@link TaskListFragment} after the fragment has been
     * added, <b>and</b> after the {@link android.app.FragmentManager} has run through its queue to add the
     * fragment. There is a non-trivial amount of time after the fragment is instantiated and before
     * this call returns a non-null value, depending on the {@link android.app.FragmentManager}. If you
     * need the fragment immediately after adding it, consider making the fragment an observer of
     * the controller and perform the task immediately on {@link android.app.Fragment#onActivityCreated(Bundle)}
     */
    protected TaskListFragment getTaskListFragment() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_TASK_LIST);
        if (isValidFragment(fragment)) {
            return (TaskListFragment) fragment;
        }
        return null;
    }

    protected TaskPagerFragment getTaskPagerFragment() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_TASK_ITEM);
        if (isValidFragment(fragment)) {
            return (TaskPagerFragment) fragment;
        }
        return null;
    }

    protected ContextListFragment getContextListFragment() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTEXT_LIST);
        if (isValidFragment(fragment)) {
            return (ContextListFragment) fragment;
        }
        return null;
    }

    protected ProjectListFragment getProjectListFragment() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_PROJECT_LIST);
        if (isValidFragment(fragment)) {
            return (ProjectListFragment) fragment;
        }
        return null;
    }

    /**
     * Check if the fragment is attached to an activity and has a root view.
     * @param in fragment to be checked
     * @return true if the fragment is valid, false otherwise
     */
    private static boolean isValidFragment(Fragment in) {
        return !(in == null || in.getActivity() == null || in.getView() == null);
    }

}
