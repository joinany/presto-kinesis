/*
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
package com.facebook.presto.kinesis;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;
import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;
import java.util.function.Supplier;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Useful suppliers.
 *
 * <p>All methods return serializable suppliers as long as they're given
 * serializable parameters.
 *
 * @author Laurence Gonsalves
 * @author Harry Heymann
 * @since 2.0 (imported from Google Collections Library)
 */
@GwtCompatible
public final class KinesisSuppliers
{
    private KinesisSuppliers()
    {
    }

    /**
     * Returns a supplier which caches the instance retrieved during the first
     * call to {@code get()} and returns that value on subsequent calls to
     * {@code get()}. See:
     * <a href="http://en.wikipedia.org/wiki/Memoization">memoization</a>
     *
     * <p>The returned supplier is thread-safe. The supplier's serialized form
     * does not contain the cached value, which will be recalculated when {@code
     * get()} is called on the reserialized instance.
     *
     * <p>If {@code delegate} is an instance created by an earlier call to {@code
     * memoize}, it is returned directly.
     */
    public static <T> Supplier<T> memoize(Supplier<T> delegate)
    {
        return (delegate instanceof MemoizingSupplier)
                ? delegate
                : new MemoizingSupplier<T>(checkNotNull(delegate));
    }

    @VisibleForTesting
    static class MemoizingSupplier<T> implements Supplier<T>, Serializable
    {
        final Supplier<T> delegate;
        transient volatile boolean initialized;
        // "value" does not need to be volatile; visibility piggy-backs
        // on volatile read of "initialized".
        transient T value;

        MemoizingSupplier(Supplier<T> delegate)
        {
            this.delegate = delegate;
        }

        @Override public T get()
        {
            // A 2-field variant of Double Checked Locking.
            if (!initialized) {
                synchronized (this) {
                    if (!initialized) {
                        T t = delegate.get();
                        value = t;
                        initialized = true;
                        return t;
                    }
                }
            }
            return value;
        }

        @Override public String toString()
        {
            return "Suppliers.memoize(" + delegate + ")";
        }

        private static final long serialVersionUID = 0;
    }

    /**
     * Returns a supplier that always supplies {@code instance}.
     */
    public static <T> Supplier<T> ofInstance(@Nullable T instance)
    {
        return new SupplierOfInstance<T>(instance);
    }

    private static class SupplierOfInstance<T>
            implements Supplier<T>, Serializable
    {
        final T instance;

        SupplierOfInstance(@Nullable T instance)
        {
            this.instance = instance;
        }

        @Override public T get()
        {
            return instance;
        }

        @Override public boolean equals(@Nullable Object obj)
        {
            if (obj instanceof SupplierOfInstance) {
                SupplierOfInstance<?> that = (SupplierOfInstance<?>) obj;
                return Objects.equals(instance, that.instance);
            }
            return false;
        }

        @Override public int hashCode()
        {
            return Objects.hashCode(instance);
        }

        @Override public String toString()
        {
            return "Suppliers.ofInstance(" + instance + ")";
        }

        private static final long serialVersionUID = 0;
    }
}
