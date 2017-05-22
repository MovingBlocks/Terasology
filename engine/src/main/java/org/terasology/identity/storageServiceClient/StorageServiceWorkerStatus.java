/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.identity.storageServiceClient;

public enum StorageServiceWorkerStatus {
    // Logged out for a "good reason" (user never logged in, or manually logged out)
    LOGGED_OUT_OK,
    // Logged out due to an external reason (expired session token, service unreachable, etc)
    LOGGED_OUT_ERROR,
    // Logged in and doing nothing
    LOGGED_IN_IDLE,
    // Performing an operation (syncing, logging out, etc)
    LOGGED_IN_WORKING,
    // Performing login
    LOGGING_IN
}
