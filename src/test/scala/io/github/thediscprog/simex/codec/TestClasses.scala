package io.github.thediscprog.simex.codec

import io.github.thediscprog.simexmessaging.messaging.Datum
import io.github.thediscprog.slogic.Xor
import io.github.thediscprog.simex.codec.errors.DecodingError
import io.github.thediscprog.simex.codec.Shape.Circle
import io.github.thediscprog.simex.codec.Shape.Triangle
import io.github.thediscprog.simex.codec.Shape.Rectangle

enum Shape {
  case NoShape
  case Circle(radius: Double)
  case Square(side: Double)
  case Triangle(base: Double, height: Double)
  case Rectangle(width: Double, height: Double)
}

case class Primitives(
    id: Long,
    name: String,
    score: Double,
    isAlive: Boolean,
    schoolingYears: Int,
    short: Short,
    float: Float,
    bigDecimal: BigDecimal,
    bigInt: BigInt,
    char: Char,
    byte: Byte
) derives SimexEncoder,
      SimexDecoder

case class EnclosedTypes(
    option: Option[Int],
    either: Either[Int, String],
    alist: List[Long],
    anArray: Array[String]
) derives SimexEncoder,
      SimexDecoder

case class Child(
    id: Long,
    name: String
) derives SimexEncoder,
      SimexDecoder

case class Parent(
    id: Long,
    name: String,
    child: Child
) derives SimexEncoder,
      SimexDecoder

given shapeEncoder: SimexEncoder[Shape] = SimexEncoder.deriveEnum[Shape]

given shapeDecoder: SimexDecoder[Shape] = new SimexDecoder[Shape] {

  override def decode(data: Vector[Datum]): Either[DecodingError, Shape] = {
    val description = extractDatum(data)
    val (name, args) = parseEnumStrings(description)
    val decodedArgs: List[Either[DecodingError, Double]] = decodeArguments(args)
    name match
      case "NoShape" => Right(Shape.NoShape)
      case "Circle" =>
        if (decodedArgs.size != 1) {
          Left(
            DecodingError.ArgumentMisMatchError(
              s"Shape was a Circle but argument size was ${decodedArgs.size}"
            )
          )
        } else {
          decodedArgs(0).map(r => Shape.Circle(r))
        }
      case "Square" =>
        if (decodedArgs.size != 1) {
          Left(
            DecodingError.ArgumentMisMatchError(
              s"Shape was a Square but argument size was ${decodedArgs.size}"
            )
          )
        } else {
          decodedArgs(0).map(s => Shape.Square(s))
        }
      case "Triangle" =>
        if (decodedArgs.size != 2) {
          Left(
            DecodingError.ArgumentMisMatchError(
              s"Shape was a Triangle but argument size was ${decodedArgs.size}"
            )
          )
        } else {
          val values = decodedArgs(0).flatMap { base =>
            decodedArgs(1).map(height => (base, height))
          }
          values.map(v => Triangle(v._1, v._2))
        }
      case "Rectangle" =>
        if (decodedArgs.size != 2) {
          Left(
            DecodingError.ArgumentMisMatchError(
              s"Shape was a Rectangle but argument size was ${decodedArgs.size}"
            )
          )
        } else {
          val values = decodedArgs(0).flatMap { width =>
            decodedArgs(1).map(height => (width, height))
          }
          values.map(v => Rectangle(v._1, v._2))
        }
      case _ => Left(DecodingError.NoMatchingProperty(name))

  }

  private def decodeArguments(args: List[String]): List[Either[DecodingError, Double]] = {
    val doubleDecoder = summon[StringDecoder[Double]]
    args.map(doubleDecoder.decode)
  }

  private def parseEnumStrings(desc: String): (String, List[String]) = {
    val trimmed = desc.trim()
    val idx = trimmed.indexOf("(")
    if idx == -1 then (trimmed, Nil)
    else {
      val name = trimmed.substring(0, idx)
      val args = trimmed
        .substring(idx + 1, trimmed.length() - 1)
        .split(",")
        .map(_.trim)
        .toList
      (name, args)
    }
  }

  private def extractDatum(data: Vector[Datum]): String = {
    val datum = data.headOption.getOrElse(throw new RuntimeException(s"Enum data size was zero"))
    if (datum.value.isLeft) {
      datum.value.getLeft match
        case Some(desc) => desc
        case None => throw new RuntimeException(s"There was no encoding in Xor.Left")
    } else {
      throw new RuntimeException(s"Our enums are encoded in the Xor.Left")
    }
  }

}

case class EnumClass(id: Long, shape: Shape, description: String) derives SimexEncoder, SimexDecoder

object TestClasses {
  val primitive = Primitives(
    id = 100L,
    name = "ChildA",
    score = 88.9,
    isAlive = true,
    schoolingYears = 10,
    short = 55,
    float = 123.789f,
    bigDecimal = 123456789.01234,
    bigInt = 0x123a4b,
    char = 'C',
    byte = 0x08
  )

  val enclosedTypes = EnclosedTypes(
    option = Option(101),
    either = Right("Result is 102"),
    alist = List(1L, 0L, 3L),
    anArray = Array("one", "two")
  )

  val child = Child(200L, "Jack")
  val parent = Parent(100L, "John", child)

  val enumClass =
    EnumClass(
      id = 100L,
      shape = Shape.Rectangle(10, 15),
      description = "Rectangle with 150 square units"
    )

  val primitiveEncoded = Vector(
    Datum("id", None, Xor.applyLeft("100")),
    Datum("name", None, Xor.applyLeft("ChildA")),
    Datum("score", None, Xor.applyLeft("88.9")),
    Datum("isAlive", None, Xor.applyLeft("true")),
    Datum("schoolingYears", None, Xor.applyLeft("10")),
    Datum("short", None, Xor.applyLeft("55")),
    Datum("float", None, Xor.applyLeft("123.789")),
    Datum("bigDecimal", None, Xor.applyLeft("123456789.01234")),
    Datum("bigInt", None, Xor.applyLeft("1194571")),
    Datum("char", None, Xor.applyLeft("C")),
    Datum("byte", None, Xor.applyLeft("8"))
  )

  val enclosedTypesEncoded = Vector(
    Datum("option", None, Xor.applyLeft("101")),
    Datum("either", None, Xor.applyLeft("Result is 102")),
    Datum("alist", None, Xor.applyLeft("[1,0,3]")),
    Datum("anArray", None, Xor.applyLeft("[one,two]"))
  )

  val parentEncoded = Vector(
    Datum("id", None, Xor.applyLeft("100")),
    Datum("name", None, Xor.applyLeft("John")),
    Datum(
      "child",
      None,
      Xor.applyRight(
        Vector(
          Datum("id", None, Xor.applyLeft("200")),
          Datum("name", None, Xor.applyLeft("Jack"))
        )
      )
    )
  )

  val enumClassEncoded = Vector(
    Datum("id", None, Xor.applyLeft("100")),
    Datum("shape", None, Xor.applyLeft("Rectangle(10, 15)")),
    Datum("description", None, Xor.applyLeft("Rectangle with 150 square units"))
  )
}
