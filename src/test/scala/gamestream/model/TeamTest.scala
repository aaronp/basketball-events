package gamestream.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TeamTest extends AnyWordSpec with Matchers {

  "Team.apply" should {
    "fail on invalid input" in {
      val e = intercept[Exception](Team(3))
      e.getMessage should include ("Invalid team index: 3")
    }
  }
  "Team.unapply" should {
    "match on 0 and 1" in {
      0 match {
        case Team(TeamOne) => // ok
      }
      1 match {
        case Team(TeamTwo) => // ok
      }
      2 match {
        case Team(_) => fail("shouldn't match")
        case _ => // ok
      }
    }
  }
  "Team.equals" should {
    "work" in {
      TeamOne shouldEqual TeamOne
      TeamTwo shouldEqual TeamTwo
      TeamOne should not equal TeamTwo
      TeamTwo should not equal TeamOne
    }
  }
}
