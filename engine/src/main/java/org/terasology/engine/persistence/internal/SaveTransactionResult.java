// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.internal;

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
