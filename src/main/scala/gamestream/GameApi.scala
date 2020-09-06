package gamestream

import java.nio.file.Path

import gamestream.io.LineIterator
import gamestream.model._

/**
 * This is the API as described in the spec - the functions which allow clients to interrogate the game data.
 *
 * This is the interface as described in the spec
 */
trait GameApi {

  /**
   * All other functions can be implemented in terms of this function
   *
   * @return all the game events
   */
  def allEvents: Iterable[DataEvent]

  def lastEvent: Option[DataEvent] = allEvents.lastOption

  def lastEvents(limit: Int): Iterable[DataEvent] = allEvents.takeRight(limit)

  def team1Score: Int = lastEvent.fold(0)(_.team1Score.points)

  def team2Score: Int = lastEvent.fold(0)(_.team2Score.points)

  def mostRecentPointsScored: Int = lastEvent.fold(0)(_.scored.points)

  def lastTeamToScore: Option[Team] = lastEvent.map(_.byTeam)

  def asAsciiTable: String = GameApi.format(this)
}

object GameApi {

  /**
   * No logic whatsoever - just warp the events
   *
   * @param gameEvents the events to wrap
   * @return a GameApi for these events
   */
  def identity(gameEvents: Iterable[DataEvent]): GameApi = new GameApi {
    override val allEvents: Iterable[DataEvent] = gameEvents
  }

  def forPath(data: Path): GameApi = {
    val events = LineIterator(data).map(hexStrToInt).map(DataEvent.apply)

    // other APIs are available - e.g. GameStateApi.validating(events)
    GameState(events)
  }

  private[gamestream] def hexStrToInt(str: String) = {
    str match {
      case s"0x${hex}" => Integer.parseInt(hex, 16)
      case hex => Integer.parseInt(hex, 16)
    }
  }

  object format {
    def apply(api: GameApi): String = {
      api.allEvents.map(e => asRow(e)).mkString(" time | event          | team1 score | team2 score\n", "\n", "\n")
    }

    def asRow(event: DataEvent, sep: String = "|") = {
      val team = event.byTeam match {
        case TeamOne => "one"
        case TeamTwo => "two"
      }

      def padPoints(points: Int, width: Int) = points.toString.reverse.padTo(width, ' ').reverse

      s"${event.matchTime} $sep ${team} scored ${padPoints(event.scored.points, 3)} $sep ${padPoints(event.team1Score.points, 11)} $sep ${event.team2Score.points}"
    }
  }

}