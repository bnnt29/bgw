/*
 * Copyright 2023-2025 The BoardGameWork Authors
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

package tools.aqua.bgw.components.layoutviews

import data.event.InternalCameraPanData
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.event.MouseButtonType
import tools.aqua.bgw.observable.properties.BooleanProperty
import tools.aqua.bgw.observable.properties.DoubleProperty
import tools.aqua.bgw.observable.properties.Property
import tools.aqua.bgw.util.Coordinate
import tools.aqua.bgw.visual.Visual

/**
 * A pane representing a camera view that can be used to display and manipulate a target layout
 * view.
 *
 * @param T The type of the [target] [LayoutView]. Must extend the [LayoutView] class.
 * @param posX The x-coordinate of the camera pane's position on the screen. Default is 0.
 * @param posY The y-coordinate of the camera pane's position on the screen. Default is 0.
 * @param width The width of the camera pane.
 * @param height The height of the camera pane.
 * @param visual The visual representation of the camera pane. Default is an empty visual.
 * @param target The target [LayoutView] that this camera pane will display.
 * @param limitBounds Whether the target layout view should be limited to the bounds of the camera
 * pane. Default is true.
 *
 * @see GridPane
 * @see Pane
 * @see Visual
 * @see ComponentView
 *
 * @since 0.8
 */
open class CameraPane<T : LayoutView<*>>(
    posX: Number = 0,
    posY: Number = 0,
    width: Number,
    height: Number,
    visual: Visual = Visual.EMPTY,
    limitBounds: Boolean = true,
    internal val target: T
) : ComponentView(posX = posX, posY = posY, width = width, height = height, visual = visual) {
  /** [Property] for the [zoom] state of the [CameraPane]. */
  internal val zoomProperty: DoubleProperty = DoubleProperty(1)

  internal var internalData: InternalCameraPanData = InternalCameraPanData()

  /** Zoom factor of the camera starting from 1. */
  var zoom: Double
    get() = zoomProperty.value
    set(value) {
      zoom(value)
    }

  /** [Property] for the [interactive] state of the [CameraPane]. */
  internal val interactiveProperty: BooleanProperty = BooleanProperty(false)

  /**
   * Determines if the camera pane is interactive, which means that you can scroll to zoom and drag
   * to pan around.
   *
   * @see panMouseButton
   */
  var interactive: Boolean
    get() = interactiveProperty.value
    set(value) {
      interactiveProperty.value = value
    }

  /** [Property] for the [limitBounds] state of the [CameraPane]. */
  internal val limitBoundsProperty = BooleanProperty(limitBounds)

  /**
   * Determines if the target layout view should be limited to the bounds of the camera pane. This
   * will also affect the panning of the camera pane with [pan] and [panBy] as well as zooming.
   *
   * @since 0.10
   */
  var limitBounds: Boolean
    get() = limitBoundsProperty.value
    set(value) {
      limitBoundsProperty.value = value
    }

  /** Upper-left corner of the current scrolling window. */
  @Deprecated("May result in wrong values.", level = DeprecationLevel.WARNING)
  val scroll: Coordinate
    get() = anchorPoint

  internal val anchorPointProperty: Property<Coordinate> = Property(Coordinate())

  internal var anchorPoint: Coordinate
    get() = anchorPointProperty.value
    set(value) {
      if (value.xCoord in 0.0..target.width && value.yCoord in 0.0..target.height) {
        anchorPointProperty.value = value
      }
    }

  internal val panDataProperty: Property<InternalCameraPanData> = Property(InternalCameraPanData())

  internal var panData: InternalCameraPanData
    get() = panDataProperty.value
    set(value) {
      panDataProperty.value = value
    }

  internal val isHorizontalLockedProperty: BooleanProperty = BooleanProperty()

  /**
   * Determines if the camera pane is locked horizontally, which means that you can only scroll
   * vertically.
   *
   * @see interactive
   * @see isVerticalLocked
   * @see isZoomLocked
   *
   * @since 0.10
   */
  var isHorizontalLocked: Boolean
    get() = isHorizontalLockedProperty.value
    set(value) {
      isHorizontalLockedProperty.value = value
    }

  internal val isVerticalLockedProperty: BooleanProperty = BooleanProperty()

  /**
   * Determines if the camera pane is locked vertically, which means that you can only scroll
   * horizontally.
   *
   * @see interactive
   * @see isHorizontalLocked
   * @see isZoomLocked
   *
   * @since 0.10
   */
  var isVerticalLocked: Boolean
    get() = isVerticalLockedProperty.value
    set(value) {
      isVerticalLockedProperty.value = value
    }

  internal val isZoomLockedProperty: BooleanProperty = BooleanProperty()

  /**
   * Determines if the camera pane is locked for zooming, which means that you can only scroll.
   *
   * @see interactive
   * @see isHorizontalLocked
   * @see isVerticalLocked
   *
   * @since 0.10
   */
  var isZoomLocked: Boolean
    get() = isZoomLockedProperty.value
    set(value) {
      isZoomLockedProperty.value = value
    }

  internal val panMouseButtonProperty: Property<MouseButtonType> =
      Property(MouseButtonType.LEFT_BUTTON)

  /**
   * The mouse button that is used to pan the camera pane.
   *
   * @see interactive
   *
   * @since 0.9
   */
  var panMouseButton: MouseButtonType
    get() = panMouseButtonProperty.value
    set(value) {
      panMouseButtonProperty.value = value
    }

  init {
    target.parent = this
  }

  /**
   * Gets invoked whenever the camera pane is zoomed.
   *
   * @see zoom
   *
   * @since 0.10
   */
  var onZoomed: ((Number) -> Unit)? = null

  /**
   * Pans the view of the camera to focus the specified coordinates and zoom level. The coordinates
   * specified represent the center of the view. If [limitBounds] is set to true, the target layout
   * view will be limited to the bounds of the camera pane.
   *
   * @param x The x-coordinate to scroll to.
   * @param y The y-coordinate to scroll to.
   * @param zoom The zoom level to zoom to.
   * @param smooth Whether the pan should be smooth (or instant). Default is true.
   *
   * @see limitBounds
   * @see pan
   * @see panBy
   *
   * @since 0.10
   */
  fun pan(x: Number, y: Number, zoom: Double, smooth: Boolean = true) {
    panData =
        InternalCameraPanData(
            panSmooth = smooth,
            panBy = false,
            panTo = Pair(-x.toDouble(), -y.toDouble()),
            zoom = zoom)
  }

  /**
   * Pans the view of the camera to focus the specified coordinates. The coordinates specified
   * represent the center of the view. If [limitBounds] is set to true, the target layout view will
   * be limited to the bounds of the camera pane.
   *
   * @param x The x-coordinate to scroll to.
   * @param y The y-coordinate to scroll to.
   * @param smooth Whether the pan should be smooth (or instant). Default is true.
   *
   * @see limitBounds
   * @see pan
   * @see panBy
   */
  fun pan(x: Number, y: Number, smooth: Boolean = true) {
    if (panData.zoomOnly) {
      pan(x, y, zoom = panData.zoom!!, smooth = smooth)
    } else {
      panData =
          InternalCameraPanData(
              panSmooth = smooth, panBy = false, panTo = Pair(-x.toDouble(), -y.toDouble()))
    }
  }

  /**
   * Pans the view of the camera by the given offsets and zooms to the specified zoom level. If
   * [limitBounds] is set to true, the target layout view will be limited to the bounds of the
   * camera pane.
   *
   * @param xOffset The amount to pan the view horizontally.
   * @param yOffset The amount to pan the view vertically.
   * @param zoom The zoom level to zoom to.
   * @param smooth Whether the pan should be smooth (or instant). Default is true.
   *
   * @see limitBounds
   * @see pan
   * @see panBy
   *
   * @since 0.10
   */
  fun panBy(xOffset: Number, yOffset: Number, zoom: Double, smooth: Boolean = true) {
    panData =
        InternalCameraPanData(
            panSmooth = smooth,
            panBy = true,
            panTo = Pair(-xOffset.toDouble(), -yOffset.toDouble()),
            zoom = zoom)
  }

  /**
   * Pans the view of the camera by the given offsets. If [limitBounds] is set to true, the target
   * layout view will be limited to the bounds of the camera pane.
   *
   * @param xOffset The amount to pan the view horizontally.
   * @param yOffset The amount to pan the view vertically.
   * @param smooth Whether the pan should be smooth (or instant). Default is true.
   *
   * @see limitBounds
   * @see pan
   * @see panBy
   */
  fun panBy(xOffset: Number, yOffset: Number, smooth: Boolean = true) {
    if (panData.zoomOnly) {
      panBy(xOffset, yOffset, zoom = panData.zoom!!, smooth = smooth)
    } else {
      panData =
          InternalCameraPanData(
              panSmooth = smooth,
              panBy = true,
              panTo = Pair(-xOffset.toDouble(), -yOffset.toDouble()))
    }
  }

  /**
   * Zooms the view of the camera to the specified zoom level. If [limitBounds] is set to true, the
   * target layout view will be limited to the bounds of the camera pane.
   *
   * @param zoom The zoom level to zoom to.
   *
   * @see limitBounds
   */
  private fun zoom(zoom: Double) {
    if (panData.panTo != null) {
      pan(panData.panTo!!.first, panData.panTo!!.second, zoom, panData.panSmooth)
    } else if (panData.panBy) {
      panBy(panData.panTo!!.first, panData.panTo!!.second, zoom, panData.panSmooth)
    } else {
      panData = InternalCameraPanData(zoom = zoom, zoomOnly = true)
    }
  }

  override fun removeChild(component: ComponentView) {
    throw UnsupportedOperationException("This $this ComponentView has no children.")
  }
}
