package graphics

case class Color(red: Int, green: Int, blue: Int) {
  def toHex: String = "#%02X%02X%02X".format(red, green, blue)

  def toRGB: (Double, Double, Double) = (red / 255.0, green / 255.0, blue / 255.0)
}
