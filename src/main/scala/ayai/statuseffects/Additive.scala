package ayai.statuseffects

case class Multiplier(multiplier: Double) {

  /**
   * Examples - if you want to add %5 of something do value(1) * .05
   * Or if you want to subtract %5 of something do value(1) * -.05
  */
  def process(value: Int): Int = {
    (multiplier * value).toInt
  }
}
