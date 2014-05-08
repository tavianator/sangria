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

package com.tavianator.sangria.listbinder;

import com.google.inject.Key;

import com.tavianator.sangria.core.Priority;

/**
 * An individual element in a ListBinder.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.1
 * @since 1.1
 */
class ListElement<T> implements Comparable<ListElement<T>> {
    final Key<T> key;
    final Priority priority;

    ListElement(Key<T> key, Priority priority) {
        this.key = key;
        this.priority = priority;
    }

    @Override
    public int compareTo(ListElement<T> o) {
        return priority.compareTo(o.priority);
    }
}
