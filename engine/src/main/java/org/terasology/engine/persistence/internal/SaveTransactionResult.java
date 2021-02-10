/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.persistence.internal;

/**
 * Represents the result of a {@link SaveTransaction}
 */
final class SaveTransactionResult {
    private final Throwable catchedThrowable;

    private SaveTransactionResult(Throwable catchedThrowable) {
        this.catchedThrowable = catchedThrowable;
    }

    static  SaveTransactionResult createSuccessResult() {
        return new SaveTransactionResult(null);
    }

    static  SaveTransactionResult createFailureResult(Throwable catchedThrowable) {
        return new SaveTransactionResult(catchedThrowable);
    }

    public boolean isSuccess() {
        return catchedThrowable == null;
    }

    public Throwable getCatchedThrowable() {
        return catchedThrowable;
    }
}
