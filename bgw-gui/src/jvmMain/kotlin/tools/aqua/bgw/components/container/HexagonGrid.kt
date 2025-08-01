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

package tools.aqua.bgw.components.container

import kotlin.math.sqrt
import tools.aqua.bgw.components.container.HexagonGrid.CoordinateSystem
import tools.aqua.bgw.components.gamecomponentviews.HexagonView
import tools.aqua.bgw.core.HexOrientation
import tools.aqua.bgw.visual.Visual

private typealias HexCoordinate = Pair<Int, Int>

/**
 * A class representing a grid of hexagons.
 *
 * @param posX The x-coordinate of the hexagon grid's position on the screen. Default is 0.
 * @param posY The y-coordinate of the hexagon grid's position on the screen. Default is 0.
 * @param width The width of the hexagon grid. It grows dynamically by the amount hexagons in it.
 * @param height The height of the hexagon grid. It grows dynamically by the amount hexagons in it.
 * @param visual The visual representation of the hexagon grid. Default is an empty visual.
 * @param coordinateSystem The coordinate system to use for the grid. Default is
 * [CoordinateSystem.OFFSET].
 * @param orientation The orientation of the hexagons in the grid. Default is
 * [HexOrientation.POINTY_TOP].
 *
 * @see CoordinateSystem
 * @see HexOrientation
 * @see HexagonView
 * @see Visual
 * @see GameComponentContainer
 *
 * @since 0.8
 */
class HexagonGrid<T : HexagonView>(
    posX: Number = 0,
    posY: Number = 0,
    width: Number = 0,
    height: Number = 0,
    visual: Visual = Visual.EMPTY,

    /** The coordinate system to use for the grid. Default is [CoordinateSystem.OFFSET]. */
    val coordinateSystem: CoordinateSystem = CoordinateSystem.OFFSET,

    /**
     * The orientation of the hexagons in the grid. Default is [HexOrientation.POINTY_TOP].
     * @since 0.10
     */
    var orientation: HexOrientation = HexOrientation.POINTY_TOP
) :
    GameComponentContainer<T>(
        posX = posX, posY = posY, width = width, height = height, visual = visual) {

  /** A mutable map that stores the hexagons in the grid. */
  internal val map: MutableMap<HexCoordinate, T> = mutableMapOf()

  init {
    observableComponents.setInternalListenerAndInvoke(emptyList()) { _, _ ->
      layout(coordinateSystem)
    }
  }

  /**
   * Gets the hexagon at the specified column index and row index.
   *
   * @param columnIndex The column index of the hexagon.
   * @param rowIndex The row index of the hexagon.
   * @return The hexagon at the specified coordinates, or null if no hexagon is found.
   *
   * @see components
   */
  operator fun get(columnIndex: Int, rowIndex: Int): T? = map[columnIndex to rowIndex]

  /**
   * Sets the hexagon at the specified column index and row index.
   *
   * @param columnIndex The column index of the hexagon.
   * @param rowIndex The row index of the hexagon.
   * @param component The hexagon component to set.
   *
   * @see components
   */
  operator fun set(columnIndex: Int, rowIndex: Int, component: T) {
    map[columnIndex to rowIndex]?.run {
      observableComponents.remove(this)
      onRemove?.invoke(this)
    }
    component.orientation = orientation
    component.parent = this
    map[columnIndex to rowIndex] = component
    observableComponents.add(component)
    onAdd?.invoke(component)
  }

  /**
   * Returns all hexagons in the grid as a map from [HexCoordinate] to [T].
   *
   * @return A map from [HexCoordinate] to [T] containing all hexagons in the grid.
   *
   * @see components
   *
   * @since 0.10
   */
  fun getCoordinateMap(): Map<HexCoordinate, T> {
    return map
  }

  /**
   * Removes the hexagon at the specified column index and row index.
   *
   * @param columnIndex The column index of the hexagon.
   * @param rowIndex The row index of the hexagon.
   *
   * @see components
   *
   * @since 0.10
   */
  fun remove(columnIndex: Int, rowIndex: Int) {
    map[columnIndex to rowIndex]?.run {
      observableComponents.remove(this)
      onRemove?.invoke(this)
    }
  }

  /**
   * Internal function to lay out the hexagons in the grid based on the specified coordinate system.
   *
   * @param coordinateSystem The coordinate system to use for the layout.
   */
  private fun layout(coordinateSystem: CoordinateSystem) {
    var minX = Double.MAX_VALUE
    var minY = Double.MAX_VALUE

    var maxX = Double.MIN_VALUE
    var maxY = Double.MIN_VALUE

    map.forEach { (coords, hexagon) ->
      val (x, y) = coords
      val (q, r) =
          when (coordinateSystem) {
            CoordinateSystem.OFFSET -> {
              if (orientation == HexOrientation.POINTY_TOP) x to y else y to x
            }
            CoordinateSystem.AXIAL -> {
              if (orientation == HexOrientation.POINTY_TOP) x + (y - (y and 1)) / 2 to y
              else y + (x - (x and 1)) / 2 to x
            }
          }
      with(hexagon) {
        if (orientation == HexOrientation.POINTY_TOP) {
          hexagon.orientation = HexOrientation.POINTY_TOP
          val hexWidth = width / 2 * sqrt(3.0)
          inParentPosXProperty.setSilent(hexWidth * q + if (r % 2 == 0) 0.0 else hexWidth / 2)
          inParentPosYProperty.setSilent(height * r - r * height / 4)

          if (inParentPosXProperty.value < minX) minX = inParentPosXProperty.value
          if (inParentPosYProperty.value < minY) minY = inParentPosYProperty.value

          if (inParentPosXProperty.value + hexWidth > maxX)
              maxX = inParentPosXProperty.value + hexWidth
          if (inParentPosYProperty.value + height > maxY) maxY = inParentPosYProperty.value + height
        } else {
          hexagon.orientation = HexOrientation.FLAT_TOP
          val hexHeight = height / 2 * sqrt(3.0)
          inParentPosYProperty.setSilent(hexHeight * q + if (r % 2 == 0) 0.0 else hexHeight / 2)
          inParentPosXProperty.setSilent(width * r - r * width / 4)

          if (inParentPosXProperty.value < minX) minX = inParentPosXProperty.value
          if (inParentPosYProperty.value < minY) minY = inParentPosYProperty.value

          if (inParentPosXProperty.value + width > maxX) maxX = inParentPosXProperty.value + width
          if (inParentPosYProperty.value + hexHeight > maxY)
              maxY = inParentPosYProperty.value + hexHeight
        }
      }
    }

    widthProperty.setSilent(maxX - minX)
    heightProperty.setSilent(maxY - minY)

    components.forEach {
      it.inParentPosXProperty.setSilent(it.inParentPosXProperty.value - minX)
      it.inParentPosYProperty.setSilent(it.inParentPosYProperty.value - minY)
    }
  }

  override fun T.onRemove() {
    map.forEach { (coords, hexagon) ->
      if (hexagon == this) {
        map.remove(coords)
        return
      }
    }
  }

  override fun T.onAdd() {
    this.parent = this@HexagonGrid
  }

  /** Enumeration class representing the coordinate system options for the hexagon grid. */
  enum class CoordinateSystem {
    OFFSET,
    AXIAL
  }
}
