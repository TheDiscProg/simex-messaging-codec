package io.github.thediscprog.simex.codec.encoder

enum Shape {
  case NoShape
  case Circle(radius: Double)
  case Square(side: Double)
  case Triangle(base: Double, height: Double)
  case Rectangle(width: Double, height: Double)
}
