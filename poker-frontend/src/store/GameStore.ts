import {rootStore} from 'app/store/RootStore';
import {Assert} from 'app/util/Assert';
import {computed, observable} from 'mobx';
import {
  createModelSchema,
  primitive,
  reference,
  list,
  object,
  identifier,
  serialize,
  deserialize,
  getDefaultModelSchema,
  serializable
} from 'serializr'

export class Card {
  @serializable
  public suit: string;

  @serializable
  public rank: string;

  @computed
  get name(): string {
    return `${this.rank}${this.suit}`
  }

  @computed
  get displayRank(): string {
    return this.rank == 'T' ? '10' : this.rank;
  }

  @computed
  get cssColor(): string {
    return this.suit == 'd' || this.suit == 'h' ? 'red' : 'black'
  }
}

export class Player {
  @observable
  @serializable
  public id: string;

  @observable
  @serializable
  public balance: number;

  @observable
  @serializable(list(object(Card)))
  public hand: Array<Card>;

  @observable
  @serializable
  public gameBet: number;

  @observable
  @serializable
  public roundBet: number;

  @observable
  @serializable
  public resultComboName: string;

  @observable
  @serializable
  public resultMoneyWon: number;

  @observable
  @serializable
  public actedInRound: Boolean;

  @observable
  @serializable
  public allIn: Boolean;

  @observable
  @serializable
  public sittingOut: Boolean;
}

export enum Phase {
  PreDeal = 'pre-deal',
  PreFlop = 'pre-flop',
  Flop = 'flop',
  Turn = 'turn',
  River = 'river',
  Showdown = 'showdown',
  Ended = 'ended'
}

export class Game {
  @observable
  @serializable
  public id: string;

  @observable
  @serializable
  public name: string;

  @observable
  @serializable
  public phase: string;

  @observable
  @serializable(list(object(Card)))
  public board: Array<Card>;

  @observable
  @serializable
  public pot: number;

  @observable
  @serializable
  public smallBlind: number;

  @observable
  @serializable
  public roundBet: number;

  @observable
  @serializable(list(object(Player)))
  public players: Array<Player>;

  @observable
  @serializable
  public currentPlayerIndex: number;

  @observable
  @serializable
  public dealerPlayerIndex: number;
}

export class ActiveGame {
  @observable
  @serializable
  public id: string;

  @observable
  @serializable
  public name: string;

  @observable
  @serializable
  public smallBlind: number;

  @observable
  @serializable
  public buyIn: number;

  @observable
  @serializable
  public playerCount: number;
}

export class GameStore {
  @observable
  public playerId: string;

  @observable
  public balance: number = 0;

  @observable
  public raiseAmount: number = 0;

  @observable
  public raiseAmountNum: number = 0;

  @observable
  public raiseAmountValid: boolean = true;

  @observable
  public activeGames: Array<ActiveGame> = [];

  @observable
  public playerGames: Array<Game> = [];

  @observable
  public currentGame: Game = null;

  @computed
  get thisPlayer(): Player {
    if (!this.currentGame)
      return null;

    const thisPlayer = this.currentGame.players.find(p => p.id == this.playerId);
    Assert.yes(thisPlayer, 'thisPlayer should be defined at this point');

    return thisPlayer;
  }

  @computed
  get currentPlayer(): Player {
    if (!this.currentGame)
      return null;

    return this.activePlayers[this.currentGame.currentPlayerIndex];
  }

  @computed
  get dealerPlayer(): Player {
    if (!this.currentGame)
      return null;

    return this.activePlayers[this.currentGame.dealerPlayerIndex];
  }

  @computed
  get activePlayers(): Array<Player> {
    if (!this.currentGame)
      return [];

    return this.currentGame.players.filter(p => !p.sittingOut);
  }

  @computed
  get currentBalance(): number {
    if (!this.thisPlayer)
      return null;

    return this.thisPlayer.balance;
  }

  @computed
  get canFold(): boolean {
    return this.currentGame &&
      this.thisPlayer == this.currentPlayer;
  }

  @computed
  get canCheck(): boolean {
    return this.currentGame &&
      this.thisPlayer == this.currentPlayer &&
      this.thisPlayer.roundBet == this.currentGame.roundBet;
  }

  @computed
  get canCall(): boolean {
    return this.currentGame &&
      this.thisPlayer == this.currentPlayer &&
      this.thisPlayer.roundBet < this.currentGame.roundBet;
  }

  @computed
  get canRaise(): boolean {
    return this.currentGame &&
      this.thisPlayer == this.currentPlayer &&
      this.currentBalance + this.thisPlayer.roundBet > this.currentGame.roundBet;
  }

  @computed
  get cardsDealt(): boolean {
    return this.thisPlayer && this.thisPlayer.hand.length > 0;
  }

  @computed
  get isShowdown(): boolean {
    return this.currentGame && this.currentGame.phase == 'showdown'
  }

  public thisPlayerIsInGame(gameId: string): boolean {
    return this.playerGames.some(g => g.id == gameId);
  }
}
