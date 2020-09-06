package gamestream.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameStateTest extends AnyWordSpec with Matchers {

  "GameState.validating" should {
    "return an error if any events produce an invalid state" in {
      val events = TestScenario.sample2.states.drop(5).map(_.event)
      val Left(EventError(invalidEvent, 5, previousApi, detail)) = GameState.validating(events.iterator)
      detail shouldBe "Expected the score to be (7,13) after DataEvent(PointScored(2),TeamTwo,TotalScore(8),TotalScore(13),03:15), but it was (8,13)"
      invalidEvent shouldBe DataEvent(PointScored(2), TeamTwo, TotalScore(8), TotalScore(13), ElapsedMatchTime.fromMinutesSeconds(3, 15))

      previousApi.lastEvent shouldBe Some(DataEvent(PointScored(2), TeamTwo, TotalScore(7), TotalScore(11), ElapsedMatchTime.fromMinutesSeconds(2, 23)))
    }
    "return the api if the events are all valid" in {
      val Right(api) = GameState.validating(TestScenario.sample1.states.map(_.event).iterator)
      withClue(api.asAsciiTable) {
        api.team1Score shouldBe 27
        api.team2Score shouldBe 29
      }
    }
  }
  "GameState" should {
    "parse the ints as expected" in {
      val scenario = TestScenario.sample2
      scenario.states.foreach { next =>
        next.inputText shouldBe s"0x${next.parsedBinaryMsg.toHexString}"
      }
    }
  }
  "GameState.deduplicate" should {
    "remove duplicate events from the insert operation" in {
      val e1 = DataEvent(PointScored(2), TeamTwo, TotalScore(4), TotalScore(9), ElapsedMatchTime(10))
      val e2 = DataEvent(PointScored(2), TeamOne, TotalScore(2), TotalScore(9), ElapsedMatchTime(20))

      val updated = GameState.empty
        .update(e1) // <-- start with one event
        .newState.update(e2)
        .newState.update(e1) // <-- add our duplicate event

      updated.subsequentEvents.size shouldBe 1
      updated.newState.eventsMostRecentFirst.size shouldBe 3

      updated.deduplicate.subsequentEvents.size shouldBe 0
      updated.deduplicate.newState.eventsMostRecentFirst.size shouldBe 2
    }
  }
  "GameState.insertInto" should {
    "insert in order" in {
      GameState.insertInto(3, List(1, 2, 4, 5)) shouldBe(List(1, 2), 3, List(4, 5))
      GameState.insertInto(3, List()) shouldBe(Nil, 3, Nil)
      GameState.insertInto(1, List(2)) shouldBe(Nil, 1, List(2))
      GameState.insertInto(2, List(1)) shouldBe(List(1), 2, Nil)
    }
  }
}