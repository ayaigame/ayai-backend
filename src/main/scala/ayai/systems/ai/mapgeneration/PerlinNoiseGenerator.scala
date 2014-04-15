package ayai.systems.mapgenerator

import java.util.Random

object PerlinNoiseGenerator {

  //returns noise with values from 0 to 1.
  def getScaledNoise(width: Int, height: Int, frequency: Int): Array[Array[Double]] = {
    val noise = getNoise(width, height, frequency)
    def getMax(row: Array[Double]): Double = {row reduceLeft (_ max _)}
    def getMin(row: Array[Double]): Double = {row reduceLeft (_ min _)}
    val noiseMax = noise map getMax reduceLeft (_ max _)
    val noiseMin = noise map getMin reduceLeft (_ min _)
    val absoluteMin = noiseMin * -1

    def scalePixel(pixelVal: Double) = {(pixelVal + absoluteMin) / (noiseMax + absoluteMin)}
    noise map (_ map scalePixel)
  }

  //width*height # of tiles
  def getNoise(width: Int, height: Int, frequency: Int): Array[Array[Double]] = {
    val latticeX = 10
    val latticeY = 10
    // println(s"frequency: $frequency")
    // val latticeX = width / frequency
    // val latticeY = height / frequency
    val rand = new Random(5);

    def getVector = {Array.fill[Float](2)(rand.nextFloat())}

    val allGradients = Array.fill[Array[Float]](width/latticeX+1, height/latticeY+1)(getVector)

    //Returns four displacement vectors, one for each corner
    def getDisplacements(x: Int, y: Int): Array[Array[Int]] = {
      val bottomLeftX = (x/latticeX)*latticeX
      val bottomLeftY = (y/latticeY)*latticeY

      //Start at bottomLeft, go clockwise
      Array(Array(bottomLeftX - x, bottomLeftY - y),
            Array(bottomLeftX - x, bottomLeftY + latticeY - y),
            Array(bottomLeftX + latticeX - x, bottomLeftY + latticeY - y),
            Array(bottomLeftX + latticeX - x, bottomLeftY - y))
    }

    def getGradientVectors(x: Int, y: Int): Array[Array[Float]] = {
      val bottomLeftX = x/latticeX
      val bottomLeftY = y/latticeY
      // println(s"bottomLeftX: $bottomLeftX, bottomLeftY: $bottomLeftY")

      Array(allGradients(bottomLeftX)(bottomLeftY),
            allGradients(bottomLeftX)(bottomLeftY+1),
            allGradients(bottomLeftX+1)(bottomLeftY+1),
            allGradients(bottomLeftX+1)(bottomLeftY))
    }

    def interpolate(points: Array[Float]): Double = {

      return ((3 * math.pow(points(0), 2.0)) - (2 * math.pow(points(0), 3.0))) *
             ((3 * math.pow(points(1), 2.0)) - (2 * math.pow(points(1), 3.0))) *
             ((3 * math.pow(points(2), 2.0)) - (2 * math.pow(points(2), 3.0))) *
             ((3 * math.pow(points(3), 2.0)) - (2 * math.pow(points(3), 3.0)))
    }

    def dotProduct(ints: Array[Int], floats: Array[Float]): Float = {
      ints(0) * floats(0) + ints(1) * floats(1)
    }

    //Takes displacement vectors and gradient vectors for a single point
    //Returns gradient values
    def getGradientValues(x: Int, y: Int): Array[Float] = {
      (getDisplacements(x, y), getGradientVectors(x, y)).zipped map (dotProduct(_, _))
    }


    //println(getGradientValues(5, 6).deep.mkString("\n"))

    var noise = Array.fill[Array[Double]](width)(Array.fill[Double](height)(0.0f))


    for(i <- 0 to width-1) {
      for(j <- 0 to height-1) {
        // var x = i / latticeX
        // var y = j / latticeY

        // print(allGradients(i / latticeX)(j / latticeY)(0), allGradients(i / latticeX)(j / latticeY)(1) + ": ")
        // println(x + ", " + y)

        val gradientValues = getGradientValues(i, j)
        noise(i)(j) = interpolate(gradientValues)

      }
    }

    return noise
    //m rows, n columns
    //higher number of squares is higher frequency
    //f(x, y) = (3x^2 - 2x^3)(3y^2 - 2y^3)
  }
}