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


export class ActiveGame {
  @observable
  @serializable(identifier())
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
  public playersCount: number;
}

export class JoinedGame extends ActiveGame {
}


export class GameStore {
  @observable
  public activeGames: Array<ActiveGame> = [];

  @observable
  public joinedGames: Array<JoinedGame> = [];
}
