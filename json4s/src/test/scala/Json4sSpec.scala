package mcoffin.maester

import instances._
import json4s._

import java.math.BigInteger

import org.json4s._
import org.scalatest._
import org.typelevel.scalatest._

import scalaz._
import Scalaz._

class Json4sSpec extends FlatSpec with Matchers with DisjunctionMatchers {
  type JTry[A] = \/[DecodeException[JValue], A]

  private[this] def decodeAs[A](v: JValue)(implicit decoder: JDecode[A]): JTry[A] = {
    Encoding.decode[JTry, DecodeException[JValue], JValue, A](v)
  }

  "Default json decoding" should "decode integers" in {
    val expected: Int = 3
    val json = JInt(new BigInt(java.math.BigInteger.valueOf(expected)))
    val result = Encoding.decode[JTry, DecodeException[JValue], JValue, Int](json)
    result should beRight(expected)
  }

  it should "fail integer parsing but pass long parsing for too large integers" in {
    val bigIntegerExpected: Long = (Integer.MAX_VALUE:Long) + 1
    val json = JInt(new BigInt(BigInteger.valueOf(bigIntegerExpected)))
    decodeAs[Int](json) should be(left)
    decodeAs[Long](json) should beRight(bigIntegerExpected)
  }

  it should "fail long parsing but pass BigInt parsing for too large longs" in {
    val bigLongExpected: BigInt = BigInt(java.lang.Long.MAX_VALUE) + BigInt(1)
    val json = JInt(bigLongExpected)
    decodeAs[Long](json) should be(left)
    decodeAs[BigInt](json) should be(right)
  }
}
