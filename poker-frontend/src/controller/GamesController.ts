import {cls} from 'app/controller/Controllers';
import {rootStore} from 'app/store/RootStore';
import {action} from 'mobx';

export class GamesController {

  @action.bound
  public init() {
    cls.connection.listenEvents('game-list', event => {

    });

    cls.connection.listenEvents('game-state', event => {

    });

  }
}
