package gamestream.model

import gamestream.GameApi

/**
 * The incoming parsed game event
 */
final case class DataEvent(scored: PointScored, byTeam: Team, team1Score: TotalScore, team2Score: TotalScore, matchTime: ElapsedMatchTime) extends Ordered[DataEvent] {
  override def compare(that: DataEvent): Int = matchTime.compareTo(that.matchTime)
}

object DataEvent {
  def apply(msg: Int): DataEvent = DataEventCodec.fromMessage(msg)
}


final case class PointScored(points: Int) {
  require(points < 4)
  require(points >= 0)
}

final case class ElapsedMatchTime(seconds: Int) extends Ordered[ElapsedMatchTime] {
  require(seconds >= 0)

  override def toString = {
    val min = seconds / 60
    val sec = seconds % 60
    val minPad = if (min < 10) "0" else ""
    val secPad = if (sec < 10) "0" else ""
    s"$minPad$min:$secPad$sec"
  }

  override def compare(that: ElapsedMatchTime): Int = seconds.compareTo(that.seconds)
}

object ElapsedMatchTime {
  def fromMinutesSeconds(minutes: Int, seconds: Int) = {
    require(minutes >= 0)
    require(seconds >= 0)
    ElapsedMatchTime(minutes * 60 + seconds)
  }
}

final case class TotalScore(points: Int) {
  require(points >= 0)
}

sealed class Team(val index: Int)

object Team {
  def apply(index: Int): Team = unapply(index).getOrElse(sys.error(s"Invalid team index: $index"))

  def unapply(index: Int): Option[Team] = index match {
    case TeamOne.index => Some(TeamOne)
    case TeamTwo.index => Some(TeamTwo)
    case _ => None
  }
}

case object TeamOne extends Team(0)

case object TeamTwo extends Team(1)


case class EventError(invalidEvent: DataEvent, eventIndex: Int, previousValidState: GameApi, detail: String) extends Exception {
  override def getMessage = s"Event $eventIndex $invalidEvent cause: $detail"
}
