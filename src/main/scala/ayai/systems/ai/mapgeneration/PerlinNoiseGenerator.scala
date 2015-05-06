package ayai.systems.mapgenerator

import java.util.Random

//Change this to extend noisegenerator instead
object PerlinNoiseGenerator {
  //Keep seed constant for all noise generation.
  val seed = System.currentTimeMillis()
  println(s"The seed is: $seed")
  val rand = new Random(seed)
  // val rand = new Random(25636);
  //Good seed = 1400572427905

  ///width*height # of tiles
  def getNoise(width: Int, height: Int, frequency: Int): Array[Array[Double]] = {
    val latticeX = width/frequency
    val latticeY = height/frequency

    def getVector = {Array.fill[Double](2)(rand.nextDouble())}

    val allGradients = Array.fill[Array[Double]](width / latticeX + 2, height / latticeY + 2)(getVector)

    //Returns four displacement vectors, one for each corner
    def getDisplacements(x: Int, y: Int): Array[Array[Int]] = {
      val bottomLeftX = (x/latticeX)*latticeX
      val bottomLeftY = (y/latticeY)*latticeY

      //Start at bottomLeft, go clockwise
      Array(Array(x - bottomLeftX, y - bottomLeftY),
            Array(x - bottomLeftX, y - (bottomLeftY + latticeY)),
            Array(x -(bottomLeftX + latticeX), y - (bottomLeftY + latticeY)),
            Array(x - (bottomLeftX + latticeX), y - bottomLeftY))
    }

    def getGradientVectors(x: Int, y: Int): Array[Array[Double]] = {
      val bottomLeftX = x / latticeX
      val bottomLeftY = y / latticeY

      Array(allGradients(bottomLeftX)(bottomLeftY),
            allGradients(bottomLeftX)(bottomLeftY + 1),
            allGradients(bottomLeftX + 1)(bottomLeftY + 1),
            allGradients(bottomLeftX + 1)(bottomLeftY))
    }

    def interpolate(x: Int, y: Int, gradientValues: Array[Double]): Double = {
      val displacements = getDisplacements(x, y)

      val deltaX = (1.0 * displacements(0)(0)) / latticeX
      val deltaY = (1.0 * displacements(0)(1)) / latticeY

      val weightX = (3 * math.pow(deltaX, 2.0)) - (2 * math.pow(deltaX, 3.0))
      val v0 = gradientValues(0) + weightX * (gradientValues(3) - gradientValues(0))
      val v1 = gradientValues(1) + weightX * (gradientValues(2) - gradientValues(1))

      val weightY = (3 * math.pow(deltaY, 2.0)) - (2 * math.pow(deltaY, 3.0))
      v0 + weightY * (v1 - v0)
    }

    def dotProduct(ints: Array[Int], doubles: Array[Double]): Double = {
      ints(0) * doubles(0) + ints(1) * doubles(1)
    }

    //Takes displacement vectors and gradient vectors for a single point
    //Returns gradient values
    def getGradientValues(x: Int, y: Int): Array[Double] = {
      (getDisplacements(x, y), getGradientVectors(x, y)).zipped map (dotProduct(_, _))
    }

    val noise = Array.fill[Array[Double]](width)(Array.fill[Double](height)(0.0f))

     for(i <- 0 to width-1) {
      for(j <- 0 to height-1) {
        val gradientValues = getGradientValues(i, j)
        noise(i)(j) = interpolate(i, j, gradientValues)

      }
    }

    return noise
  }
}