import {CreateGameCommand, GameTransition, TransitionCommand} from 'app/controller/ConnectionController';
import {cls} from 'app/controller/Controllers';
import {ActiveGame, Game} from 'app/store/GameStore';
import {rootStore} from 'app/store/RootStore';
import {Assert} from 'app/util/Assert';
import {action, observable} from 'mobx';

import {deserialize, list, object, serializable} from 'serializr'

export class PlayerState {
  @observable
  @serializable
  id: string;

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
  gameId: string;

  @observable
  @serializable(object(Game))
  public game: Game;
}

export class GamesController {

  @action.bound
  public init() {
    cls.connection.listenEvents('player-state', event => {
      this.handleNewPlayerState(
        deserialize(PlayerState, event)
      );
    });

    cls.connection.listenEvents('active-games-state', event => {
      this.handleNewActiveGamesState(
        deserialize(ActiveGamesState, event)
      );
    });

    cls.connection.listenEvents('game-state', event => {
      this.handleNewGameState(
        deserialize(GameState, event)
      );
    });
  }

  private handleNewPlayerState(playerState: PlayerState): void {
    console.log('New PlayerState: ', playerState);

    rootStore.game.playerId = playerState.id;
    rootStore.game.balance = playerState.balance;
    rootStore.game.playerGames = playerState.games;

    const currentGame = rootStore.game.currentGame;
    const newCurrentGame = playerState.games && playerState.games
      .find(g => currentGame && g.id == currentGame.id);

    rootStore.game.currentGame = newCurrentGame ||
      (playerState.games.length > 0 && playerState.games[0]) || null;
  }

  private handleNewActiveGamesState(activeGameState: ActiveGamesState): void {
    console.log('New ActiveGamesState: ', activeGameState);
    rootStore.game.activeGames = activeGameState.activeGames;
  }

  private handleNewGameState(gameState: GameState): void {
    console.log('New GameState: ', gameState);
  }

  @action.bound
  public createGame(name: string, smallBlind: number, buyIn: number): void {
    cls.connection.send(new CreateGameCommand(name, smallBlind, buyIn));
  }

  @action.bound
  public selectGame(gameId: string): void {
    rootStore.game.currentGame = rootStore.game.playerGames.find(g => g.id == gameId) || null;
  }

  @action.bound
  public transitionGame(gameId: string, transition: GameTransition): void {
    cls.connection.send(new TransitionCommand(gameId, transition));
  }

  @action.bound
  public changeRaiseAmount(raiseAmount: any): void {
    rootStore.game.raiseAmount = raiseAmount;

    const raiseAmountNum = parseInt(raiseAmount);
    if (isNaN(raiseAmountNum) || raiseAmountNum <= 0) {
      rootStore.game.raiseAmountNum = 0;
      rootStore.game.raiseAmountValid = false;
    } else {
      rootStore.game.raiseAmountNum = raiseAmountNum;
      rootStore.game.raiseAmountValid = true;
    }
  }

  @action.bound
  public doRaise(): void {
    const {playerId, currentGame, thisPlayer, currentBalance} = rootStore.game;
    const {raiseAmountNum, raiseAmountValid} = rootStore.game;

    Assert.yes(currentGame);
    Assert.yes(raiseAmountValid);

    const raiseAmount = Math.min(raiseAmountNum, currentBalance);
    this.transitionGame(currentGame.id, {
      transition: 'raise',
      playerId: playerId,
      amount: raiseAmountNum
    })
  }

  @action.bound
  public doCall(): void {
    const {playerId, currentGame} = rootStore.game;
    this.transitionGame(currentGame.id, {
      transition: 'call',
      playerId: playerId
    })
  }

  @action.bound
  public doFold(): void {
    const {playerId, currentGame} = rootStore.game;
    this.transitionGame(currentGame.id, {
      transition: 'fold',
      playerId: playerId
    })
  }

  @action.bound
  public doCheck(): void {
    const {playerId, currentGame} = rootStore.game;
    this.transitionGame(currentGame.id, {
      transition: 'check',
      playerId: playerId
    })
  }

  @action.bound
  public nextRound(gameId: string): void {
    const {playerId} = rootStore.game;
    this.transitionGame(gameId, {
      transition: 'next-round'
    })
  }

  @action.bound
  public join(gameId: string): void {
    const {playerId} = rootStore.game;
    this.transitionGame(gameId, {
      transition: 'join',
      playerId: playerId
    })
  }

  @action.bound
  public leave(gameId: string): void {
    const {playerId} = rootStore.game;
    this.transitionGame(gameId, {
      transition: 'leave',
      playerId: playerId
    })
  }
}
