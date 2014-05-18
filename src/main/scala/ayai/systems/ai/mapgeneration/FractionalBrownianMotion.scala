package ayai.systems.mapgenerator

import scala.math.{min, log, floor}

import java.util.Random

object FractionalBrownianMotionGenerator {
  def apply(noiseGenerator: NoiseGenerator) = new FractionalBrownianMotionGenerator(noiseGenerator)
}

class FractionalBrownianMotionGenerator(val noiseGenerator: NoiseGenerator) {

  def _addLayers(noiseMapA: Array[Array[Double]], noiseMapB: Array[Array[Double]], amplitude: Double): Array[Array[Double]] = {
    if(noiseMapA.size == noiseMapB.size && noiseMapA(0).size == noiseMapB(0).size) {
      for(i <- 0 until noiseMapA.size) {
        for(j <- 0 until noiseMapA(0).size) {
          noiseMapA(i)(j) = noiseMapA(i)(j) + (noiseMapB(i)(j) * amplitude)
        }
      }
    }
    noiseMapA
  }

  //width*height # of tiles
  def getNoise(width: Int, height: Int): Array[Array[Double]] = {
    var frequency = 4
    var amplitude = 0.95

    //The first two octaves are skipped, so subtract 2
    var octaves = floor(log(min(width, height))/log(2)).toInt - 2
    // var octaves = 7

    var totalNoise = noiseGenerator.getNoise(width, height, 1)

    for( i <- 0 to octaves) {
      var noiseLayer = noiseGenerator.getNoise(width, height, frequency)

      totalNoise = _addLayers(totalNoise, noiseLayer, amplitude)

      frequency = frequency * 2
      amplitude = amplitude * amplitude
    }
    totalNoise
  }

}