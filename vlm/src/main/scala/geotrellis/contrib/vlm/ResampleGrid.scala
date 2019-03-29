/*
 * Copyright 2018 Azavea
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

package geotrellis.contrib.vlm

import geotrellis.raster.{RasterExtent, GridExtent}
import geotrellis.raster.reproject.Reproject
import spire.math.Integral

sealed trait ResampleGrid[N] {
  // this is a by name parameter, as we don't need to call the source in all ResampleGrid types
  def apply(source: => GridExtent[N]): GridExtent[N]
}

case class Dimensions[N: Integral](cols: N, rows: N) extends ResampleGrid[N] {
  def apply(source: => GridExtent[N]): GridExtent[N] =
    new GridExtent(source.extent, cols, rows)
}

case class TargetGrid[N: Integral](grid: GridExtent[Long]) extends ResampleGrid[N] {
  def apply(source: => GridExtent[N]): GridExtent[N] =
    grid.createAlignedGridExtent(source.extent).toGridType[N]
}

case class TargetRegion[N: Integral](region: GridExtent[N]) extends ResampleGrid[N] {
  def apply(source: => GridExtent[N]): GridExtent[N] =
    region
}


object ResampleGrid {
  /** Used when somebody is trying to reproject to same CRS, pick-out the grid */
  private[vlm] def fromReprojectOptions(options: Reproject.Options): Option[ResampleGrid[Long]] ={
    if (options.targetRasterExtent.isDefined) {
      Some(TargetRegion(options.targetRasterExtent.get.toGridType[Long]))
    } else if (options.parentGridExtent.isDefined) {
      Some(TargetGrid(options.parentGridExtent.get))
    } else if (options.targetCellSize.isDefined) {
      ??? // TODO: convert from CellSize to Column count based on ... something
    } else {
      None
    }
  }
}