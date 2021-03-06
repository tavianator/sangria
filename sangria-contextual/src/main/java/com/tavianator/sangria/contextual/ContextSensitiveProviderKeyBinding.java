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

package com.tavianator.sangria.contextual;

import com.google.inject.Key;

/**
 * SPI for {@link ContextSensitiveProvider} key bindings.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @since 1.0
 * @see ContextSensitiveBindingBuilder#toContextSensitiveProvider(Key)
 */
public interface ContextSensitiveProviderKeyBinding<T>  {
    /**
     * @return The {@link Key} used to retrieve the {@link ContextSensitiveProvider}'s binding.
     */
    Key<? extends ContextSensitiveProvider<? extends T>> getContextSensitiveProviderKey();
}
