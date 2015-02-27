package ayai.systems.mapgenerator

import java.util.Random
import scala.collection.immutable.Range

class BadConfigException(msg: String) extends RuntimeException(msg)

object NoiseGenerator {
  def apply(noiseType: String) = new NoiseGenerator(noiseType)
}

class NoiseGenerator(val noiseType: String) {

  //noiseType specifies the algorithm used to generate noise
  //width*height # of tiles
  def getNoise(width: Int, height: Int, frequency: Int): Array[Array[Double]] = {
    // var noise: Array[Array[Int]]
    if (noiseType == "perlin") {
      PerlinNoiseGenerator.getNoise(width, height, frequency)
      // val noise = PerlinNoiseGenerator.getNoise(width, height, frequency)
      // val scaledNoise = rescaleNoise(noise)
      // println(scaledNoise.map(_.mkString(" ")).mkString("\n"))
      // scaledNoise
    }
    else if (noiseType == "blank") {
      Array.fill[Array[Double]](width)(Array.fill[Double](height)(0.0))
    }
    else {
      throw new BadConfigException("Cannot match noise algorithm.")
    }
  }
}