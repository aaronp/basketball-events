package gamestream

import java.nio.file.Path

import gamestream.model._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameApiTest extends AnyWordSpec with Matchers {

  "GameApi.hexStrToInt" should {
    "parse 0x801002" in {
      GameApi.hexStrToInt("0x801002") shouldBe 8392706
      GameApi.hexStrToInt("801002") shouldBe 8392706
    }
  }
  "GameApi" should {
    val event1 = DataEvent(PointScored(2), TeamTwo, TotalScore(100), TotalScore(200), ElapsedMatchTime(12))
    val event2 = DataEvent(PointScored(3), TeamOne, TotalScore(103), TotalScore(200), ElapsedMatchTime(13))

    val testApi = GameApi.identity(event1 :: event2 :: Nil)
    "return the lastEvent" in {
      testApi.lastEvent shouldBe Some(event2)
      GameApi.identity(Nil).lastEvent shouldBe None
    }
    "return the lastEvents" in {
      testApi.lastEvents(0).size shouldBe 0
      testApi.lastEvents(1) should contain only (event2)
      testApi.lastEvents(2).toList should contain inOrderOnly(event1, event2)
      testApi.lastEvents(3).toList should contain inOrderOnly(event1, event2)
      GameApi.identity(Nil).lastEvents(10).size shouldBe 0
    }
    "return the team1Score and team2Score" in {
      testApi.team1Score shouldBe 103
      testApi.team2Score shouldBe 200
      GameApi.identity(Nil).team1Score shouldBe 0
      GameApi.identity(Nil).team2Score shouldBe 0
    }
    "return mostRecentPointsScored" in {
      testApi.mostRecentPointsScored shouldBe 3
      GameApi.identity(Nil).mostRecentPointsScored shouldBe 0
    }
    "return lastTeamToScore" in {
      testApi.lastTeamToScore shouldBe Some(TeamOne)
      GameApi.identity(Nil).lastTeamToScore shouldBe None
    }
  }

  "GameApi.format" should {
    "render the sample2 events as a table" in {
      verify(TestScenario.sample2Path) {
        """time | event          | team1 score | team2 score
          >00:15 | one scored   2 |           2 | 0
          >00:28 | two scored   2 |           2 | 2
          >00:33 | two scored   0 |           2 | 2
          >01:00 | two scored   3 |           2 | 5
          >01:15 | one scored   2 |           4 | 5
          >01:37 | two scored   2 |           4 | 7
          >01:53 | two scored   2 |           4 | 9
          >01:53 | two scored   2 |           4 | 9
          >02:02 | one scored   3 |           7 | 9
          >02:23 | two scored   2 |           7 | 11
          >02:48 | one scored   1 |           8 | 11
          >03:15 | two scored   2 |           8 | 13
          >03:35 | one scored   2 |          10 | 13
          >03:54 | two scored   0 |          14 | 13
          >04:11 | two scored   2 |          10 | 15
          >04:39 | one scored   2 |          12 | 15
          >04:55 | two scored   2 |          12 | 17
          >05:22 | two scored   2 |          12 | 19
          >06:09 | one scored   2 |          14 | 20
          >06:33 | two scored   2 |          14 | 22
          >06:48 | two scored   2 |          14 | 24
          >07:06 | one scored   2 |          16 | 24
          >07:26 | two scored   2 |          16 | 26
          >07:36 | one scored   3 |          19 | 26
          >07:52 | two scored   2 |          19 | 30
          >08:25 | one scored   2 |          21 | 28
          >08:53 | two scored   1 |          21 | 29
          >09:20 | one scored   2 |          23 | 29
          >09:39 | one scored   2 |          25 | 29
          >25:00 | one scored   3 |         232 | 234""".stripMargin('>')
      }
    }

    "render the sample1 events as a table" in verify(TestScenario.sample1Path) {

      """time | event          | team1 score | team2 score
        >00:16 | one scored   2 |           2 | 0
        >00:31 | two scored   2 |           2 | 2
        >00:59 | two scored   3 |           2 | 5
        >01:13 | one scored   2 |           4 | 5
        >01:32 | two scored   2 |           4 | 7
        >01:45 | two scored   2 |           4 | 9
        >01:59 | one scored   3 |           7 | 9
        >02:21 | two scored   2 |           7 | 11
        >02:46 | one scored   1 |           8 | 11
        >03:20 | two scored   2 |           8 | 13
        >03:39 | one scored   2 |          10 | 13
        >03:56 | one scored   2 |          12 | 13
        >04:23 | two scored   2 |          12 | 15
        >04:45 | one scored   2 |          14 | 15
        >04:54 | two scored   2 |          14 | 17
        >05:18 | two scored   2 |          14 | 19
        >05:46 | two scored   1 |          14 | 20
        >06:07 | one scored   2 |          16 | 20
        >06:23 | two scored   2 |          16 | 22
        >06:42 | two scored   2 |          16 | 24
        >07:00 | one scored   2 |          18 | 24
        >07:22 | two scored   2 |          18 | 26
        >07:54 | one scored   3 |          21 | 26
        >08:19 | two scored   2 |          21 | 28
        >08:53 | one scored   2 |          23 | 28
        >09:19 | two scored   1 |          23 | 29
        >09:41 | one scored   2 |          25 | 29
        >09:58 | one scored   2 |          27 | 29""".stripMargin('>')

    }

    def verify(inputFile: Path)(expectedData: String): Unit = {
      val expectedLines = expectedData.linesIterator.toList
      val actual = GameApi.forPath(inputFile).asAsciiTable.linesIterator.map(_.trim).toList
      actual.size shouldBe expectedLines.size
      actual.zipWithIndex.zip(expectedLines).foreach {
        case ((actual, index), expectedLine) =>
          withClue(s"$inputFile line $index") {
            actual shouldBe expectedLine
          }
      }
    }
  }
}
