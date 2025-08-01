/*
 * Copyright 2021-2025 The BoardGameWork Authors
 * SPDX-License-Identifier: Apache-2.0
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

@file:Suppress("unused")

package tools.aqua.bgw.animation

import IDGenerator
import tools.aqua.bgw.event.AnimationFinishedEvent
import tools.aqua.bgw.event.AnimationCleanedEvent

/**
 * [Animation] baseclass.
 *
 * @param duration Duration in milliseconds.
 *
 * @since 0.1
 */
abstract class Animation(
    /** Duration in milliseconds. */
    val duration: Int
) {
  internal val id = IDGenerator.generateAnimationID()

  /** [Boolean] indicating whether the [Animation] is currently running. */
  var isRunning: Boolean = false
    internal set

  /** Gets invoked when [Animation] has finished. */
  var onFinished: ((AnimationFinishedEvent) -> Unit)? = null

  /** Gets invoked when [Animation] has been cleaned up. An Animation is cleaned up [CLEANUP_MS](100ms) after onFinished is invoked. */
  var onCleaned: ((AnimationCleanedEvent) -> Unit)? = null
}
