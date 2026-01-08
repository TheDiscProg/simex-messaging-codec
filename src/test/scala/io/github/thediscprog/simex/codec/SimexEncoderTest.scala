package io.github.thediscprog.simex.codec

import io.github.thediscprog.simexmessaging.messaging.Datum
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import TestClasses.*

class SimexEncoderTest extends AnyFlatSpec with Matchers {

  it should "encode a simple case class consisting of primitives" in {
    val encoder: SimexEncoder[Primitives] = summon[SimexEncoder[Primitives]]

    val result = encoder.encode(None, primitive)

    result shouldBe primitiveEncoded
  }

  it should "encode enclosing primitive types" in {
    val encoder = summon[SimexEncoder[EnclosedTypes]]

    val result: Vector[Datum] = encoder.encode(None, enclosedTypes)

    result shouldBe enclosedTypesEncoded
  }

  it should "encode subclasses" in {
    val encoder = summon[SimexEncoder[Parent]]

    val result = encoder.encode(None, parent)

    result shouldBe parentEncoded
  }

  it should "encode class with enum" in {
    val test = EnumClass(100L, Shape.Circle(100), "no shape")
    val encoder = summon[SimexEncoder[EnumClass]]

    val result = encoder.encode(None, test)

    result.map(_.field) should contain allElementsOf List("id", "shape", "description")
  }

}
