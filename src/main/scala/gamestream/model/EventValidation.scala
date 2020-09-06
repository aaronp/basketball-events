package gamestream.model

import gamestream.GameApi

/**
 * A place where we keep our validation rules
 */
object EventValidation {

  /**
   *
   * @param previousState the previous game state
   * @param event         the input event
   * @param newState      the new game state
   * @return either None if this was a valid event or an error message wrapped in a Some
   */
  def validate(previousState: GameApi, event: DataEvent, newState: GameApi): Option[String] = {

    // TODO - at the moment we just validate the score. We can continue to add more validation here as well
    // If needed we can provided this detail in the return type instead of just a string if clients need
    // to take different actions based on the type of validation error -- or calling code can just decide
    // to invoke individual validation functions
    previousState.lastEvent.flatMap(validateScore(_, event, newState))
  }

  def validateScore(previousState: DataEvent, event: DataEvent, newState: GameApi): Option[String] = {
    val expectedScore = event.byTeam match {
      case TeamOne =>
        (previousState.team1Score.points + event.scored.points, previousState.team2Score.points)
      case TeamTwo =>
        (previousState.team1Score.points, previousState.team2Score.points + event.scored.points)
    }

    val actual = (newState.team1Score, newState.team2Score)
    if (expectedScore != actual) {
      Option(s"Expected the score to be ${expectedScore} after $event, but it was $actual")
    } else {
      None
    }
  }

}
