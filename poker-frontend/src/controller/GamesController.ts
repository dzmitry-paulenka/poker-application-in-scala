import {cls} from 'app/controller/Controllers';
import {ActiveGame, Game, Player} from 'app/store/GameStore';
import {rootStore} from 'app/store/RootStore';
import {action, observable} from 'mobx';

import {deserialize, serializable, list, object} from 'serializr'

export class PlayerState {
  @observable
  @serializable
  balance: number;

  @observable
  @serializable(list(object(Game)))
  public games: Array<Game>;
}

export class ActiveGamesState {
  @observable
  @serializable(list(object(ActiveGame)))
  public activeGames: Array<ActiveGame>;
}

export class GameState {
  @observable
  @serializable
  gameId: String;

  @observable
  @serializable(object(Game))
  public game: Game;
}

export class GamesController {

  @action.bound
  public init() {
    cls.connection.listenEvents('player-state', event => {
      const playerState = deserialize(PlayerState, event);
      console.log('PlayerState: ', playerState)
    });

    cls.connection.listenEvents('active-games-state', event => {
      const activeGamesState = deserialize(ActiveGamesState, event);
      console.log('ActiveGamesState: ', activeGamesState)
    });

    cls.connection.listenEvents('game-state', event => {
      const gameState = deserialize(GameState, event);
      console.log('GameState: ', GameState)
    });
  }
}
