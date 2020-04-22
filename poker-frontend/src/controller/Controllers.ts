import {ConnectionController} from 'app/controller/ConnectionController';
import {GamesController} from 'app/controller/GamesController';
import {ProfileController} from 'app/controller/ProfileController';

export class Controllers {
  public readonly profile = new ProfileController();
  public readonly connection = new ConnectionController();
  public readonly games = new GamesController();

  public initialize() {
    this.profile.init();
    this.connection.init();
    this.games.init();
  }
}

export const cls = new Controllers();
