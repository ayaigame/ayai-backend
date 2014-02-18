package ayai.maps

class TileFeature() {
  case class Walkable() extends TileFeature()
  case class Door() extends TileFeature()
}
