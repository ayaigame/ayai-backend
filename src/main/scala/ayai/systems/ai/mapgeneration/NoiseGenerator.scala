package ayai.maps.generator

import java.util.Random

class BadConfigException(msg: String) extends RuntimeException(msg)

object NoiseGenerator {

  //noiseType specifies the algorithm used to generate noise
  //width*height # of tiles
  def getNoise(noiseType: String, width: Int, height: Int): Array[Array[Double]] = {
    if(noiseType == "perlin") {
      PerlinNoiseGenerator.getNoise(width, height)
    }
    else {
      throw new BadConfigException("Cannot match noise algorithm.")
    }
  }

}