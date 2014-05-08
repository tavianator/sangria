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
import com.google.inject.spi.Message;

import com.tavianator.sangria.core.Priority;

/**
 * Error holder for {@link ListBinder}s.
 *
 * @param <T> Only used to allow different {@link Key}s.
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.1
 * @since 1.1
 */
class ListBinderErrors<T> {
    final Priority priority;
    final Message duplicateBindersError;
    final Message conflictingDefaultExplicitError;

    ListBinderErrors(Priority priority, Message duplicateBindersError, Message conflictingDefaultExplicitError) {
        this.priority = priority;
        this.duplicateBindersError = duplicateBindersError;
        this.conflictingDefaultExplicitError = conflictingDefaultExplicitError;
    }
}
