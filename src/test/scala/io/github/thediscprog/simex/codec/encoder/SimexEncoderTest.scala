package io.github.thediscprog.simex.codec.encoder

import io.github.thediscprog.simexmessaging.messaging.Datum
import io.github.thediscprog.slogic.Xor
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SimexEncoderTest extends AnyFlatSpec with Matchers {

  "SimexEncoder" should "encode a simple case class consisting of primitives" in {
    val encoder: SimexEncoder[Primitives] = summon[SimexEncoder[Primitives]]
    val childA = Primitives(
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

    val result = encoder.encode(None, childA)

    result shouldBe Vector(
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
  }

  "SimexEncoder" should "encode enclosing primitive types" in {
    val encoder = summon[SimexEncoder[EnclosedTypes]]
    val toEncode = EnclosedTypes(
      option = Option(101),
      either = Right("Result is 102"),
      alist = List(1L, 0L, 3L),
      anArray = Array("one", "two")
    )
    val result: Vector[Datum] = encoder.encode(None, toEncode)

    result shouldBe Vector(
      Datum("option", None, Xor.applyLeft("101")),
      Datum("either", None, Xor.applyLeft("Result is 102")),
      Datum("alist", None, Xor.applyLeft("[1,0,3]")),
      Datum("anArray", None, Xor.applyLeft("[one,two]"))
    )
  }

  "SimexEncoder" should "encode subclasses" in {
    val child = Child(200L, "Jack")
    val parent = Parent(100L, "John", child)
    val encoder = summon[SimexEncoder[Parent]]

    val result = encoder.encode(None, parent)

    result shouldBe Vector(
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
  }

  "SimexEncoder" should "encode class with enum" in {
    given shapeEncoder: SimexEncoder[Shape] = SimexEncoder.deriveEnum[Shape]

    case class EnumClass(id: Long, shape: Shape, description: String) derives SimexEncoder
    val test = EnumClass(100L, Shape.Circle(100), "no shape")
    val encoder = summon[SimexEncoder[EnumClass]]
    val result = encoder.encode(None, test)

    result.map(_.field) should contain allElementsOf List("id", "shape", "description")
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
  ) derives SimexEncoder

  case class EnclosedTypes(
      option: Option[Int],
      either: Either[Int, String],
      alist: List[Long],
      anArray: Array[String]
  ) derives SimexEncoder

  case class Child(
      id: Long,
      name: String
  ) derives SimexEncoder

  case class Parent(
      id: Long,
      name: String,
      child: Child
  ) derives SimexEncoder
}
