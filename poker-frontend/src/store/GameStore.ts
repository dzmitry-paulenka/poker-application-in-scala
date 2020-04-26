import {observable} from 'mobx';
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
  public phase: string;

  @observable
  @serializable(list(object(Card)))
  public board: Array<Card>;

  @observable
  @serializable
  public pot: number;

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
  public activeGames: Array<ActiveGame> = [];

  @observable
  public joinedGames: Array<any> = [];
}
