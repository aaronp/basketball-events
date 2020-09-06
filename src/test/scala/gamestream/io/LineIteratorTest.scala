package gamestream.io

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LineIteratorTest extends AnyWordSpec with Matchers {

  "LineIterator" should {
    "still close the source even when there is no data" in {
      LineIterator(Nil).isClosed() shouldBe true
    }
    "close on complete" in {
      val nonEmpty = LineIterator(List('a', 'b'))
      nonEmpty.isClosed() shouldBe false
      nonEmpty.hasNext shouldBe true
      nonEmpty.next shouldBe "ab"
      nonEmpty.hasNext shouldBe false
      nonEmpty.isClosed() shouldBe true
    }
  }
}
