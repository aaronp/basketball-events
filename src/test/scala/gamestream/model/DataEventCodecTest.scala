package gamestream.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

object DataEventCodecTest {

  implicit class RichInt(val i: Int) extends AnyVal {
    def asPaddedBinary: String = {
      i.toBinaryString.reverse.padTo(Int.MaxValue.toBinaryString.size, '0').reverse
    }
  }

  implicit class JsonHelper(val sc: StringContext) extends AnyVal {
    def bin(args: Any*): Int = Integer.parseInt(sc.s(args: _*), 2)
  }

}

class DataEventCodecTest extends AnyWordSpec with Matchers {

  import DataEventCodecTest._

  "DataEventCodec.flags" should {
    "parse pointsScored based on bit offset 0-1" in {
      DataEventCodec.flags.pointsScored(bin"01").points shouldBe 1
      DataEventCodec.flags.pointsScored(bin"10").points shouldBe 2
      DataEventCodec.flags.pointsScored(bin"0").points shouldBe 0
      DataEventCodec.flags.pointsScored(bin"100").points shouldBe 0
      DataEventCodec.flags.pointsScored(bin"11111").points shouldBe 3
    }
    "parse whoScored based on the 3rd bit (bit offset 2)" in {
      DataEventCodec.flags.whoScored(bin"000") shouldBe TeamOne
      DataEventCodec.flags.whoScored(bin"011") shouldBe TeamOne
      DataEventCodec.flags.whoScored(bin"111") shouldBe TeamTwo
      DataEventCodec.flags.whoScored(bin"111") shouldBe TeamTwo
    }
    "parse team one points from bit offset 3 to 10" in {
      DataEventCodec.flags.teamOneTotalPoints(Int.MaxValue).points shouldBe 255
      DataEventCodec.flags.teamOneTotalPoints(bin"0000000000001111111100000000000").points shouldBe 255
      DataEventCodec.flags.teamOneTotalPoints(bin"0000000000011111111110000000000").points shouldBe 255
      DataEventCodec.flags.teamOneTotalPoints(bin"0000000000100000000010000000000").points shouldBe 0
      DataEventCodec.flags.teamOneTotalPoints(bin"0000000000000000000100000000000").points shouldBe 1
      DataEventCodec.flags.teamOneTotalPoints(bin"0000000000000000001000000000000").points shouldBe 2
    }
    "parse team two points from bit offset 3 to 10" in {
      DataEventCodec.flags.teamTwoTotalPoints(Int.MaxValue).points shouldBe 255
      DataEventCodec.flags.teamTwoTotalPoints(bin"0000000000000000000011111111000").points shouldBe 255
      DataEventCodec.flags.teamTwoTotalPoints(bin"0000000000000000000111111111100").points shouldBe 255
      DataEventCodec.flags.teamTwoTotalPoints(bin"0000000000000000001000000000100").points shouldBe 0
      DataEventCodec.flags.teamTwoTotalPoints(bin"0000000000000000000000000001000").points shouldBe 1
      DataEventCodec.flags.teamTwoTotalPoints(bin"0000000000000000000000000011000").points shouldBe 3
    }
  }
  "DataEventCodec" should {
    "parse 0x781002 = 7868418 = 0 000000001111 00000010 00000000 0 10" in {
      val input = 0x781002
      val event = DataEventCodec.fromBinary(input)
      println(event)
    }
  }
}
