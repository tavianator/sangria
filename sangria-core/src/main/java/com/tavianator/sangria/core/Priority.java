/****************************************************************************
 * Sangria                                                                  *
 * Copyright (C) 2014 Tavian Barnes <tavianator@tavianator.com>             *
 *                                                                          *
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 * http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ****************************************************************************/

package com.tavianator.sangria.core;

import java.util.*;

import com.google.common.collect.ComparisonChain;
import com.google.common.primitives.Ints;

/**
 * A loosely-coupled, infinitely divisible priority/weight system.
 *
 * <p>
 * This class implements an extensible priority system based on lexicographical ordering. In its simplest use, {@code
 * Priority.create(0)} is ordered before {@code Priority.create(1)}, then {@code Priority.create(2)}, etc.
 * </p>
 *
 * <p>
 * To create a priority that is ordered between two existing ones, simply add another parameter: {@code
 * Priority.create(1, 1)} comes after {@code Priority.create(1)}, but before {@code Priority.create(2)}. In this way,
 * priorities can always be inserted anywhere in a sequence.
 * </p>
 *
 * <p>
 * The {@link #next()} method creates a priority that is ordered immediately following the current one, and is distinct
 * from all priorities obtained by any other means. This provides a convenient way to order entire segments of lists.
 * </p>
 *
 * <p>
 * A special priority, obtained by {@code Priority.getDefault()}, sorts before all other priorities.
 * </p>
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.1
 * @since 1.1
 */
public class Priority implements Comparable<Priority> {
    private static final Priority DEFAULT = new Priority(new int[0], 0);
    private static final Comparator<int[]> COMPARATOR = Ints.lexicographicalComparator();

    private final int[] weights;
    private final int seq;

    /**
     * @return The default priority, which comes before all other priorities.
     */
    public static Priority getDefault() {
        return DEFAULT;
    }

    /**
     * Create a {@link Priority} with the given sequence.
     *
     * @param weight  The first value of the weight sequence.
     * @param weights An integer sequence. These sequences are sorted lexicographically, so {@code Priority.create(1)}
     *                sorts before {@code Priority.create(1, 1)}, which sorts before {@code Priority.create(2)}.
     * @return A new {@link Priority}.
     */
    public static Priority create(int weight, int... weights) {
        int[] newWeights = new int[weights.length + 1];
        newWeights[0] = weight;
        System.arraycopy(weights, 0, newWeights, 1, weights.length);
        return new Priority(newWeights, 0);
    }

    private Priority(int[] weights, int seq) {
        this.weights = weights;
        this.seq = seq;
    }

    /**
     * @return Whether this priority originated in a call to {@link #getDefault()}.
     */
    public boolean isDefault() {
        return weights.length == 0;
    }

    /**
     * @return A new {@link Priority} which immediately follows this one, and which is distinct from all other
     * priorities obtained by {@link #create(int, int...)}.
     */
    public Priority next() {
        return new Priority(weights, seq + 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Priority)) {
            return false;
        }

        Priority other = (Priority)obj;
        return Arrays.equals(weights, other.weights)
                && seq == other.seq;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(weights) + seq;
    }

    @Override
    public int compareTo(Priority o) {
        return ComparisonChain.start()
                .compare(weights, o.weights, COMPARATOR)
                .compare(seq, o.seq)
                .result();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Priority.");
        if (weights.length == 0) {
            builder.append("getDefault()");
        } else {
            builder.append("create(");
            for (int i = 0; i < weights.length; ++i) {
                if (i != 0) {
                    builder.append(", ");
                }
                builder.append(weights[i]);
            }
            builder.append(")");
        }
        if (seq != 0) {
            builder.append(".next(")
                    .append(seq)
                    .append(")");
        }
        return builder.toString();
    }
}
