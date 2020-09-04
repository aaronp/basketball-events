package gamestream.model


final case class DataEvent(scored: PointScored, byTeam : Team, team1Score : TotalScore, team2Score : TotalScore, matchTime : ElapsedMatchTime)

final case class PointScored(points: Int) {
  require(points < 4)
  require(points >= 0)
}

final case class ElapsedMatchTime(seconds : Int) {
  require(seconds >= 0)
}
final case class TotalScore(points : Int) {
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

