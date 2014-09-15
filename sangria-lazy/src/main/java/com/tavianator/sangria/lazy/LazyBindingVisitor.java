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

package com.tavianator.sangria.lazy;

import com.google.inject.spi.BindingTargetVisitor;

/**
 * Visitor interface for the lazy binding SPI.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.2
 * @since 1.2
 */
public interface LazyBindingVisitor<T, V> extends BindingTargetVisitor<T, V> {
    /**
     * Visit a {@link LazyBinding}.
     *
     * @param binding The binding to visit.
     * @return A value of type {@code V}.
     */
    V visit(LazyBinding<? extends T> binding);
}
