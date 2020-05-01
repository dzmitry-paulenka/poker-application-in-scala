import {ConnectionController} from 'app/controller/ConnectionController';
import {GamesController} from 'app/controller/GamesController';
import {ProfileController} from 'app/controller/ProfileController';
import {UiController} from 'app/controller/UiController';

export class Controllers {
  public readonly profile = new ProfileController();
  public readonly connection = new ConnectionController();
  public readonly games = new GamesController();
  public readonly ui = new UiController();

  public async initialize() {
    await this.profile.init();
    await this.games.init();
  }
}

export const cls = new Controllers();
