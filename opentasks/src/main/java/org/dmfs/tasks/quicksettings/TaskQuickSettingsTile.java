package org.dmfs.tasks.quicksettings;

import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import androidx.annotation.RequiresApi;

import org.dmfs.tasks.EditTaskActivity;

@RequiresApi(api = Build.VERSION_CODES.N)
public class TaskQuickSettingsTile extends TileService {

    @Override
    public void onClick() {
        final Intent taskCreateIntent = new Intent(getApplicationContext(), EditTaskActivity.class);
        taskCreateIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        unlockAndRun(new Runnable() {
            @Override
            public void run() {
                startActivityAndCollapse(taskCreateIntent);
            }
        });
    }

    @Override
    public void onStartListening() {
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_ACTIVE);
        tile.updateTile();
    }

}
