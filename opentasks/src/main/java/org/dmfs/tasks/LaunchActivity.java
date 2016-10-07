package org.dmfs.tasks;

import android.content.Intent;
import android.os.Bundle;
import org.dmfs.tasks.utils.AppCompatActivity;


/**
 * The MAIN Activity which is launched by the app icon from the home screen.
 * <p>
 * It is an empty Activity. If the app is not running, it launches {@link TaskListActivity}, otherwise the app's task is
 * brought to foreground by default system behavior.
 * <p>
 * See <a href=http://stackoverflow.com/a/9532756/4247460>http://stackoverflow.com/a/9532756/4247460</a> and <a
 * href=http://stackoverflow.com/questions/2417468>http://stackoverflow.com/questions/2417468</a> (for why is this
 * needed).
 *
 * @author Gabor Keszthelyi
 */
public class LaunchActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /*
        If this activity is the root activity of the task, the app is not running.

        Note: It seems this onCreate() is not even called if app was in background and this Activity is targeted with an intent.
        So this check may not be needed at all, just keeping here to be on the safe side.)
        */
        if (isTaskRoot())
        {
            Intent intent = new Intent(getApplicationContext(), TaskListActivity.class);
            startActivity(intent);
        }

        finish();
    }
}
