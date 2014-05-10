package ayai.systems.mapgenerator

import java.util.Random
import scala.collection.immutable.Range

class BadConfigException(msg: String) extends RuntimeException(msg)

object NoiseGenerator {

  //noiseType specifies the algorithm used to generate noise
  //width*height # of tiles
  def getNoise(noiseType: String, width: Int, height: Int, frequency: Int): Array[Array[Int]] = {
    // var noise: Array[Array[Int]]
    if(noiseType == "perlin") {
      val noise = PerlinNoiseGenerator.getScaledNoise(width, height, frequency)
      val scaledNoise = rescaleNoise(noise)
      println(scaledNoise.map(_.mkString(" ")).mkString("\n"))
      scaledNoise
    }
    else if(noiseType == "blank") {
      Array.fill[Array[Int]](width)(Array.fill[Int](height)(0))
    }
    else {
      throw new BadConfigException("Cannot match noise algorithm.")
    }

  }

  def rescaleNoise(noise: Array[Array[Double]]) : Array[Array[Int]] = {
    def getMax(row: Array[Double]): Double = {row reduceLeft (_ max _)}
    def getMin(row: Array[Double]): Double = {row reduceLeft (_ min _)}
    val noiseMax = noise map getMax reduceLeft (_ max _)
    val noiseMin = noise map getMin reduceLeft (_ min _)

    val range = noiseMax - noiseMin

    val numLevels = 3
    val brackets = Range.Double(noiseMin, noiseMax, range/(numLevels - 1))

    def rescaleTile(x: Double): Int = {
      //Find the bracket the tile falls into
      brackets.zipWithIndex.find(_._1 >= x).getOrElse((0.0,0))._2
    }

    def rescaleRow(row: Array[Double]): Array[Int] = {
      row map rescaleTile
    }

    noise map rescaleRow
 }
}