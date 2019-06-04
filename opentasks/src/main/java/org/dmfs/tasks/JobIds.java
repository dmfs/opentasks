/*
 * Copyright 2019 dmfs GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dmfs.tasks;

import androidx.core.app.JobIntentService;


/**
 * Apparently there is some sort of contraint on Job IDs used with {@link JobIntentService}s or Jobs in general.
 * <p>
 * To avoid ID collisions this interface hosts all Job IDs we're using.
 *
 * @author Marten Gajda
 */
public interface JobIds
{
    // base number ("task" in hex)
    int BASE = 0x7461736b;

    int NOTIFICATION_SERVICE = BASE + 1;
    int NOTIFICATION_ACTION_SERVICE = BASE + 2;
}
